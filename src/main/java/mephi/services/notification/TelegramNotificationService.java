package mephi.services.notification;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final String telegramApiUrl;
    private final String chatId;

    public TelegramNotificationService() {
        Properties config = this.loadConfig();
        this.telegramApiUrl = config.getProperty("telegramApiUrl");
        this.chatId = config.getProperty("chatId");
    }

    private Properties loadConfig() {
        try {
            Properties properties = new Properties();
            properties.load(
                TelegramNotificationService.class
                    .getClassLoader()
                    .getResourceAsStream("telegram.properties")
            );

            return properties;
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void sendTelegramRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.error("Telegram API error. Status code: {}", statusCode);
                } else {
                    logger.info("Telegram message sent successfully");
                }
            }
        }
        catch (IOException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage());
        }
    }

    @Override
    public void sendCode(String destination, String code) {
        String message = String.format(
            "%s, your confirmation code is: %s",
            destination,
            code
        );

        String url = String.format(
            "%s?chat_id=%s&text=%s",
            telegramApiUrl,
            chatId,
            this.urlEncode(message)
        );

        sendTelegramRequest(url);
    }
}
