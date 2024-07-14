package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.LocaleList;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView carImageView;
    TextView smartDriveTextView;
    Animation carAnimation;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);

        smartDriveTextView = findViewById(R.id.drive_smart_text_view);
        carImageView = findViewById(R.id.car_image_view);

        smartDriveTextView.setText("");
        carAnimation = AnimationUtils.loadAnimation(this, R.anim.car_animation);

        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.car_sound);

        boolean isDarkTheme = sharedPreferences.getBoolean("is_dark_theme", true);
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        String lang = sharedPreferences.getString("app_lang", "en");
        setLocale(this, lang);

        carImageView.setAnimation(carAnimation);
        carImageView.startAnimation(carAnimation);
        carAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mediaPlayer.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                mediaPlayer.stop();
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        new CountDownTimer(5000, 200) {
            int counter = 0;
            String displayedText = "";
            final String DRIVE_SMART = "DriveSmart";

            @Override
            public void onTick(long l) {
                if (!displayedText.equals(DRIVE_SMART)) {
                    displayedText += DRIVE_SMART.charAt(counter);
                    counter ++;
                    smartDriveTextView.setText(displayedText);
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    private void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        config.setLocale(locale);
        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        config.setLocales(localeList);
        context.createConfigurationContext(config);
    }
}