package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    Button editProfileButton, trafficAlertsButton, changeLanguageButton;
    SwitchMaterial themeToggle;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editProfileButton = findViewById(R.id.edit_profile_button);
        trafficAlertsButton = findViewById(R.id.traffic_alerts_button);
        themeToggle = findViewById(R.id.theme_toggle);
        changeLanguageButton = findViewById(R.id.change_language_button);

        boolean isDarkTheme = sharedPreferences.getBoolean("is_dark_theme", true);
        if (isDarkTheme) {
            themeToggle.setText(getString(R.string.dark));
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            themeToggle.setChecked(true);
        } else {
            themeToggle.setText(getString(R.string.light));
            themeToggle.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        themeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("is_dark_theme", isChecked);
            editor.apply();
            recreate();
        });

        editProfileButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), EditProfileActivity.class)));
        trafficAlertsButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TrafficAlertsActivity.class)));
        changeLanguageButton.setOnClickListener(view -> showChangeLanguageDialog());
    }

    private void showChangeLanguageDialog() {
        String[] languages = {"en", "ro"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_language));
        builder.setItems(languages, (dialog, which) -> {
            String selectedLanguage = languages[which];
            editor.putString("app_lang", selectedLanguage);
            editor.apply();
            Toast.makeText(getApplicationContext(), R.string.restart_app, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

}
