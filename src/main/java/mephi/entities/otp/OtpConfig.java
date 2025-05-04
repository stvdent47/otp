package mephi.entities.otp;

public record OtpConfig(
    String id,
    int length,
    long expiration
) {}
