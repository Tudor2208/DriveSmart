package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ImageView carImageView;
    TextView smartDriveTextView;
    Animation carAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smartDriveTextView = findViewById(R.id.drive_smart_text_view);
        smartDriveTextView.setText("");

        carImageView = findViewById(R.id.car_image_view);
        carAnimation = AnimationUtils.loadAnimation(this, R.anim.car_animation);

        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.car_sound);

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
            final String SMART_DRIVE = "SmartDrive";

            @Override
            public void onTick(long l) {
                if (!displayedText.equals(SMART_DRIVE)) {
                    displayedText += SMART_DRIVE.charAt(counter);
                    counter ++;
                    smartDriveTextView.setText(displayedText);
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }
}