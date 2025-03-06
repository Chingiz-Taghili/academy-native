package com.academy.payload;

public class MessageResponse extends ApiResponse {

    private final String message;

    public MessageResponse(String message) {
        super(true);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}