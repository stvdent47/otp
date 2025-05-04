package mephi.controllers.user;

import com.sun.net.httpserver.HttpExchange;

public interface UserController {
    void register(HttpExchange exchange);
    void login(HttpExchange exchange);
    void generateOtp(HttpExchange exchange);
    void validateOtp(HttpExchange exchange);
}
