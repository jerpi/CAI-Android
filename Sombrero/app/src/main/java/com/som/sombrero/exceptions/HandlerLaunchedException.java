package com.som.sombrero.exceptions;

public class HandlerLaunchedException extends Exception {

    public HandlerLaunchedException() {
    }

    public HandlerLaunchedException(String message) {
        super(message);
    }

    public HandlerLaunchedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerLaunchedException(Throwable cause) {
        super(cause);
    }
}
