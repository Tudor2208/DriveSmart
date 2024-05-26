package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText resetPasswordEditText;
    Button resetPasswordButton;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        resetPasswordButton = findViewById(R.id.reset_password_button);
        resetPasswordEditText = findViewById(R.id.reset_password_email);

        resetPasswordButton.setOnClickListener(view -> {
            String email = resetPasswordEditText.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(getApplicationContext(), R.string.empty_email, Toast.LENGTH_SHORT).show();
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), R.string.check_email, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}