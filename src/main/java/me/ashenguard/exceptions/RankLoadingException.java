package me.ashenguard.exceptions;

public class RankLoadingException extends RuntimeException {
    public RankLoadingException() {
        super();
    }
    public RankLoadingException(String message) {
        super(message);
    }
    public RankLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
    public RankLoadingException(Throwable cause) {
        super(cause);
    }
}