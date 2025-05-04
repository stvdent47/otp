package mephi.entities.user;

public record User(
    String id,
    UserRole role,
    String username,
    String password
) {}
