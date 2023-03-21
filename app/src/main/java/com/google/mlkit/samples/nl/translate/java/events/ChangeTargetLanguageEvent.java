package com.google.mlkit.samples.nl.translate.java.events;

import com.google.mlkit.samples.nl.translate.java.TranslationBackend;

public class ChangeTargetLanguageEvent {
    public final String languageCode;
    public ChangeTargetLanguageEvent(String lang){
        this.languageCode = lang;
    }
}
