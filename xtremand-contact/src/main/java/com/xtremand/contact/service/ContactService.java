package com.xtremand.contact.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.xtremand.common.exception.BadRequestException;
import com.xtremand.common.exception.RecordNotFoundException;
import com.xtremand.contact.repository.ContactListRepository;
import com.xtremand.contact.repository.ContactRepository;
import com.xtremand.contact.specification.ContactListSpecification;
import com.xtremand.domain.dto.ContactDto;
import com.xtremand.domain.dto.ContactListDto;
import com.xtremand.domain.entity.Contact;
import com.xtremand.domain.entity.ContactList;
import com.xtremand.domain.entity.User;
import com.xtremand.user.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, ContactListRepository contactListRepository,UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.contactListRepository = contactListRepository;
        this.userRepository = userRepository;
    }

    public ContactDto createContact(ContactDto dto, Authentication authentication) {
        User user = userRepository.fetchByUsername(authentication.getName());
        Contact contact = toEntity(dto);
        contact.setCreatedBy(user);
        contact.setCreatedAt(LocalDate.now());
        contact = contactRepository.save(contact);
        return toDto(contact);
    }

    public ContactDto updateContact(Long id, ContactDto dto, Authentication authentication) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Contact not found with id: " + id));
        User user = userRepository.fetchByUsername(authentication.getName());

        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setCompany(dto.getCompany());
        contact.setJobTitle(dto.getJobTitle());
        contact.setLocation(dto.getLocation());
        contact.setTags(dto.getTags());
        contact.setDetails(dto.getDetails());
        contact.setActive(dto.isActive());
        contact.setUpdatedBy(user);
        contact.setUpdatedAt(LocalDate.now());

        contact = contactRepository.save(contact);
        return toDto(contact);
    }

    public void deleteContact(Long id, Authentication authentication) {
        if (!contactRepository.existsById(id)) {
            throw new RecordNotFoundException("Contact not found with id: " + id);
        }
        // Optional: add audit logging here with user info from authentication
        contactRepository.deleteById(id);
    }

    private Contact toEntity(ContactDto dto) {
        Contact contact = new Contact();
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setCompany(dto.getCompany());
        contact.setJobTitle(dto.getJobTitle());
        contact.setLocation(dto.getLocation());
        contact.setTags(dto.getTags());
        contact.setDetails(dto.getDetails());
        contact.setActive(dto.isActive());
        return contact;
    }

    private ContactDto toDto(Contact contact) {
        return new ContactDto(
                contact.getId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getJobTitle(),
                contact.getCompany(),
                contact.getLocation(),
                contact.getTags(),
                contact.getCreatedAt(),
                contact.getUpdatedAt(),
                contact.isActive(),
                contact.getDetails()
        );
    }

    /* ======================= File Upload & Parsing ======================= */

    public void save(MultipartFile file, boolean skipHeader, boolean isListCreate, String listName, Authentication authentication) {
        String filename = Optional.ofNullable(file.getOriginalFilename()).filter(name -> !name.isBlank())
                .orElseThrow(() -> new BadRequestException("Filename is missing."));
        List<Contact> contacts = parseFile(file, filename, skipHeader);
        List<Contact> contactsWithEmail = contacts.stream()
                .filter(contact -> contact.getEmail() != null && !contact.getEmail().isBlank()).toList();
        Set<String> existingEmails = contactRepository.findAll().stream().map(Contact::getEmail)
                .filter(email -> email != null && !email.isBlank()).map(String::toLowerCase)
                .collect(Collectors.toSet());
        List<Contact> filteredContacts = contactsWithEmail.stream().filter(contact -> {
            String email = contact.getEmail().toLowerCase();
            return !existingEmails.contains(email);
        }).toList();
        if (filteredContacts.isEmpty()) {
            throw new BadRequestException("No new contacts to import.");
        }

        // Set createdBy and createdAt on each contact before saving
        User user = userRepository.fetchByUsername(authentication.getName());
        LocalDate now = LocalDate.now();
        filteredContacts.forEach(contact -> {
            contact.setCreatedBy(user);
            contact.setCreatedAt(now);
        });

        List<Contact> savedContacts = contactRepository.saveAll(filteredContacts);

        if (isListCreate) {
            String finalListName = listName;
            if (finalListName == null || finalListName.isBlank()) {
                String base = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
                String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                finalListName = base + "_" + stamp;
            }
            if (contactListRepository.existsByNameIgnoreCase(finalListName)) {
                throw new BadRequestException("A contact list with this name already exists: " + finalListName);
            }
            ContactList contactList = new ContactList();
            contactList.setName(finalListName);
            contactList.setDescription("Imported from: " + filename);
            contactList.setCreatedAt(now);
            contactList.setCreatedBy(user);
            contactList.setUpdatedAt(now);
            contactList.setContacts(new HashSet<>(savedContacts));
            contactListRepository.save(contactList);
        }
    }


    /*
     * ======= Header normalization and validation helpers (shared CSV/Excel)
     * =======
     */

    private static String normalizeHeader(String header) {
        return header == null ? "" : header.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private Map<String, Integer> buildHeaderIndexMap(List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); ++i) {
            map.put(normalizeHeader(headers.get(i)), i);
        }
        return map;
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        if (!headerMap.containsKey(normalizeHeader("email"))) {
            throw new BadRequestException("File must contain an 'email' column for import.");
        }
    }

    /* ================== CSV parsing ================== */

    private List<Contact> parseCsv(MultipartFile file, boolean skipHeader) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                throw new BadRequestException("CSV file is empty");
            }
            List<String> headerList = Arrays.asList(rows.get(0));
            Map<String, Integer> headerMap = buildHeaderIndexMap(headerList);
            validateHeaders(headerMap);

            int startIndex = skipHeader ? 1 : 0;
            return rows.stream().skip(startIndex).map(row -> buildContactFromRow(row, headerMap)).toList();
        }
    }

    private Contact buildContactFromRow(String[] row, Map<String, Integer> headerMap) {
        Contact contact = new Contact();
        contact.setFirstName(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("first_name"), -1)));
        contact.setLastName(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("last_name"), -1)));
        contact.setEmail(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("email"), -1)));
        contact.setPhone(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("phone"), -1)));
        contact.setJobTitle(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("job_title"), -1)));
        contact.setCompany(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("company"), -1)));
        contact.setLocation(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("location"), -1)));
        contact.setTags(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("tags"), -1)));
        contact.setDetails(getSafeValue(row, headerMap.getOrDefault(normalizeHeader("details"), -1)));
        String activeStr = getSafeValue(row, headerMap.getOrDefault(normalizeHeader("is_active"), -1));
        contact.setActive(!activeStr.isBlank() && Boolean.parseBoolean(activeStr));
        return contact;
    }

    /*
     * ================== Excel parsing with robust header logic ==================
     */
    private List<Contact> parseExcel(MultipartFile file, boolean skipHeader) throws IOException {
        List<Contact> contacts = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                throw new BadRequestException("Excel sheet is empty");
            }
            Row headerRow = rowIterator.next();
            List<String> headerCells = new ArrayList<>();
            for (Cell cell : headerRow) {
                headerCells.add(getCellValue(cell));
            }
            Map<String, Integer> headerMap = buildHeaderIndexMap(headerCells);
            validateHeaders(headerMap);
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int size = headerCells.size();
                String[] values = new String[size];
                for (int i = 0; i < size; ++i) {
                    values[i] = getCellValue(row.getCell(i));
                }
                contacts.add(buildContactFromRow(values, headerMap));
            }
        }
        return contacts;
    }

    private String getSafeValue(String[] row, int index) {
        return index >= 0 && index < row.length ? Optional.ofNullable(row[index]).orElse("") : "";
    }

    private String getCellValue(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> evaluateFormula(cell);
            default -> "";
        };
    }

    private String evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            return switch (evaluator.evaluateFormulaCell(cell)) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private List<Contact> parseFile(MultipartFile file, String filename, boolean skipHeader) {
        try {
            if (filename.endsWith(".csv")) {
                return parseCsv(file, skipHeader);
            } else if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
                return parseExcel(file, skipHeader);
            } else {
                throw new BadRequestException("Unsupported file type: " + filename);
            }
        } catch (IOException | CsvException e) {
            throw new BadRequestException("Failed to parse file: " + filename + " (" + e.getMessage() + ")");
        }
    }

    /* ======================= Contact List Management ======================= */

    public ContactList createList(String name, String description, Authentication authentication) {
        User user = userRepository.fetchByUsername(authentication.getName());
        if (contactListRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("A contact list with the given name already exists");
        }
        ContactList list = new ContactList();
        list.setName(name);
        list.setDescription(description);
        list.setCreatedAt(LocalDate.now());
        list.setCreatedBy(user);
        list.setUpdatedAt(LocalDate.now());
        list.setUpdatedBy(user);
        return contactListRepository.save(list);
    }

    public ContactList updateList(Long id, String name, String description, Authentication authentication) {
        ContactList list = getListById(id);
        User user = userRepository.fetchByUsername(authentication.getName());
        if (contactListRepository.existsByNameIgnoreCase(name) && !list.getName().equalsIgnoreCase(name)) {
            throw new BadRequestException("A contact list with the given name already exists");
        }
        list.setName(name);
        list.setDescription(description);
        list.setUpdatedAt(LocalDate.now());
        list.setUpdatedBy(user);
        return contactListRepository.save(list);
    }

    public void deleteList(Long id, Authentication authentication) {
        // Optional: audit with user info
        contactListRepository.deleteById(id);
    }

    public void addContactsToList(Long listId, List<Long> contactIds, Authentication authentication) {
        ContactList list = getListById(listId);
        Set<Long> existingContactIds = list.getContacts().stream().map(Contact::getId).collect(Collectors.toSet());

        List<Long> duplicates = contactIds.stream().filter(existingContactIds::contains).collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            throw new BadRequestException("The following contacts are already in the list: " + duplicates);
        }
        List<Contact> contacts = contactRepository.findAllById(contactIds);
        list.getContacts().addAll(contacts);
        contactListRepository.save(list);
    }

    public void removeContactFromList(Long listId, Long contactId, Authentication authentication) {
        ContactList list = getListById(listId);
        list.getContacts().removeIf(contact -> contactId.equals(contact.getId()));
        contactListRepository.save(list);
    }

    public List<ContactListDto> getAllContactLists(String search, Authentication authentication) {
        Specification<ContactList> spec = ContactListSpecification.hasNameOrDescriptionLike(search);
        List<ContactList> lists = contactListRepository.findAll(spec);
        return lists.stream().map(list -> new ContactListDto(list.getId(), list.getName(), list.getDescription(),
                list.getContacts().size())).toList();
    }

    public ContactListDto getContactListDtoById(Long listId, Authentication authentication) {
        ContactList list = getListById(listId);
        List<ContactDto> contactDtos = list.getContacts().stream().map(this::mapToDto).toList();
        return new ContactListDto(list.getId(), list.getName(), list.getDescription(), contactDtos);
    }

    public Map<String, Object> getAllContactsWithCounts(Authentication authentication) {
        List<ContactDto> contacts = getAllContacts();
        long totalContacts = contacts.size();
        long activeContacts = contacts.stream().filter(ContactDto::isActive).count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalContacts", totalContacts);
        response.put("activeContacts", activeContacts);
        response.put("contacts", contacts);

        return response;
    }

    public List<ContactDto> getAllContacts() {
        return contactRepository.findAll().stream().map(this::mapToDto).toList();
    }

    public ContactList getListById(Long id) {
        return contactListRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Contact list not found with id: " + id));
    }

    public ContactDto mapToDto(Contact contact) {
        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        dto.setJobTitle(contact.getJobTitle());
        dto.setCompany(contact.getCompany());
        dto.setLocation(contact.getLocation());
        dto.setTags(contact.getTags());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());
        dto.setActive(contact.isActive());
        return dto;
    }

    public ContactDto getContactById(Long id, Authentication authentication) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Contact not found with id: " + id));
        return mapToDto(contact);
    }

    public ContactDto getContactByEmail(String email) {
        Contact contact = contactRepository.findByEmailIgnoreCase(email);
        if (contact == null) {
            return null;
        }
        return mapToDto(contact);
    }

    public ContactDto saveAndConvertToDto(Contact contact) {
        Contact saved = contactRepository.save(contact);
        return mapToDto(saved);
    }
}
