package com.teamopensmartglasses.translate.events;

public class ChangeTargetLanguageEvent {
    public final String languageCode;
    public ChangeTargetLanguageEvent(String lang){
        this.languageCode = lang;
    }
}
