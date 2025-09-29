package com.xtremand.email.service;

import java.util.Properties;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

import com.xtremand.config.repository.MailConfigRepository;
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.domain.entity.MailConfig;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.domain.util.XtremandUtil;
import com.xtremand.email.repository.EmailAnalyticsRepository;

@Service
public class EmailInboxPollService {

	@Autowired
	private EmailAnalyticsRepository repo;
	
	@Autowired
	private XtremandUtil xtremandUtil;
	
	@Autowired
	private MailConfigRepository mailConfigRepository;

	/**
	 * Poll inbox using either app password or OAuth2 token.
	 *
	 * @param emailUser       Email address
	 * @param passwordOrToken Either Gmail App Password or OAuth2 access token
	 * @param useOAuth        true if OAuth2 token, false if app password
	 * @throws Exception
	 */
	
	
	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void scheduledPollAndStoreInbox() {
		if (xtremandUtil.isProfileActive()) {
			try {
				List<MailConfig> configs = mailConfigRepository.findAll();
				for (MailConfig config : configs) {
					 boolean useOAuth;
					 String passwordOrToken;
				        if (config.getConfigType() == EmailConfigType.OAUTH_CONFIG && config.getOauthAccessToken() != null
				                && !config.getOauthAccessToken().isEmpty()) {
				            passwordOrToken = config.getOauthAccessToken();
				            useOAuth = true;
				        } else {
				            passwordOrToken = decryptPassword(config.getPassword());
				            useOAuth = false;
				        }
				pollAndStoreInbox(config.getEmail(), passwordOrToken, useOAuth);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	   public String decryptPassword(String encoded) {
	        if (encoded == null || encoded.isEmpty()) {
	            return null;
	        }
	        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
	        return new String(decodedBytes, StandardCharsets.UTF_8);
	    }
	
	public void pollAndStoreInbox(String emailUser, String passwordOrToken, boolean useOAuth) throws Exception {
		Properties props = new Properties();
		props.put("mail.store.protocol", "imaps");
		props.put("mail.imaps.host", "imap.gmail.com");
		props.put("mail.imaps.port", "993");
		props.put("mail.imaps.ssl.enable", "true");
		props.put("mail.imaps.starttls.enable", "true");
		props.put("mail.imaps.ssl.trust", "*");
		if (useOAuth) {
			props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
		}
		Session session = Session.getInstance(props);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", emailUser, passwordOrToken);
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flag.SEEN), false);
		Message[] unreadMessages = inbox.search(unseenFlagTerm);
		Arrays.sort(unreadMessages, Comparator.comparing((Message m) -> {
			try {
				return m.getSentDate();
			} catch (MessagingException e) {
				return null;
			}
		}).reversed());
		for (Message m : unreadMessages) {
			Address[] fromAddresses = m.getFrom();
			String from = "";
			if (fromAddresses != null && fromAddresses.length > 0) {
				if (fromAddresses[0] instanceof InternetAddress internetAddress) {
					from = internetAddress.getAddress();
				} else {
					from = fromAddresses[0].toString();
				}
			}
			String to = InternetAddress.toString(m.getRecipients(Message.RecipientType.TO));
			String subject = m.getSubject();
			String bodyText = extractBody(m);
			String msgId = ((MimeMessage) m).getMessageID();
			String[] inReplyToHeaders = m.getHeader("In-Reply-To");
			String inReplyTo = (inReplyToHeaders != null && inReplyToHeaders.length > 0) ? inReplyToHeaders[0] : null;
			if (repo.findByMessageId(msgId).isPresent())
				continue;
			if (inReplyTo == null || repo.findByMessageId(inReplyTo).isEmpty()) {
				continue;
			}
			EmailAnalytics ea = new EmailAnalytics();
			ea.setFromEmail(from);
			ea.setSubject(subject);
			ea.setBody(bodyText);
			ea.setMessageId(msgId);
			ea.setInReplyTo(inReplyTo);
			ea.setStarred(false);
			ea.setDeleted(false);
			ea.setIncoming(true);
			ea.setSentAt(m.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			repo.save(ea);
		}
		inbox.close(false);
		store.close();
	}

	private String extractBody(Message message) throws Exception {
		Object content = message.getContent();
		if (content instanceof String string) {
			return string;
		} else if (content instanceof Multipart multipart) {
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart part = multipart.getBodyPart(i);
				String disposition = part.getDisposition();
				if (disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) {
					Object partContent = part.getContent();
					if (partContent instanceof String stringPart) {
						return stringPart;
					}
				}
			}
			BodyPart part = multipart.getBodyPart(0);
			Object partContent = part.getContent();
			if (partContent instanceof String stringPart) {
				return stringPart;
			}
		}
		return "";
	}
}
