package com.google.mlkit.samples.nl.translate.java.events;

public class NeedDownloadLanguageEvent {
    public final String languageCode;
    public final boolean isComplete;
    public NeedDownloadLanguageEvent(String lang, boolean isComplete){
        this.languageCode = lang;
        this.isComplete = isComplete;
    }
}
