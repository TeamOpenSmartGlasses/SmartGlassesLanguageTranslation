package com.teamopensmartglasses.translate.events;

public class ChangeSourceLanguageEvent {
    public final String languageCode;
    public ChangeSourceLanguageEvent(String lang){
        this.languageCode = lang;
    }
}
