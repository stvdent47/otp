package mephi.entities.otp;

public record Otp(
    String id,
    String value,
    long createdAt,
    OtpStatus status,
    String userId,
    String operationId
) {}
