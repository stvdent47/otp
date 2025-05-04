package mephi.controllers.admin;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import mephi.Transport.HttpTransport;
import mephi.services.ServiceResult;
import mephi.entities.otp.OtpConfigDto;
import mephi.entities.user.User;
import mephi.services.otp.OtpService;
import mephi.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AdminControllerImpl implements AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminControllerImpl.class);

    private final UserService userService;
    private final OtpService otpService;

    public AdminControllerImpl(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @Override
    public void getUsers(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("get")) {
            return;
        }

        logger.info("/admin/get-users");

        List<User> users = this.userService.getAll();

        HttpTransport.sendResponse(
            exchange,
            200,
            HttpTransport.getResponseSuccess(users)
        );
    }

    @Override
    public void deleteUser(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("delete")) {
            return;
        }

        logger.info("/admin/delete-user");

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String[] pathSplit = path.split("/");

        ServiceResult result = this.userService.delete(pathSplit[pathSplit.length - 1]);

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
            200,
            HttpTransport.getResponseEmptySuccess()
        );
    }

    @Override
    public void changeOtpConfig(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("post")) {
            return;
        }

        logger.info("/admin/change-otp-config");

        OtpConfigDto otpConfigData;
        try (InputStream inputStream = exchange.getRequestBody()) {
            otpConfigData = new Gson().fromJson(
                new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                OtpConfigDto.class
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
            otpConfigData.length() == 0 ||
            otpConfigData.expiration() == 0
        ) {
            HttpTransport.sendResponse(
                exchange,
                400,
                HttpTransport.getResponseError("invalid data")
            );
            return;
        }

        boolean result = this.otpService.updateConfig(otpConfigData);

        if (!result) {
            HttpTransport.sendResponse(
                exchange,
                400,
                HttpTransport.getResponseError("something went wrong")
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
