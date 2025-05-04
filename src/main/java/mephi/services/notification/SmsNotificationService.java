package mephi.services.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpp.*;
import org.smpp.pdu.*;

import java.io.IOException;
import java.util.Properties;

public class SmsNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties properties = this.loadConfig();
        this.host = properties.getProperty("smpp.host");
        this.port = Integer.parseInt(properties.getProperty("smpp.port"));
        this.systemId = properties.getProperty("smpp.system_id");
        this.password = properties.getProperty("smpp.password");
        this.systemType = properties.getProperty("smpp.system_type");
        this.sourceAddress = properties.getProperty("smpp.source_addr");
    }

    private Properties loadConfig() {
        try {
            Properties properties = new Properties();
            properties.load(
                SmsNotificationService.class
                    .getClassLoader()
                    .getResourceAsStream("sms.properties")
            );

            return properties;
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendCode(String destination, String code) {
        Connection connection;
        Session session;

        try {
            connection = new TCPIPConnection(this.host, this.port);
            session = new Session(connection);

            BindTransmitter bindTransmitter = new BindTransmitter();
            bindTransmitter.setSystemId(this.systemId);
            bindTransmitter.setPassword(this.password);
            bindTransmitter.setSystemType(this.systemType);
            bindTransmitter.setInterfaceVersion((byte) 0x34);
            bindTransmitter.setAddressRange(this.sourceAddress);

            BindResponse bindResponse = session.bind(bindTransmitter);
            if (bindResponse.getCommandStatus() != 0) {
                throw new Exception("Bind failed: " + bindResponse.getCommandStatus());
            }

            SubmitSM submitSM = new SubmitSM();
            submitSM.setSourceAddr(this.sourceAddress);
            submitSM.setDestAddr(destination);
            submitSM.setShortMessage(String.format("Your code: %s", code));

            session.submit(submitSM);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
