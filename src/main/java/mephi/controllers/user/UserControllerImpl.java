package mephi.controllers.user;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import mephi.Transport.HttpTransport;
import mephi.Transport.ResponseJwt;
import mephi.services.ServiceResult;
import mephi.entities.otp.GenerateOtpDto;
import mephi.entities.user.LoginDto;
import mephi.entities.user.RegisterDto;
import mephi.entities.otp.ValidateOtpDto;
import mephi.services.jwt.JwtService;
import mephi.services.notification.*;
import mephi.services.otp.OtpService;
import mephi.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UserControllerImpl implements UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserControllerImpl.class);

    private final Gson gson = new Gson();
    private final UserService userService;
    private final OtpService otpService;

    private final NotificationService emailNotificationService;
    private final NotificationService fileNotificationService;
    private final NotificationService smsNotificationService;
    private final NotificationService telegramNotificationService;

    public UserControllerImpl(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;

        this.emailNotificationService = new EmailNotificationService();
        this.fileNotificationService = new FileNotificationService();
        this.smsNotificationService = new SmsNotificationService();
        this.telegramNotificationService = new TelegramNotificationService();
    }

    @Override
    public void register(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("post")) {
            return;
        }

        logger.info("/users/register");

        try {
            RegisterDto registerData;
            try (InputStream inputStream = exchange.getRequestBody()) {
                registerData = this.gson.fromJson(
                    new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                    RegisterDto.class
                );
            }

            if (
                registerData.username() == null ||
                registerData.password() == null ||
                registerData.role() == null
            ) {
                HttpTransport.sendResponse(
                    exchange,
                    400,
                    HttpTransport.getResponseError("invalid credentials")
                );
                return;
            }

            ServiceResult result = this.userService.register(registerData);

            if (!result.status()) {
                HttpTransport.sendResponse(
                    exchange,
                    400,
                    HttpTransport.getResponseError(result.message())
                );
                return;
            }

            HttpTransport.sendResponse(
                exchange,
                201,
                HttpTransport.getResponseEmptySuccess()
            );
        }
        catch (IOException e) {
            logger.error(e.getMessage());

            HttpTransport.sendResponse(
                exchange,
                500,
                HttpTransport.getResponseError("something went wrong")
            );
        }
    }

    @Override
    public void login(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("post")) {
            return;
        }

        logger.info("/users/login");

        LoginDto loginData;
        try (InputStream inputStream = exchange.getRequestBody()) {
            loginData = this.gson.fromJson(
                new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                LoginDto.class
            );
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            HttpTransport.sendResponse(
                exchange,
                500,
                HttpTransport.getResponseError("something went wrong")
            );
            return;
        }

        ServiceResult result = this.userService.login(loginData);

        if (!result.status()) {
            logger.error(result.message());
            HttpTransport.sendResponse(
                exchange,
                400,
                HttpTransport.getResponseError(result.message())
            );
            return;
        }

        HttpTransport.sendResponse(
            exchange,
            200,
            HttpTransport.getResponseSuccess(new ResponseJwt(result.message()))
        );
    }

    private NotificationService getNotificationService(NotificationChannel notificationChannel) {
        Map<NotificationChannel, NotificationService> notificationServiceMap = new HashMap<>();

        notificationServiceMap.put(NotificationChannel.email, this.emailNotificationService);
        notificationServiceMap.put(NotificationChannel.file, this.fileNotificationService);
        notificationServiceMap.put(NotificationChannel.sms, this.smsNotificationService);
        notificationServiceMap.put(NotificationChannel.telegram, this.telegramNotificationService);

        return notificationServiceMap.get(notificationChannel);
    }

    @Override
    public void generateOtp(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("post")) {
            return;
        }

        logger.info("/users/generate-otp");

        GenerateOtpDto generateOtpDto;
        try (InputStream inputStream = exchange.getRequestBody()) {
            generateOtpDto = this.gson.fromJson(
                new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                GenerateOtpDto.class
            );
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            HttpTransport.sendResponse(
                exchange,
                500,
                HttpTransport.getResponseError("something went wrong")
            );
            return;
        }

        if (
            generateOtpDto.channel() == null ||
            generateOtpDto.destination() == null
        ) {
            HttpTransport.sendResponse(
                exchange,
                400,
                HttpTransport.getResponseError("invalid data")
            );
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String jwt = authHeader.substring(7);
        String userId = JwtService.validateJwt(jwt).getSubject();

        String otpGenerated = this.otpService.generate(userId);

        NotificationService notificationService = this.getNotificationService(generateOtpDto.channel());
        notificationService.sendCode(generateOtpDto.destination(), otpGenerated);

        HttpTransport.sendResponse(
            exchange,
            200,
            HttpTransport.getResponseEmptySuccess()
        );
    }

    @Override
    public void validateOtp(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("post")) {
            return;
        }

        logger.info("/users/validate-otp");

        ValidateOtpDto validateOtpData;
        try (InputStream inputStream = exchange.getRequestBody()) {
            validateOtpData = this.gson.fromJson(
                new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                ValidateOtpDto.class
            );
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            HttpTransport.sendResponse(
                exchange,
                500,
                HttpTransport.getResponseError("something went wrong")
            );
            return;
        }

        if (
            validateOtpData.userId() == null ||
            validateOtpData.otp() == null
        ) {
            HttpTransport.sendResponse(
                exchange,
                400,
                HttpTransport.getResponseError("invalid data")
            );
            return;
        }

        boolean result = this.otpService.validate(validateOtpData);

        if (!result) {
            HttpTransport.sendResponse(
                exchange,
                400,
                HttpTransport.getResponseError("invalid otp")
            );
            return;
        }

        HttpTransport.sendResponse(
            exchange,
            200,
            HttpTransport.getResponseEmptySuccess()
        );
    }
}
