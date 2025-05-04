package mephi.entities.otp;

public record OtpConfigDto(
    int length,
    long expiration
) {}
