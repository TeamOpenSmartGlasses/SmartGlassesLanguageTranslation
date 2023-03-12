package com.google.mlkit.samples.nl.translate.java;

import static com.google.mlkit.samples.nl.translate.java.Globals.sgmLib;

import android.util.Log;

import com.google.mlkit.samples.nl.translate.R;
import com.google.mlkit.samples.nl.translate.java.events.RequestTranslateMessageEvent;
import com.google.mlkit.samples.nl.translate.java.events.TranscriptReceivedEvent;
import com.google.mlkit.samples.nl.translate.java.events.TranslateSuccessEvent;
import com.teamopensmartglasses.sgmlib.DataStreamType;
import com.teamopensmartglasses.sgmlib.SGMCommand;
import com.teamopensmartglasses.sgmlib.SGMLib;
import com.teamopensmartglasses.sgmlib.SmartGlassesAndroidService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.UUID;

public class TranslationService extends SmartGlassesAndroidService {
    public final String TAG = "TranslateApp_TranslateAppService";

    //our instance of the SGM library
    public SGMLib sgmLib;
    public boolean newScreen = true;
    public String previousTranslation = "";

    public TranslationService(){
        super(MainActivity.class,
                "translation_app",
                1001,
                "Translation",
                "Translation app for smartglasses", R.drawable.common_google_signin_btn_icon_light_normal);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sgmLib = new SGMLib(getApplicationContext());

        SGMCommand command = new SGMCommand("Translate", UUID.randomUUID(), new String[]{"Translate"}, "A Translation App");
        sgmLib.registerCommand(command, this::translateCommandCallback);
        sgmLib.subscribe(DataStreamType.TRANSCRIPTION_STREAM, this::processTranscriptionCallback);
        EventBus.getDefault().register(this);

        Log.d(TAG, "TRANSLATION SERVICE STARTED");
    }

    public void processTranscriptionCallback(String transcript, long timestamp, boolean isFinal){
        Log.d(TAG, "PROCESS TRANSCRIPTION CALLBACK. IS IT FINAL? " + isFinal + " " + transcript);
        if(isFinal) translateText(transcript);
    }
    public void translateCommandCallback(){
        Log.d("TAG","Translation callback called");
        translateText("Translate this text!");
    }

    public void translateText(String text){
        EventBus.getDefault().post(new RequestTranslateMessageEvent(text));
    }

    @Subscribe
    public void onTranslateSuccess(TranslateSuccessEvent event){
        Log.d(TAG, "Success! SHITFUCK");
        if(sgmLib == null) return;

        if(newScreen) {
            newScreen = false;
            sgmLib.startScrollingText("Translate2");
        }
        sgmLib.pushScrollingText(event.message);
    }
}
