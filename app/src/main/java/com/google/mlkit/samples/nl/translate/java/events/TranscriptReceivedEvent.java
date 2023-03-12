package com.google.mlkit.samples.nl.translate.java.events;

public class TranscriptReceivedEvent {
    public final String message;
    public TranscriptReceivedEvent(String message){
        this.message = message;
    }
}
