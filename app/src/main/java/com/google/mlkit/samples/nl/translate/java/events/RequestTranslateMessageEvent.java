package com.google.mlkit.samples.nl.translate.java.events;

public class RequestTranslateMessageEvent {
    public final String message;
    public RequestTranslateMessageEvent(String message){
        this.message = message;
    }
}
