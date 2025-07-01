package com.example.owlpost_2_0.Email;
import java.util.Properties;
import java.util.Random;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {
    private static final String EMAIL_FROM = "rakibcdt1958@gmail.com";
//    private static final String EMAIL_TO = "witchergeralt1969@gmail.com";
    private static final String APP_PASSWORD = "alqt chng ngny jqxg";

    public static int sendEmail(String EMAIL_TO) throws Exception {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_TO));
        message.setSubject("Email subject");
        int randomNum = new Random().nextInt(1000000);
        message.setText("Reset password using " + randomNum);
        Transport.send(message);
        return randomNum;
    }

    private static Session getEmailSession() {
        return Session.getInstance(getGmailProperties(), new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });
    }

    private static Properties getGmailProperties() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return prop;
    }

}// alqt chng ngny jqxg