package com.xtremand.email.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.search.*;
import com.xtremand.config.repository.MailConfigRepository;
import com.xtremand.contact.repository.ContactListRepository;
import com.xtremand.contact.repository.ContactRepository;
import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.*;
import com.xtremand.domain.entity.*;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.email.repository.*;
import com.xtremand.user.repository.UserRepository;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EmailService {

    @Value("${email.tracking.url}")
    private String trackingUrl;

    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.port}")
    private String smtpPort;

    private final ProducerTemplate producerTemplate;
    private final ContactService contactService;
    private final EmailAnalyticsService analyticsService;
    private final ContactListEmailAnalyticsRepository mappingRepo;
    private final EmailAnalyticsRepository repo;
    private final ContactListRepository contactListRepository;
    private final MailConfigRepository mailConfigRepository;
    private final ContactListEmailAnalyticsRepository contactListEmailAnalyticsRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public EmailService(ProducerTemplate producerTemplate, ContactService contactService,
                        EmailAnalyticsService analyticsService, ContactListEmailAnalyticsRepository mappingRepo,
                        EmailAnalyticsRepository repo, ContactListRepository contactListRepository,
                        MailConfigRepository mailConfigRepository,
                        ContactListEmailAnalyticsRepository contactListEmailAnalyticsRepository,
                        ContactRepository contactRepository,UserRepository userRepository) {
        this.producerTemplate = producerTemplate;
        this.contactService = contactService;
        this.analyticsService = analyticsService;
        this.mappingRepo = mappingRepo;
        this.repo = repo;
        this.contactListRepository = contactListRepository;
        this.mailConfigRepository = mailConfigRepository;
        this.contactListEmailAnalyticsRepository = contactListEmailAnalyticsRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
    	User user = userRepository.fetchByUsername(authentication.getName());
        return user;
    }

    public MailConfig getMailConfigForUser(Authentication authentication,Long configId) {
        User user = getAuthenticatedUser(authentication);
        return mailConfigRepository.findByCreatedByUserAndIds(user, configId);
    }

    private List<ContactDto> expandRecipients(RecipientGroup group) {
        if (group == null)
            return Collections.emptyList();
        List<ContactDto> contacts = new ArrayList<>();
        if (group.getContactIds() != null) {
            for (Long cid : group.getContactIds()) {
                ContactDto c = contactService.getContactById(cid, null);
                if (c != null)
                    contacts.add(c);
            }
        }
        if (group.getEmails() != null) {
            for (String email : group.getEmails()) {
                ContactDto c = contactService.getContactByEmail(email);
                if (c == null) {
                    Contact newContact = new Contact();
                    newContact.setEmail(email);
                    newContact.setActive(true);
                    String localPart = email.split("@")[0];
                    newContact.setFirstName(localPart);
                    c = contactService.saveAndConvertToDto(newContact);
                }
                contacts.add(c);
            }
        }
        if (group.getContactListIdToContactIds() != null) {
            for (Map.Entry<Long, List<Long>> entry : group.getContactListIdToContactIds().entrySet()) {
                Long listId = entry.getKey();
                List<Long> contactIds = entry.getValue();
                ContactList contactList = contactListRepository.findById(listId)
                        .orElseThrow(() -> new RuntimeException("Contact list not found: " + listId));
                if (contactIds == null || contactIds.isEmpty()) {
                    contacts.addAll(contactList.getContacts().stream().map(contactService::mapToDto).toList());
                } else {
                    List<ContactDto> filteredContacts = contactList.getContacts().stream()
                            .filter(c -> contactIds.contains(c.getId())).map(contactService::mapToDto).toList();
                    contacts.addAll(filteredContacts);
                }
            }
        }
        return contacts;
    }

    public void saveContactListEmailAnalyticsForEmail(RecipientGroup group, EmailAnalytics emailAnalytics, String type) {
        if (group == null)
            return;
        if (group.getContactListIdToContactIds() != null) {
            for (Map.Entry<Long, List<Long>> entry : group.getContactListIdToContactIds().entrySet()) {
                Long listId = entry.getKey();
                List<Long> contactIds = entry.getValue();
                ContactList contactList = contactListRepository.findById(listId)
                        .orElseThrow(() -> new RuntimeException("Contact list not found: " + listId));
                List<Contact> contactsToSave;
                if (contactIds == null || contactIds.isEmpty()) {
                    contactsToSave = new ArrayList<>(contactList.getContacts());
                } else {
                    contactsToSave = contactList.getContacts().stream().filter(c -> contactIds.contains(c.getId()))
                            .toList();
                }
                for (Contact contact : contactsToSave) {
                    ContactListEmailAnalytics entryEntity = new ContactListEmailAnalytics();
                    entryEntity.setContactList(contactList);
                    entryEntity.setContact(contact);
                    entryEntity.setType(type);
                    entryEntity.setEmailAnalytics(emailAnalytics);
                    contactListEmailAnalyticsRepository.save(entryEntity);
                }
            }
        }
        if (group.getContactIds() != null) {
            for (Long cid : group.getContactIds()) {
                Contact contact = contactRepository.getReferenceById(cid);
                if (contact != null) {
                    ContactListEmailAnalytics entryEntity = new ContactListEmailAnalytics();
                    entryEntity.setContact(contact);
                    entryEntity.setType(type);
                    entryEntity.setEmailAnalytics(emailAnalytics);
                    contactListEmailAnalyticsRepository.save(entryEntity);
                }
            }
        }
        if (group.getEmails() != null) {
            for (String email : group.getEmails()) {
                Contact contact = contactRepository.findByEmailIgnoreCase(email);
                if (contact == null) {
                    ContactListEmailAnalytics entryEntity = new ContactListEmailAnalytics();
                    entryEntity.setNewEmail(email);
                    entryEntity.setType(type);
                    entryEntity.setEmailAnalytics(emailAnalytics);
                    contactListEmailAnalyticsRepository.save(entryEntity);
                }
            }
        }
    }

    public void sendBulkEmail(SendEmailRequest request, Campaign campaign, Authentication authentication) {
    	MailConfig config = new MailConfig();
    	if (authentication != null) {
			 config = getMailConfigForUser(authentication, request.getConfigId());
		} else {
			 config = mailConfigRepository.getReferenceById(request.getConfigId());
		}
        List<ContactDto> toContacts = expandRecipients(request.getTo());
        List<ContactDto> ccContacts = expandRecipients(request.getCc());
        List<ContactDto> bccContacts = expandRecipients(request.getBcc());

        String toEmails = toContacts.stream().map(ContactDto::getEmail).filter(e -> e != null && !e.isEmpty())
                                .distinct().collect(Collectors.joining(","));
        String ccEmails = ccContacts.stream().map(ContactDto::getEmail).filter(e -> e != null && !e.isEmpty())
                                .distinct().collect(Collectors.joining(","));
        String bccEmails = bccContacts.stream().map(ContactDto::getEmail).filter(e -> e != null && !e.isEmpty())
                                .distinct().collect(Collectors.joining(","));

        String genericBody = personalizeBodyGeneric(request.getBody(), config.getEmail());
        sendEmail(request.getSubject(), genericBody, toEmails, ccEmails, bccEmails, config);

        String generatedMessageId = "<" + UUID.randomUUID() + "@gmail.com>";
        EmailAnalytics emailAnalytics = analyticsService.logEmailSent(null, toEmails, request.getSubject(),
                request.getBody(), null, campaign, request.getPrompt(), generatedMessageId, config.getEmail(), authentication);
        saveContactListEmailAnalyticsForEmail(request.getTo(), emailAnalytics, "to");
        saveContactListEmailAnalyticsForEmail(request.getCc(), emailAnalytics, "cc");
        saveContactListEmailAnalyticsForEmail(request.getBcc(), emailAnalytics, "bcc");

        String allRecipients = Stream.of(toEmails, ccEmails)
                                     .filter(s -> s != null && !s.isEmpty())
                                     .collect(Collectors.joining(","));
        String realMessageId = fetchRealMessageIdWithRetry(request.getSubject(), allRecipients, config);
        if (realMessageId != null && !realMessageId.equals(generatedMessageId)) {
            emailAnalytics.setMessageId(realMessageId);
            analyticsService.update(emailAnalytics);
        }
    }

    private String fetchRealMessageIdWithRetry(String subject, String recipients, MailConfig config) {
        int maxRetries = 10;
        int retryIntervalMs = 3000;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Date tenMinutesAgo = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
            String realMessageId = fetchRealMessageIdFromSentFolder(subject, recipients, tenMinutesAgo, config);
            if (realMessageId != null) {
                return realMessageId;
            }
            try {
                Thread.sleep(retryIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    public String decryptPassword(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private Store connectToImap(MailConfig config) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.ssl.enable", "true");
        boolean isOAuth = config.getConfigType() == EmailConfigType.OAUTH_CONFIG && config.getOauthAccessToken() != null
                && !config.getOauthAccessToken().isEmpty();
        Session session;
        if (isOAuth) {
            props.put("mail.imap.auth.mechanisms", "XOAUTH2");
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getEmail(), config.getOauthAccessToken());
                }
            });
        } else {
            session = Session.getInstance(props);
        }
        Store store = session.getStore("imap");
        if (isOAuth) {
            store.connect("imap.gmail.com", config.getEmail(), config.getOauthAccessToken());
        } else {
            String password = decryptPassword(config.getPassword());
            store.connect("imap.gmail.com", config.getEmail(), password);
        }
        return store;
    }

    private String fetchRealMessageIdFromSentFolder(String subject, String recipients, Date sentAfter,
                                                   MailConfig config) {
        Store store = null;
        Folder sentFolder = null;
        try {
            store = connectToImap(config);
            sentFolder = store.getFolder("[Gmail]/Sent Mail");
            sentFolder.open(Folder.READ_ONLY);
            SearchTerm subjectTerm = new SubjectTerm(subject);
            SearchTerm sentDateTerm = new SentDateTerm(ComparisonTerm.GE, sentAfter);
            SearchTerm andTerm = new AndTerm(subjectTerm, sentDateTerm);
            Message[] foundMessages = sentFolder.search(andTerm);
            Set<String> inputRecipients = Arrays.stream(recipients.split(",")).map(String::trim)
                    .map(String::toLowerCase).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
            for (Message message : foundMessages) {
                if (message instanceof MimeMessage) {
                    List<String> allMessageRecipients = new ArrayList<>();
                    Address[] tos = message.getRecipients(Message.RecipientType.TO);
                    Address[] ccs = message.getRecipients(Message.RecipientType.CC);
                    if (tos != null) {
                        for (Address addr : tos) {
                            allMessageRecipients.add(((InternetAddress) addr).getAddress().toLowerCase());
                        }
                    }
                    if (ccs != null) {
                        for (Address addr : ccs) {
                            allMessageRecipients.add(((InternetAddress) addr).getAddress().toLowerCase());
                        }
                    }
                    Set<String> messageRecipientSet = new HashSet<>(allMessageRecipients);
                    if (messageRecipientSet.equals(inputRecipients)) {
                        return ((MimeMessage) message).getMessageID();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (sentFolder != null && sentFolder.isOpen())
                    sentFolder.close(false);
                if (store != null)
                    store.close();
            } catch (MessagingException me) {
                me.printStackTrace();
            }
        }
        return null;
    }

    private void saveAnalyticsMappingsForContact(ContactDto contact, EmailAnalytics emailAnalytics,
                                                  SendEmailRequest request) {
        saveMappingsFromRecipientGroup(contact, emailAnalytics, request.getTo());
        saveMappingsFromRecipientGroup(contact, emailAnalytics, request.getCc());
        saveMappingsFromRecipientGroup(contact, emailAnalytics, request.getBcc());
    }

    private void saveMappingsFromRecipientGroup(ContactDto contact, EmailAnalytics emailAnalytics,
                                                RecipientGroup group) {
        if (group == null || group.getContactListIdToContactIds() == null)
            return;
        for (Map.Entry<Long, List<Long>> entry : group.getContactListIdToContactIds().entrySet()) {
            Long contactListId = entry.getKey();
            List<Long> contactIds = entry.getValue();
            if (contactIds != null && contactIds.contains(contact.getId())) {
                analyticsService.saveContactAnalyticsMapping(contactListId, emailAnalytics);
            }
        }
    }

    private String personalizeBodyGeneric(String body, String fromEmail) {
        String personalized = body.replace("[Recipient's Name]", "Valued Customer").replace("{{email}}", "")
                .replace("[Name]", "Valued Customer").replace("[firstName]", "").replace("[lastName]", "")
                .replace("[Email]", "").replace("[company]", "").replace("[phoneNumber]", "").replace("[jobtitle]", "")
                .replace("[Location]", "").replace("[SenderEmail]", fromEmail);
        String htmlBody = convertPlainTextToHtml(personalized);
        String genericTrackingId = "generic-tracking-id";
        String trackedPixel = "<img src=\"https://yourserver.com/track/open/" + genericTrackingId
                + "\" width=\"1\" height=\"1\" />";
        String trackedLink = trackingUrl.replace("{trackingId}", genericTrackingId);
        String trackingHtml = "<br><a href=\"" + trackedLink + "\">Click Here</a>" + trackedPixel;
        return htmlBody.replace("</body></html>", trackingHtml + "</body></html>");
    }

    private void sendEmail(String subject, String body, String to, String cc, String bcc, MailConfig config) {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        boolean isOAuth = (config.getConfigType() == EmailConfigType.OAUTH_CONFIG || config.getConfigType() == EmailConfigType.OFFICE || config.getConfigType() == EmailConfigType.OUTLOOK)&& config.getOauthAccessToken() != null
                && !config.getOauthAccessToken().isEmpty();

        if (isOAuth) {
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (isOAuth) {
                    return new PasswordAuthentication(config.getEmail(), config.getOauthAccessToken());
                } else {
                    String password = decryptPassword(config.getPassword());
                    return new PasswordAuthentication(config.getEmail(), password);
                }
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getEmail()));

            if (to != null && !to.isEmpty()) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            }
            if (cc != null && !cc.isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            }
            if (bcc != null && !bcc.isEmpty()) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
            }

            message.setSubject(subject, "UTF-8");
            message.setContent(body, "text/html; charset=UTF-8");
            message.setSentDate(new Date());

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String convertPlainTextToHtml(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }
        String escapedText = plainText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "'");
        String htmlContent = escapedText.replaceAll("(\r\n|\r|\n){2,}", "</p><p>").replaceAll("(\r\n|\r|\n)", "<br/>");
        htmlContent = "<p>" + htmlContent + "</p>";
        return "<html><body style=\"font-family: Arial, sans-serif; color: #333;\">" + htmlContent + "</body></html>";
    }

    public void sendBulkEmailToList(ContactList contactList, String subject, String body, Campaign campaign,Authentication authentication) {
        SendEmailRequest req = new SendEmailRequest();
        req.setSubject(subject);
        req.setBody(body);
        req.setConfigId(campaign.getConfig().getId());
        RecipientGroup toGroup = new RecipientGroup();
        toGroup.setContactIds(contactList.getContacts().stream().map(Contact::getId).toList());
        req.setTo(toGroup);
        req.setUserId(campaign.getCreatedBy().getId());
        sendBulkEmail(req, campaign,authentication);
    }

    public void deleteEmailByMessageId(Authentication authentication, String messageId) throws Exception {
        User user = getAuthenticatedUser(authentication);
        MailConfig config = getMailConfigForUser(authentication,null);
        Store store = connectToImap(config);
        if (!messageId.startsWith("<")) {
            messageId = "<" + messageId;
        }
        if (!messageId.endsWith(">")) {
            messageId += ">";
        }
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        Message[] messages = inbox.search(new MessageIDTerm(messageId));
        Folder folderToModify = inbox;
        if (messages == null || messages.length == 0) {
            inbox.close(false);
            Folder sent = store.getFolder("[Gmail]/Sent Mail");
            sent.open(Folder.READ_WRITE);
            messages = sent.search(new MessageIDTerm(messageId));
            folderToModify = sent;
            if (messages == null || messages.length == 0) {
                sent.close(false);
                store.close();
                return;
            }
        }
        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.DELETED, true);
            Optional<EmailAnalytics> emailOpt = repo.findByMessageId(messageId);
            if (emailOpt.isPresent() && emailOpt.get().getCreatedBy().equals(user)) { // verify ownership
                EmailAnalytics email = emailOpt.get();
                email.setDeleted(true);
                repo.save(email);
            }
        }

        folderToModify.close(true);
        store.close();
    }

    public void starEmail(Authentication authentication, String messageId, boolean star) throws Exception {
        User user = getAuthenticatedUser(authentication);
        MailConfig config = getMailConfigForUser(authentication,null);
        Store store = connectToImap(config);
        if (!messageId.startsWith("<")) {
            messageId = "<" + messageId;
        }
        if (!messageId.endsWith(">")) {
            messageId += ">";
        }
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        Message[] messages = inbox.search(new MessageIDTerm(messageId));
        Folder folderToModify = inbox;
        if (messages == null || messages.length == 0) {
            inbox.close(false);
            Folder sent = store.getFolder("[Gmail]/Sent Mail");
            sent.open(Folder.READ_WRITE);
            messages = sent.search(new MessageIDTerm(messageId));
            folderToModify = sent;
            if (messages == null || messages.length == 0) {
                sent.close(false);
                store.close();
                return;
            }
        }
        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.FLAGGED, star);
            Optional<EmailAnalytics> emailOpt = repo.findByMessageId(messageId);
            if (emailOpt.isPresent() && emailOpt.get().getCreatedBy().equals(user)) { // verify ownership
                EmailAnalytics email = emailOpt.get();
                email.setStarred(star);
                repo.save(email);
            }
        }
        folderToModify.close(false);
        store.close();
    }

    public void replyToEmail(Authentication authentication, String messageId, String replyBody,Long configType) throws Exception {
        User user = getAuthenticatedUser(authentication);
        MailConfig config = getMailConfigForUser(authentication,configType);
        Store store = connectToImap(config);
        if (!messageId.startsWith("<")) {
            messageId = "<" + messageId;
        }
        if (!messageId.endsWith(">")) {
            messageId += ">";
        }
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        Message[] messages = inbox.search(new MessageIDTerm(messageId));
        Folder folderToUse = inbox;
        if (messages == null || messages.length == 0) {
            inbox.close(false);
            Folder sent = store.getFolder("[Gmail]/Sent Mail");
            sent.open(Folder.READ_ONLY);
            messages = sent.search(new MessageIDTerm(messageId));
            folderToUse = sent;

            if (messages == null || messages.length == 0) {
                sent.close(false);
                store.close();
                throw new Exception("Original message not found for reply");
            }
        }
        Message originalMessage = messages[0];
        Session session = createSmtpSession(config);
        Message replyMessage = originalMessage.reply(true);
        replyMessage.setFrom(new InternetAddress(config.getEmail()));
        replyMessage.setText(replyBody);
        replyMessage.setSentDate(new Date());
        Transport transport = session.getTransport("smtp");
        try {
            if (config.getConfigType() == EmailConfigType.OAUTH_CONFIG && config.getOauthAccessToken() != null) {
                transport.connect(smtpHost, Integer.parseInt(smtpPort), config.getEmail(),
                        config.getOauthAccessToken());
            } else {
                String password = decryptPassword(config.getPassword());
                transport.connect(smtpHost, Integer.parseInt(smtpPort), config.getEmail(), password);
            }
            transport.sendMessage(replyMessage, replyMessage.getAllRecipients());
        } finally {
            transport.close();
            folderToUse.close(false);
            store.close();
        }
    }

    private Session createSmtpSession(MailConfig config) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", config.getPort());
        boolean isOAuth = config.getConfigType() == EmailConfigType.OAUTH_CONFIG
                && config.getOauthAccessToken() != null;
        if (isOAuth) {
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getEmail(), config.getOauthAccessToken());
                }
            });
        } else {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getEmail(), decryptPassword(config.getPassword()));
                }
            });
        }
    }
    
    
    public void markImportantByEmailAndBody(List<String> targets,MailConfig config) throws Exception {
//        List<String> targets = config.getImportantEmail();
        if (targets == null || targets.isEmpty()) {
            throw new RuntimeException("Important email address(es) not configured ");
        }
        Set<String> normalizedTargets = targets.stream()
            .filter(em -> em != null && !em.trim().isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        Store store = connectToImap(config);

        List<EmailReplyDto> allEmails = new ArrayList<>();
        try {
            String[] foldersToCheck = new String[] { "INBOX", "[Gmail]/Sent Mail" };
            for (String folderName : foldersToCheck) {
                Folder folder = store.getFolder(folderName);
                folder.open(Folder.READ_WRITE);
                Message[] messages = folder.getMessages();
                for (Message message : messages) {
                    boolean shouldMarkImportant = false;
                    if ("[Gmail]/Sent Mail".equalsIgnoreCase(folderName)) {
                        Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
                        if (toAddresses != null) {
                            for (Address addr : toAddresses) {
                                String toEmail = ((InternetAddress) addr).getAddress().toLowerCase();
                                if (normalizedTargets.contains(toEmail)) {
                                    shouldMarkImportant = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        Address[] fromAddresses = message.getFrom();
                        if (fromAddresses != null) {
                            for (Address addr : fromAddresses) {
                                String fromEmail = ((InternetAddress) addr).getAddress().toLowerCase();
                                if (normalizedTargets.contains(fromEmail)) {
                                    shouldMarkImportant = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!shouldMarkImportant) {
                        String bodyText = extractTextFromMessage(message);
                        if (bodyText != null) {
                            for (String target : normalizedTargets) {
                                if (bodyText.toLowerCase().contains(target)) {
                                    shouldMarkImportant = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (shouldMarkImportant) {
//                        message.setFlag(Flags.Flag.FLAGGED, true);
                    	Flags importantFlag = new Flags("\\Important");
                    	message.setFlags(importantFlag, true);
                    }
                }
                folder.close(true);
            }
        } finally {
            store.close();
        }
    }

    public List<SentEmailWithReplyFlagDto> fetchImportantEmailsByTargetEmail(Authentication auth, String targetEmail,Long configId) throws Exception {
        User user = getAuthenticatedUser(auth);
        MailConfig config = getMailConfigForUser(auth, configId);
        Store store = connectToImap(config);
        List<EmailReplyDto> allEmails = new ArrayList<>();
        try {
            Folder folder = store.getFolder("[Gmail]/Important");
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            String targetLower = targetEmail.toLowerCase();
            for (Message message : messages) {
                boolean matchesTarget = false;
                Address[] fromAddresses = message.getFrom();
                if (fromAddresses != null) {
                    for (Address addr : fromAddresses) {
                        if (((InternetAddress) addr).getAddress().toLowerCase().contains(targetLower)) {
                            matchesTarget = true;
                            break;
                        }
                    }
                }
                if (!matchesTarget) {
                    Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
                    if (toAddresses != null) {
                        for (Address addr : toAddresses) {
                            if (((InternetAddress) addr).getAddress().toLowerCase().contains(targetLower)) {
                                matchesTarget = true;
                                break;
                            }
                        }
                    }
                }
                if (!matchesTarget) {
                    String bodyText = extractTextFromMessage(message);
                    if (bodyText != null && bodyText.toLowerCase().contains(targetLower)) {
                        matchesTarget = true;
                    }
                }
                if (matchesTarget) {
                    allEmails.add(mapToEmailReplyDto(message));
                }
            }
            folder.close(false);
        } finally {
            store.close();
        }

        return buildThreadedEmails(allEmails);
    }

    
    
    private EmailReplyDto mapToEmailReplyDto(Message message) throws MessagingException {
        EmailReplyDto dto = new EmailReplyDto();
        MimeMessage mimeMessage = (MimeMessage) message;
        dto.setId(null);  // or set if you have entity id
        dto.setSubject(mimeMessage.getSubject());
        dto.setMessageId(mimeMessage.getMessageID());
        String[] inReplyToHeader = mimeMessage.getHeader("In-Reply-To");
        dto.setInReplyTo(inReplyToHeader != null && inReplyToHeader.length > 0 ? inReplyToHeader[0] : null);
        dto.setFromEmail(fromAddressesToString(mimeMessage.getFrom()));
        dto.setToEmail(fromAddressesToString(mimeMessage.getAllRecipients()));
        dto.setBody(extractTextFromMessage(message));
        // You can set isIncoming, replies as per your logic
        dto.setReplies(new ArrayList<>());
        return dto;
    }

    private String fromAddressesToString(Address[] addresses) {
        if (addresses == null || addresses.length == 0) return "";
        return Arrays.stream(addresses)
            .map(Address::toString)
            .collect(Collectors.joining(", "));
    }

    private List<SentEmailWithReplyFlagDto> buildThreadedEmails(List<EmailReplyDto> allEmails) {
        Map<String, EmailReplyDto> emailMap = new HashMap<>();
        for (EmailReplyDto email : allEmails) {
            emailMap.put(email.getMessageId(), email);
        }

        List<SentEmailWithReplyFlagDto> topLevelThreads = new ArrayList<>();

        for (EmailReplyDto email : allEmails) {
            String parentId = email.getInReplyTo();
            if (parentId != null && emailMap.containsKey(parentId)) {
                EmailReplyDto parent = emailMap.get(parentId);
                parent.getReplies().add(email);
            } else {
                boolean hasReplies = !email.getReplies().isEmpty();
                topLevelThreads.add(new SentEmailWithReplyFlagDto(email, hasReplies));
            }
        }
        return topLevelThreads;
    }

    private String extractTextFromMessage(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        return (String) part.getContent();
                    } else if (part.isMimeType("text/html")) {
                        String html = (String) part.getContent();
                        return html.replaceAll("<[^>]*>", "");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public ResponseEntity<List<MailConfigImportantEmailsDto>> fetchConfiguredEmailAddresses(Authentication auth, Long configId) {
        User user = getAuthenticatedUser(auth);
        try {
            Optional<MailConfig> configOpt = mailConfigRepository.findByCreatedByUserAndId(user.getEmail(), configId);

            List<MailConfigImportantEmailsDto> result = configOpt.stream()
                .map(config -> new MailConfigImportantEmailsDto(
                    config.getId(),
                    config.getImportantEmail() == null ? Collections.emptyList() : config.getImportantEmail()
                ))
                .filter(dto -> !dto.getImportantEmails().isEmpty())
                .collect(Collectors.toList());

            if (result.isEmpty()) {
                return ResponseEntity.status(404).body(Collections.emptyList());
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }



}
