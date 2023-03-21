package com.google.mlkit.samples.nl.translate.java.events;

import com.google.mlkit.samples.nl.translate.java.TranslationBackend;

public class ChangeSourceLanguageEvent {
    public final String languageCode;
    public ChangeSourceLanguageEvent(String lang){
        this.languageCode = lang;
    }
}
