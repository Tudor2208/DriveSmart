package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    Button editProfileButton, trafficAlertsButton;
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
    }

}
