package mephi.services.password;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public class PasswordService {
    private static final int COST_FACTOR = 12;
    private static PasswordService instance;

    public static String getPasswordHash(String password) {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = BCrypt.withDefaults().hash(PasswordService.COST_FACTOR, passwordBytes);

        return new String(hashedBytes, StandardCharsets.UTF_8);
    }

    public static boolean verifyPasswordHash(String password, String passwordHash) {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] passwordHashBytes = passwordHash.getBytes(StandardCharsets.UTF_8);

        return BCrypt.verifyer().verify(passwordBytes, passwordHashBytes).verified;
    }
}
