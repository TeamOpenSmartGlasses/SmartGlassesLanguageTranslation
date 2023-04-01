/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.teamopensmartglasses.translate;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.teamopensmartglasses.translate.UI.TranslateFragment;
import com.teamopensmartglasses.translate.events.KillServiceEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {
  private String TAG = "TranslateApp_MainActivity";
  TranslateFragment frag;
  boolean mBound;
  public TranslationService mService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_translate_main);
    EventBus.getDefault().register(this);

    frag = TranslateFragment.newInstance();
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.container, frag)
          .commitNow();
    }

    mBound = false;
    startTranslationService();
  }

  @Override
  protected void onResume() {
    super.onResume();

    //bind to foreground service
    bindTranslationService();
  }

  @Override
  protected void onPause() {
    super.onPause();

    //unbind foreground service
    unbindTranslationService();
  }

  public void stopTranslationService() {
    unbindTranslationService();
    if (!isMyServiceRunning(TranslationService.class)) return;
    Intent stopIntent = new Intent(this, TranslationService.class);
    stopIntent.setAction(TranslationService.ACTION_STOP_FOREGROUND_SERVICE);
    startService(stopIntent);
  }

  public void sendTranslationServiceMessage(String message) {
    if (!isMyServiceRunning(TranslationService.class)) return;
    Intent messageIntent = new Intent(this, TranslationService.class);
    messageIntent.setAction(message);
    startService(messageIntent);
  }

  public void startTranslationService() {
    if (isMyServiceRunning(TranslationService.class)){
      Log.d(TAG, "Not starting service.");
      return;
    }
    Log.d(TAG, "Starting service.");
    Intent startIntent = new Intent(this, TranslationService.class);
    startIntent.setAction(TranslationService.ACTION_START_FOREGROUND_SERVICE);
    startService(startIntent);
    bindTranslationService();
  }

  //check if service is running
  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  public void bindTranslationService(){
    if (!mBound){
      Intent intent = new Intent(this, TranslationService.class);
      bindService(intent, translationAppServiceConnection, Context.BIND_AUTO_CREATE);
    }
  }

  public void unbindTranslationService() {
    if (mBound){
      unbindService(translationAppServiceConnection);
      mBound = false;
    }
  }

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection translationAppServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      TranslationService.LocalBinder sgmLibServiceBinder = (TranslationService.LocalBinder) service;
      mService = (TranslationService) sgmLibServiceBinder.getService();
      mBound = true;
    }
    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  public void broadcastTestClicked(View v) {
    Log.d(TAG, "Pressed 'Test Broadcast' button.");
    if (mService != null) {
      mService.sgmLib.sendReferenceCard("TPA Button Clicked", "Button was clicked. This is the content body of a card that was sent from a TPA using the SGMLib.");
    }
  }

  @Subscribe
  public void killService(KillServiceEvent event) {
    stopTranslationService();
    //finish();
  }
}
