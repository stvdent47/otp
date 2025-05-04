package mephi.middlewares;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import mephi.Transport.HttpTransport;
import mephi.entities.user.UserRole;
import mephi.services.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Auth implements HttpHandler {
    private final Logger logger = LoggerFactory.getLogger(Auth.class);

    private final HttpHandler wrappedHandler;
    private final UserRole authRole;

    public Auth(HttpHandler wrappedHandler, UserRole authRole) {
        this.wrappedHandler = wrappedHandler;
        this.authRole = authRole;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                HttpTransport.sendResponse(
                    exchange,
                    401,
                    HttpTransport.getResponseError("invalid Authorization")
                );
                return;
            }

            String jwt = authHeader.substring(7);

            Claims claims = JwtService.validateJwt(jwt);

            UserRole role = UserRole.valueOf(
                claims
                    .get("roles")
                    .toString()
                    .replace("[", "")
                    .replace("]", "")
            );

            if (role != this.authRole) {
                HttpTransport.sendResponse(
                    exchange,
                    401,
                    HttpTransport.getResponseError("unauthorised")
                );
                return;
            }

            this.wrappedHandler.handle(exchange);
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
