package mephi.services.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class EmailNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationService() {
        Properties config = this.loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        this.session = Session.getInstance(config, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties loadConfig() {
        try {
            Properties properties = new Properties();
            properties.load(
                EmailNotificationService.class
                    .getClassLoader()
                    .getResourceAsStream("email.properties"));

            return properties;
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(this.session);
            message.setFrom(new InternetAddress(this.fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP code");
            message.setText(String.format("Your otp code is %s", code));

            Transport.send(message);
        }
        catch (MessagingException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
