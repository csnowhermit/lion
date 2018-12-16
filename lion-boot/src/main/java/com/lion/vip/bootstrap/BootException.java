package com.lion.vip.bootstrap;

public class BootException extends RuntimeException {

    public BootException() {
    }

    public BootException(String message) {
        super(message);
    }

    public BootException(String message, Throwable cause) {
        super(message, cause);
    }
}
