package com.xtremand.email.service;

import com.xtremand.common.exception.RecordNotFoundException;
import com.xtremand.config.repository.MailConfigRepository;
import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.EmailAnalyticsSummaryDto;
import com.xtremand.domain.dto.EmailHistoryDto;
import com.xtremand.domain.dto.EmailReplyDto;
import com.xtremand.domain.dto.SentEmailWithReplyFlagDto;
import com.xtremand.domain.entity.*;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.domain.enums.EmailStatus;
import com.xtremand.email.repository.ContactListEmailAnalyticsRepository;
import com.xtremand.email.repository.EmailAnalyticsRepository;
import com.xtremand.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmailAnalyticsService {

    @Value("${email.tracking.url}")
    private String trackingUrl;

    @Autowired
    private EmailInboxPollService emailInboxPollService;

    @Autowired
    private MailConfigRepository mailConfigRepository;

    private final EmailAnalyticsRepository repo;

    private final ContactService contactService;

    private final ContactListEmailAnalyticsRepository mappingRepo;
    
    private final UserRepository userRepository;
    
    private final ContactListEmailAnalyticsRepository contactListEmailAnalyticsRepository;

    public EmailAnalyticsService(EmailAnalyticsRepository repo, ContactService contactService,
                                 ContactListEmailAnalyticsRepository mappingRepo,UserRepository userRepository,ContactListEmailAnalyticsRepository contactListEmailAnalyticsRepository) {
        this.repo = repo;
        this.contactService = contactService;
        this.mappingRepo = mappingRepo;
        this.userRepository = userRepository;
        this.contactListEmailAnalyticsRepository = contactListEmailAnalyticsRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
    	User user = userRepository.fetchByUsername(authentication.getName());
        return user;
    }

    public EmailAnalytics logEmailSent(Long contactId, String email, String subject, String body, UUID trackingId,
                                       Campaign campaign, String prompt, String messageId, String fromEmail, Authentication authentication) {
        User createdBy = getAuthenticatedUser(authentication);
        EmailAnalytics analytics = new EmailAnalytics();
        analytics.setFromEmail(fromEmail);
        analytics.setSubject(subject);
        analytics.setBody(body);
        analytics.setPrompt(prompt);
        analytics.setSentAt(LocalDateTime.now());
        analytics.setStatus(EmailStatus.OPENED);
        analytics.setOpenedAt(LocalDateTime.now());
        trackingId = UUID.randomUUID();
        analytics.setTrackingId(trackingId);
        String trackedLink = trackingUrl.replace("{trackingId}", trackingId.toString());
        analytics.setTrackingUrl(trackedLink);
        analytics.setCampaign(campaign);
        analytics.setMessageId(messageId);
        analytics.setIncoming(false);
        analytics.setCreatedBy(createdBy);
        EmailAnalytics a = repo.save(analytics);
        repo.flush();
        return a;
    }

    public void logEmailNotSent(Long contactId, String email, String subject, String body, UUID trackingId,
                                Campaign campaign, String prompt, Authentication authentication) {
        User createdBy = getAuthenticatedUser(authentication);
        EmailAnalytics analytics = new EmailAnalytics();
        analytics.setSubject(subject);
        analytics.setBody(body);
        analytics.setPrompt(prompt);
        analytics.setStatus(EmailStatus.NOT_SENT);
        analytics.setCampaign(campaign);
        analytics.setCreatedBy(createdBy);
        repo.save(analytics);
    }

    public void logClicked(UUID trackingId) {
        repo.findByTrackingId(trackingId).ifPresent(a -> {
            a.setStatus(EmailStatus.CLICKED);
            a.setClickedAt(LocalDateTime.now());
            repo.save(a);
        });
    }

    public void logBounced(UUID trackingId) {
        repo.findByTrackingId(trackingId).ifPresent(a -> {
            a.setStatus(EmailStatus.BOUNCED);
            a.setBouncedAt(LocalDateTime.now());
            repo.save(a);
        });
    }

    public void logReplied(UUID trackingId) {
        repo.findByTrackingId(trackingId).ifPresent(a -> {
            a.setStatus(EmailStatus.REPLIED);
            a.setRepliedAt(LocalDateTime.now());
            repo.save(a);
        });
    }

    public EmailAnalyticsSummaryDto getEmailAnalyticsSummaryForUser(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<EmailAnalytics> allRecords = repo.findAllByCreatedBy(user);
        long totalEmails = allRecords.size();
        long sentCount = allRecords.stream().filter(r -> r.getSentAt() != null).count();
        long openedCount = allRecords.stream().filter(r -> r.getOpenedAt() != null).count();
        long clickedCount = allRecords.stream().filter(r -> r.getClickedAt() != null).count();
        long bouncedCount = allRecords.stream().filter(r -> r.getBouncedAt() != null).count();
        long notSentCount = totalEmails - sentCount;
        long repliedCount = allRecords.stream().filter(r -> r.getRepliedAt() != null).count();
        double openRate = sentCount == 0 ? 0 : (double) openedCount / sentCount * 100;
        double clickRate = sentCount == 0 ? 0 : (double) clickedCount / sentCount * 100;
        double bounceRate = sentCount == 0 ? 0 : (double) bouncedCount / sentCount * 100;
        double notSentRate = totalEmails == 0 ? 0 : (double) notSentCount / totalEmails * 100;
        double replyRate = sentCount == 0 ? 0 : (double) repliedCount / sentCount * 100;
        return new EmailAnalyticsSummaryDto(totalEmails, sentCount, notSentCount, openedCount, clickedCount,
                bouncedCount, repliedCount, openRate, bounceRate, notSentRate, clickRate, replyRate);
    }

    public List<EmailHistoryDto> getAllEmailHistoryForUser(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<EmailAnalytics> allRecords = repo.findAllByCreatedBy(user);
        List<EmailHistoryDto> history = new ArrayList<>();
        for (EmailAnalytics record : allRecords) {
            EmailStatus status = record.getSentAt() != null ? EmailStatus.SENT : EmailStatus.NOT_SENT;
            history.add(new EmailHistoryDto(record.getId(), status, record.getSubject(),
                    record.getBody(), record.getSentAt()));
        }
        history.sort(Comparator.comparing(EmailHistoryDto::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
        return history;
    }

    public EmailHistoryDto getEmailHistoryByIdForUser(Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Optional<EmailAnalytics> optionalRecord = repo.findByIdAndCreatedBy(id, user);
        if (optionalRecord.isPresent()) {
            EmailAnalytics record = optionalRecord.get();
            if (record.getSentAt() != null) {
                return new EmailHistoryDto(record.getId(), EmailStatus.SENT, record.getSubject(),
                        record.getBody(), record.getSentAt());
            } else {
                return new EmailHistoryDto(record.getId(), EmailStatus.NOT_SENT,
                        record.getSubject(), record.getBody(), null);
            }
        } else {
            throw new RecordNotFoundException("No email analytics record with id: " + id);
        }
    }

    public List<SentEmailWithReplyFlagDto> getSentEmailsWithReplyStatusForUser(Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        MailConfig config = mailConfigRepository.findByCreatedByUserAndId(user.getEmail(),id)
                .orElseThrow(() -> new RuntimeException("Mail configuration not found for user: " + user.getId()));
        List<EmailAnalytics> sentEmails = repo.findByIsIncomingFalseAndCreatedBy(user);
        List<SentEmailWithReplyFlagDto> result = new ArrayList<>();
        for (EmailAnalytics sent : sentEmails) {
            EmailReplyDto sentDto = toDto(sent);
            List<EmailReplyDto> replies = getReplyChain(sent.getMessageId());
            sentDto.setReplies(replies);
            boolean hasReplies = replies != null && !replies.isEmpty();
            result.add(new SentEmailWithReplyFlagDto(sentDto, hasReplies));
        }
        return result;
    }

    public void saveContactAnalyticsMapping(Long contactListId, EmailAnalytics emailAnalytics) {
        ContactListEmailAnalytics mapping = new ContactListEmailAnalytics();
        ContactList list = contactService.getListById(contactListId);
        if (list == null) {
            throw new RecordNotFoundException("No contact list found with id: " + contactListId);
        }
        mapping.setContactList(list);
        EmailAnalytics analytics = repo.getReferenceById(emailAnalytics.getId());
        mapping.setEmailAnalytics(analytics);
        mappingRepo.save(mapping);
        mappingRepo.flush();
    }

    public void update(EmailAnalytics emailAnalytics) {
        repo.save(emailAnalytics);
    }

    private EmailReplyDto toDto(EmailAnalytics email) {
        EmailReplyDto dto = new EmailReplyDto();
        dto.setId(email.getId());
        dto.setSubject(email.getSubject());
        dto.setBody(email.getBody());
        dto.setFromEmail(email.getFromEmail());
        dto.setMessageId(email.getMessageId());
        dto.setInReplyTo(email.getInReplyTo());
        dto.setIncoming(email.isIncoming());
        List<ContactListEmailAnalytics> recipientLinks = contactListEmailAnalyticsRepository.findByEmailAnalytics(email);

        List<String> toEmails = recipientLinks.stream()
        	    .filter(link -> "TO".equalsIgnoreCase(link.getType()))
        	    .map(this::resolveEmail)
        	    .filter(Objects::nonNull)
        	    .collect(Collectors.toList());

        	List<String> ccEmails = recipientLinks.stream()
        	    .filter(link -> "CC".equalsIgnoreCase(link.getType()))
        	    .map(this::resolveEmail)
        	    .filter(Objects::nonNull)
        	    .collect(Collectors.toList());

        	List<String> bccEmails = recipientLinks.stream()
        	    .filter(link -> "BCC".equalsIgnoreCase(link.getType()))
        	    .map(this::resolveEmail)
        	    .filter(Objects::nonNull)
        	    .collect(Collectors.toList());
        
        dto.setCc(String.join(",", ccEmails));
        dto.setBcc(String.join(",", bccEmails));
        dto.setToEmail(String.join(",", toEmails));
        return dto;
    }

    private String resolveEmail(ContactListEmailAnalytics link) {
        if (link.getContact() != null && link.getContact().getEmail() != null) {
            return link.getContact().getEmail();
        } 
        return link.getNewEmail();
    }


    public List<EmailReplyDto> getReplyChain(String messageId) {
        List<EmailAnalytics> replies = repo.findByInReplyTo(messageId);
        List<EmailReplyDto> replyDtos = new ArrayList<>();
        for (EmailAnalytics reply : replies) {
            EmailReplyDto dto = toDto(reply);
            dto.setReplies(getReplyChain(reply.getMessageId()));
            replyDtos.add(dto);
        }
        return replyDtos;
    }

    public MailConfig getMailConfigForUser(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return mailConfigRepository.findByCreatedBy(user)
                .orElseThrow(() -> new RuntimeException("Mail configuration not found for user: " + user.getId()));
    }

}
