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

package com.google.mlkit.samples.nl.translate.java.UI;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.mlkit.samples.nl.translate.R;
import com.google.mlkit.samples.nl.translate.java.Globals;
import com.google.mlkit.samples.nl.translate.java.events.ChangeSourceLanguageEvent;
import com.google.mlkit.samples.nl.translate.java.events.ChangeTargetLanguageEvent;
import com.google.mlkit.samples.nl.translate.java.events.KillServiceEvent;
import com.teamopensmartglasses.sgmlib.SGMLib;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/***
 * Fragment view for handling translations
 */


public class TranslateFragment extends Fragment {
    TranslateViewModel viewModel;
    SGMLib sgmLib;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    public TranslateFragment() {
        sgmLib = Globals.sgmLib;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.translate_fragment, container, false);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final Button switchButton = view.findViewById(R.id.buttonSwitchLang); //good
    final ToggleButton sourceSyncButton = view.findViewById(R.id.buttonSyncSource);
    final ToggleButton targetSyncButton = view.findViewById(R.id.buttonSyncTarget);
    //final TextInputEditText srcTextView = view.findViewById(R.id.sourceText);
    //final TextView targetTextView = view.findViewById(R.id.targetText);
    final TextView downloadedModelsTextView = view.findViewById(R.id.downloadedModels); //good
    final Spinner sourceLangSelector = view.findViewById(R.id.sourceLangSelector); //good
    final Spinner targetLangSelector = view.findViewById(R.id.targetLangSelector); //good
    final Button killServiceButton = view.findViewById(R.id.killServiceButton);

    viewModel = ViewModelProviders.of(this).get(TranslateViewModel.class);

    // Get available language list and set up source and target language spinners
    // with default selections.
    final ArrayAdapter<TranslateViewModel.Language> adapter =
        new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.getAvailableLanguages());
    sourceLangSelector.setAdapter(adapter);
    targetLangSelector.setAdapter(adapter);
    sourceLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("en")));
    targetLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("es")));
    sourceLangSelector.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //viewModel.sourceLang.setValue(adapter.getItem(position));
            //TODO: Set source language
              EventBus.getDefault().post(new ChangeSourceLanguageEvent(adapter.getItem(position).getCode()));
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    targetLangSelector.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //viewModel.targetLang.setValue(adapter.getItem(position));
              // TODO: set target language
              EventBus.getDefault().post(new ChangeTargetLanguageEvent(adapter.getItem(position).getCode()));
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });

      killServiceButton.setOnClickListener(
              new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    EventBus.getDefault().post(new KillServiceEvent());
                  }
              });

    switchButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int sourceLangPosition = sourceLangSelector.getSelectedItemPosition();
            sourceLangSelector.setSelection(targetLangSelector.getSelectedItemPosition());
            targetLangSelector.setSelection(sourceLangPosition);
          }
        });

    // Set up toggle buttons to delete or download remote models locally.
    sourceSyncButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TranslateViewModel.Language language =
                adapter.getItem(sourceLangSelector.getSelectedItemPosition());
            if (isChecked) {
              viewModel.downloadLanguage(language);
            } else {
              viewModel.deleteLanguage(language);
            }
          }
        });
    targetSyncButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TranslateViewModel.Language language =
                adapter.getItem(targetLangSelector.getSelectedItemPosition());
            if (isChecked) {
              viewModel.downloadLanguage(language);
            } else {
              viewModel.deleteLanguage(language);
            }
          }
        });

    // Update sync toggle button states based on downloaded models list.
    viewModel.availableModels.observe(
        getViewLifecycleOwner(),
        new Observer<List<String>>() {
          @Override
          public void onChanged(@Nullable List<String> translateRemoteModels) {
            String output =
                getContext().getString(R.string.downloaded_models_label, translateRemoteModels);
            downloadedModelsTextView.setText(output);

            sourceSyncButton.setChecked(
                !viewModel.requiresModelDownload(
                    adapter.getItem(sourceLangSelector.getSelectedItemPosition()),
                    translateRemoteModels));
            targetSyncButton.setChecked(
                !viewModel.requiresModelDownload(
                    adapter.getItem(targetLangSelector.getSelectedItemPosition()),
                    translateRemoteModels));
          }
        });
  }

  private void setProgressText(TextView tv) {
    tv.setText(getContext().getString(R.string.translate_progress));
  }


}


