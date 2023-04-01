package com.teamopensmartglasses.translate;

import android.util.Log;

import com.google.mlkit.samples.nl.translate.R;
import com.teamopensmartglasses.translate.events.ChangeSourceLanguageEvent;
import com.teamopensmartglasses.translate.events.ChangeTargetLanguageEvent;
import com.teamopensmartglasses.translate.events.NeedDownloadLanguageEvent;
import com.teamopensmartglasses.translate.events.TranslateSuccessEvent;
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
    TranslationBackend translationBackend;
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
        SGMCommand command = new SGMCommand("Translate", commandUUID, new String[]{"Translate"}, "Language translation app for smart glasses");

        //Register the command
        sgmLib.registerCommand(command, this::translateCommandCallback);

        Log.d(TAG, "TRANSLATION SERVICE STARTED");
        //sgmLib.sendReferenceCard("Success", "Translation Service started");

        initializeTranslationStuff();
    }

    public void initializeTranslationStuff(){
        EventBus.getDefault().register(this);
        translationBackend = new TranslationBackend();
        translationBackend.sourceLang.setValue(new TranslationBackend.Language("en"));
        translationBackend.targetLang.setValue(new TranslationBackend.Language("es"));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        sgmLib.deinit();
        EventBus.getDefault().unregister(this);
    }

    public void processTranscriptionCallback(String transcript, long timestamp, boolean isFinal){
        Log.d(TAG, "PROCESS TRANSCRIPTION CALLBACK. IS IT FINAL? " + isFinal + " " + transcript);
        if(isFinal) translateText(transcript);
    }
    public void translateCommandCallback(String args, long commandTriggeredTime){
        Log.d("TAG","Translation callback called");

        //StartScrollingText lets us aquire SGM's mode
        sgmLib.startScrollingText("Translation: ");

        //Subscribe to transcription stream
        sgmLib.subscribe(DataStreamType.TRANSCRIPTION_ENGLISH_STREAM, this::processTranscriptionCallback);
    }

    public void translateText(String text){
        translationBackend.sourceText.setValue(text);
        translationBackend.translate();
    }

    @Subscribe
    public void onTranslateSuccess(TranslateSuccessEvent event){

        if(sgmLib == null) return;

        if(newScreen) {
            newScreen = false;
            //String sourceCode = translationBackend.sourceLang.getValue().getCode();
            //String targetCode = translationBackend.targetLang.getValue().getCode();
            //sgmLib.startScrollingText("Translation: ");
        }
        sgmLib.pushScrollingText(event.message);
    }

    @Subscribe
    public void onChangeSourceLang(ChangeSourceLanguageEvent event){
        translationBackend.sourceLang.setValue(new TranslationBackend.Language(event.languageCode));
    }
    @Subscribe
    public void onChangeTargetLang(ChangeTargetLanguageEvent event) {
        translationBackend.targetLang.setValue(new TranslationBackend.Language(event.languageCode));
    }

    @Subscribe
    public void displayDownloadingLanguage(NeedDownloadLanguageEvent event){
        if(event.isComplete)
            sgmLib.sendReferenceCard("Translation", "Language download complete!");
        else
            sgmLib.sendReferenceCard("Translation", "Download required language...");
    }
}
