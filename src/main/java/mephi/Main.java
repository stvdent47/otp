package mephi;

import com.sun.net.httpserver.HttpServer;
import mephi.controllers.admin.AdminController;
import mephi.controllers.admin.AdminControllerImpl;
import mephi.controllers.user.UserControllerImpl;
import mephi.entities.user.UserRole;
import mephi.middlewares.Auth;
import mephi.repository.otpConfig.OtpConfigRepository;
import mephi.repository.otp.OtpRepositoryImpl;
import mephi.repository.otp.OtpRepository;
import mephi.repository.otpConfig.OtpConfigRepositoryImpl;
import mephi.repository.user.UserRepository;
import mephi.repository.user.UserRepositoryImpl;
import mephi.services.otp.OtpExpirationScheduler;
import mephi.services.otp.OtpService;
import mephi.services.otp.OtpServiceImpl;
import mephi.services.user.UserService;
import mephi.services.user.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Properties loadConfig() {
        try {
            Properties properties = new Properties();
            properties.load(
                Main.class
                    .getClassLoader()
                    .getResourceAsStream("app.properties")
            );

            return properties;
        }
        catch (IOException e) {
            logger.error("failed to load properties config");
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Properties config = loadConfig();

        final String postgresUrl = config.getProperty("postgres.url", "jdbc:postgresql://localhost");
        final String postgresPort = config.getProperty("postgres.port", "5432");
        final String postgresUsername = config.getProperty("postgres.username", "postgres");
        final String postgresPassword = config.getProperty("postgres.password", "1qaz2wsx");
        final String dbURL = String.format(
            "%s:%s/otp?user=%s&password=%s",
            postgresUrl,
            postgresPort,
            postgresUsername,
            postgresPassword
        );

        final int PORT = Integer.parseInt(config.getProperty("server.port", "8081"));

        try {
            final Connection connection = DriverManager.getConnection(dbURL);
            logger.info("successfully connected to DB");

            UserRepository userRepository = new UserRepositoryImpl(connection);
            OtpConfigRepository otpConfigRepository = new OtpConfigRepositoryImpl(connection);
            OtpRepository otpRepository = new OtpRepositoryImpl(connection);

            UserService userService = new UserServiceImpl(userRepository);
            OtpService otpService = new OtpServiceImpl(otpConfigRepository, otpRepository);

            UserControllerImpl userController = new UserControllerImpl(userService, otpService);
            AdminController adminController = new AdminControllerImpl(userService, otpService);

            final HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

            // todo: create router?
            // open
            httpServer.createContext(
                "/users/register",
                userController::register
            );
            httpServer.createContext(
                "/users/login",
                userController::login
            );
            // user
            httpServer.createContext(
                "/users/generate-otp",
                new Auth(userController::generateOtp, UserRole.user)
            );
            httpServer.createContext(
                "/users/validate-otp",
                new Auth(userController::validateOtp, UserRole.user)
            );
            // admin
            httpServer.createContext(
                "/admin/change-otp-config",
                new Auth(adminController::changeOtpConfig, UserRole.admin)
            );
            httpServer.createContext(
                "/admin/get-users",
                new Auth(adminController::getUsers, UserRole.admin)
            );
            httpServer.createContext(
                "/admin/delete-user",
                new Auth(adminController::deleteUser, UserRole.admin)
            );

            httpServer.setExecutor(null);
            httpServer.start();

            logger.info("server started at port: {}", PORT);

            OtpExpirationScheduler otpExpirationScheduler = new OtpExpirationScheduler(otpService, 5);
            otpExpirationScheduler.start();

            Runtime.getRuntime().addShutdownHook(new Thread(otpExpirationScheduler::stop));
        }
        catch (SQLException | IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}