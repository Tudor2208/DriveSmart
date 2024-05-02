package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    TextView helloTextView;
    TextView usernameTextView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        helloTextView = findViewById(R.id.hello_text_view);
        usernameTextView = findViewById(R.id.username_text_view);

        helloTextView.setText(helloTextView.getText() + ",");
        usernameTextView.setText(usernameTextView.getText() + "!");
    }
}