package bookbridge.service;

public record Result<T>(boolean success, T data, String message) {

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, "Operation successful.");
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message);
    }
}
