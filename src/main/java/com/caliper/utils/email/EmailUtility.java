package com.caliper.utils.email;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.caliper.utils.exception.customException.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailUtility {

	String destinationEmailId = "";
	String subject = "";
	String mailBody = "";
	
	public void sendEmail(boolean isHtml) throws Exception {
		String to = destinationEmailId;
		String from = "adtech@interactiveavenues.net";
		String host = "email-smtp.ap-south-1.amazonaws.com";
		Properties properties = System.getProperties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.required", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.put("mail.smtp.ssl.trust", host);
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		String username = "AKIASMHHLCQYEVMXOVSA";
		String password = "BDaB0Md5AM6MmZUYB4kcqjyqbEw16nOKlCIoNgKDKnfP";
		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			if(isHtml) {
				message.setContent(mailBody,"text/html");
			}else {
				message.setText(mailBody);
			}
			Transport.send(message);
			System.out.println("Email sent without attachment.");
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static String generatePasswordFromEmail(String email) {

	    if (email == null || !email.contains("@")) {
	        throw new InvalidRequestException("Invalid email: " + email);
	    }

	    String prefix = email.substring(0, email.indexOf("@"));

	    // Capitalize first letter
	    prefix = prefix.substring(0, 1).toUpperCase() + prefix.substring(1);

	    // Generate random 3-digit number (100–999)
	    int randomNumber = ThreadLocalRandom.current().nextInt(100, 1000);

	    return prefix + "@#" + randomNumber;
	}
	
}
