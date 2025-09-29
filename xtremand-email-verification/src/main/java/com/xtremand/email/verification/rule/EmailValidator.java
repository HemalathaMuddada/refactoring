package com.xtremand.email.verification.rule;

import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class EmailValidator {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	public boolean isValidSyntax(String email) {
		return EMAIL_PATTERN.matcher(email).matches();
	}

	public String extractDomain(String email) {
		int at = email.lastIndexOf('@');
		return at != -1 ? email.substring(at + 1) : "";
	}

	public String domainHasMxOrARecord(String domain) {
		Hashtable<String, String> env = new Hashtable<>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		try {
			String mxRecord = null;
			DirContext ctx = new InitialDirContext(env);
			Attributes attrs = ctx.getAttributes(domain, new String[] { "MX" });
			Attribute attr = attrs.get("MX");
			if (attrs != null && attrs.get("MX") != null) {
				mxRecord = ((String) attr.get(0)).split(" ")[1];
			}
			attrs = ctx.getAttributes(domain, new String[] { "A" });
			if (attrs == null || attrs.get("A") == null) {
				return null;
			}
			return mxRecord;
		} catch (NamingException e) {
			return null;
		}
	}

	public static String maskEmail(String email) {
		int at = email.indexOf('@');
		if (at <= 1) {
			return "***@" + (at == -1 ? "" : email.substring(at + 1));
		}
		return email.charAt(0) + "***" + email.substring(at);
	}
}
