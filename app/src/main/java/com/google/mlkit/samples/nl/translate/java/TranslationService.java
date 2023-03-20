package com.google.mlkit.samples.nl.translate.java;

import android.util.Log;

import com.google.mlkit.samples.nl.translate.R;
import com.google.mlkit.samples.nl.translate.java.events.RequestTranslateMessageEvent;
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
    SimplifiedTranslateViewModel viewModel;
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

        //Create SGMLib instance with context: this
        sgmLib = new SGMLib(this);

        //Define command with a UUID
        UUID commandUUID = UUID.fromString("5b824bb6-d3b3-417d-8c74-3b103efb403f");
        SGMCommand command = new SGMCommand("Translate", commandUUID, new String[]{"Translate"}, "A Translation App");

        //Register the command
        sgmLib.registerCommand(command, this::translateCommandCallback);

        //Subscribe to transcription stream
        sgmLib.subscribe(DataStreamType.TRANSCRIPTION_STREAM, this::processTranscriptionCallback);

        Log.d(TAG, "TRANSLATION SERVICE STARTED");
        sgmLib.sendReferenceCard("Success", "Translation Service started");

        initializeTranslationStuff();
    }

    public void initializeTranslationStuff(){
        EventBus.getDefault().register(this);
        viewModel = new SimplifiedTranslateViewModel();
        viewModel.sourceLang.setValue(new SimplifiedTranslateViewModel.Language("en"));
        viewModel.targetLang.setValue(new SimplifiedTranslateViewModel.Language("es"));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void processTranscriptionCallback(String transcript, long timestamp, boolean isFinal){
        Log.d(TAG, "PROCESS TRANSCRIPTION CALLBACK. IS IT FINAL? " + isFinal + " " + transcript);
        if(isFinal) translateText(transcript);
    }
    public void translateCommandCallback(String args, long commandTriggeredTime){
        Log.d("TAG","Translation callback called");
        translateText("Translate this text!");
    }

    public void translateText(String text){
        viewModel.sourceText.setValue(text);
        viewModel.translate();
    }

    @Subscribe
    public void onTranslateSuccess(TranslateSuccessEvent event){
        Log.d(TAG, "Success! SUCCESS");
        if(sgmLib == null) return;

        if(newScreen) {
            newScreen = false;
            sgmLib.startScrollingText("Translate2");
        }
        sgmLib.pushScrollingText(event.message);
    }
}
