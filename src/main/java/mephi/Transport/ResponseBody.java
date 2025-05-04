package mephi.Transport;

public record ResponseBody<T>(
    ResponseBodyStatus status,
    T data,
    String reason
) {}
