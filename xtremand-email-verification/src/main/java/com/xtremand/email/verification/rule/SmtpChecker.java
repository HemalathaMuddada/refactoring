package com.xtremand.email.verification.rule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SmtpChecker {

	public EmailValidationStatus ping(String email, String mailServer) {
		System.out.println(465);
		try (Socket socket = new Socket(mailServer, 465);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
			String response = reader.readLine();
			if (response == null || !response.startsWith("220"))
				return EmailValidationStatus.SMTP_PING_FAILED;
			write(writer, "HELO example.com");
			response = reader.readLine();
			write(writer, "MAIL FROM:<verify@example.com>");
			response = reader.readLine();
			write(writer, "RCPT TO:<" + email + ">");
			response = reader.readLine();
			write(writer, "QUIT");
			if (response != null && response.startsWith("250"))
				return EmailValidationStatus.VALID;
			if (response != null && response.startsWith("550"))
				return EmailValidationStatus.MAILBOX_NOT_FOUND;
			return EmailValidationStatus.SMTP_PING_FAILED;
		} catch (IOException e) {
			e.printStackTrace();
			return EmailValidationStatus.SMTP_PING_FAILED;
		}
	}

	private void write(BufferedWriter writer, String cmd) throws IOException {
		writer.write(cmd + "\r\n");
		writer.flush();
	}
}
