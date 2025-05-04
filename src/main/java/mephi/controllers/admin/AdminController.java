package mephi.controllers.admin;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface AdminController {
    void getUsers(HttpExchange exchange) throws IOException;
    void deleteUser(HttpExchange exchange) throws IOException;
    void changeOtpConfig(HttpExchange exchange) throws IOException;
}
