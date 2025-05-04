package mephi.entities.user;

public record RegisterDto(
    String username,
    String password,
    String role
) {}
