package mephi.entities.otp;

public record ValidateOtpDto(
    String userId,
    String otp
) {}
