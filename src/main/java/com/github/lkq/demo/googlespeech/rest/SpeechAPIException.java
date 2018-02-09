package com.github.lkq.demo.googlespeech.rest;

public class SpeechAPIException extends Throwable {
    private final int status;
    private final String message;

    public SpeechAPIException(int status, String message) {

        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
