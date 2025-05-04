package mephi.entities.otp;

import mephi.services.notification.NotificationChannel;

public record GenerateOtpDto(
    NotificationChannel channel,
    String destination
) {}
