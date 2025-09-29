package com.xtremand.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.common.exception.BadRequestException;
import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/contacts")
@Tag(name = "Contact Management", description = "APIs for managing contacts and contact lists")
public class ContactController {
    @Autowired
    private AuthenticationFacade authenticationFacade;

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Upload contacts from a file", description = "Uploads a CSV or Excel file to create new contacts. Optionally map to a contact list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File uploaded and contacts saved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - File is missing, empty, or has an unsupported format",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> uploadFile(
            @Parameter(description = "The CSV or Excel file to upload.") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Whether to skip the header row of the file.") @RequestParam(name = "skipHeader", defaultValue = "true") boolean skipHeader,
            @Parameter(description = "If true, create a new list and add contacts to it") @RequestParam(name = "isListCreate", defaultValue = "false") boolean isListCreate,
            @Parameter(description = "Name of the contact list (if creating a new list)") @RequestParam(name = "listName", required = false) String listName) {
        
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is missing or empty");
        }
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.save(file, skipHeader, isListCreate, listName, authentication);
        return ResponseEntity.ok(new Response("File uploaded and contacts saved successfully"));
    }

    @Operation(summary = "Create a contact list", description = "Creates a new, empty contact list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Response> createList(@RequestBody ContactListRequestDto dto) {
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.createList(dto.getName(), dto.getDescription(), authentication);
        return ResponseEntity.ok(new Response("List created successfully"));
    }

    @Operation(summary = "Update a contact list", description = "Updates the name and/or description of an existing contact list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found - Contact list not found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Response> updateList(
            @Parameter(description = "ID of the contact list to update") @PathVariable Long id,
            @RequestBody ContactListRequestDto dto) {
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.updateList(id, dto.getName(), dto.getDescription(), authentication);
        return ResponseEntity.ok(new Response("List updated successfully"));
    }

    @Operation(summary = "Delete a contact list", description = "Deletes an existing contact list by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - Contact list not found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteList(
            @Parameter(description = "ID of the contact list to delete") @PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.deleteList(id, authentication);
        return ResponseEntity.ok(new Response("List deleted successfully"));
    }

    @Operation(summary = "Add contacts to a list", description = "Adds one or more existing contacts to a specific contact list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contacts added successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request - Contact already in list",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found - Contact list or one of the contacts not found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @PostMapping("/{id}/contacts")
    public ResponseEntity<Response> addContactsToList(
            @Parameter(description = "ID of the contact list") @PathVariable Long id,
            @RequestBody AddContactsToListDto dto) {
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.addContactsToList(id, dto.getContactIds(), authentication);
        return ResponseEntity.ok(new Response("Contacts added successfully"));
    }

    @Operation(summary = "Remove a contact from a list", description = "Removes a specific contact from a specific contact list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact removed successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - Contact list or contact not found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @DeleteMapping("/{listId}/contacts/{contactId}")
    public ResponseEntity<Response> removeContactFromList(
            @Parameter(description = "ID of the contact list") @PathVariable Long listId,
            @Parameter(description = "ID of the contact to remove") @PathVariable Long contactId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.removeContactFromList(listId, contactId, authentication);
        return ResponseEntity.ok(new Response("Contact removed successfully"));
    }

    @Operation(summary = "Get a contact list with its contacts", description = "Retrieves a single contact list and all associated contacts.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved data"),
        @ApiResponse(responseCode = "404", description = "Not Found - Contact list not found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @GetMapping("/{id}/contacts")
    public ResponseEntity<Map<String, List<ContactListDto>>> getContactListWithContacts(
            @Parameter(description = "ID of the contact list") @PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        ContactListDto dto = contactService.getContactListDtoById(id, authentication);
        return ResponseEntity.ok(Map.of("contactLists", List.of(dto)));
    }

    @Operation(summary = "Get all contacts with counts", description = "Retrieves all contacts along with total and active counts.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved data")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllContacts() {
        Authentication authentication = authenticationFacade.getAuthentication();
        Map<String, Object> result = contactService.getAllContactsWithCounts(authentication);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all contact lists", description = "Retrieves a list of all contact lists, with optional search.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved data")
    @GetMapping("/contactlists")
    public List<ContactListDto> getAllContactLists(
            @Parameter(description = "Search term to filter lists by name or description") @RequestParam(required = false) String search) {
        Authentication authentication = authenticationFacade.getAuthentication();
        return contactService.getAllContactLists(search, authentication);
    }

    @Operation(summary = "Get a contact by ID", description = "Retrieves a single contact's details by their ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved contact"),
        @ApiResponse(responseCode = "404", description = "Not Found - Contact not found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class)))
    })
    @GetMapping("/contactInfo/{id}")
    public ContactDto getContactById(@Parameter(description = "ID of the contact to retrieve") @PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        return contactService.getContactById(id, authentication);
    }

    @Operation(summary = "Download CSV", description = "Download CSV Format")
    @ApiResponse(responseCode = "200", description = "Successfully Downloaded")
    @GetMapping("/download-template")
    public ResponseEntity<InputStreamResource> downloadContactCsvTemplate() {
        String[] headers = {
            "first_name", "last_name", "email", "phone", "company", "job_title",
            "location", "tags", "details"
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);
        writer.println(String.join(",", headers));
        writer.flush();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contact_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @Operation(summary = "Create a new contact", description = "Adds a new contact to the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/create")
    public ResponseEntity<ContactDto> createContact(@RequestBody ContactDto dto) {
        Authentication authentication = authenticationFacade.getAuthentication();
        ContactDto created = contactService.createContact(dto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a contact", description = "Updates the details of an existing contact.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact updated successfully"),
        @ApiResponse(responseCode = "404", description = "Contact not found")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<ContactDto> updateContact(@PathVariable Long id, @RequestBody ContactDto dto) {
        Authentication authentication = authenticationFacade.getAuthentication();
        ContactDto updated = contactService.updateContact(id, dto, authentication);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a contact", description = "Deletes a contact by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contact deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Contact not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteContact(@PathVariable Long id) {
        Authentication authentication = authenticationFacade.getAuthentication();
        contactService.deleteContact(id, authentication);
        return ResponseEntity.ok(new Response("Contact deleted successfully"));
    }
}
