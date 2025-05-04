package mephi.services.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);

    @Override
    public void sendCode(String fileName, String code) {

        try {
            Path path = Paths.get(fileName);
            Files.write(path, code.getBytes(), StandardOpenOption.CREATE);
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
