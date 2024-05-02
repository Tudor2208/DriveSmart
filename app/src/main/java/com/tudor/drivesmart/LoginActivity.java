package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    TextView forgotTextView;
    Button login_button, sign_up_button;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        forgotTextView = findViewById(R.id.forgot_text_view);
        login_button = findViewById(R.id.login_button);
        sign_up_button = findViewById(R.id.sign_up_button);

        login_button.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                signin(email, password);
            } else {
                Toast.makeText(getApplicationContext(), "Please enter an email and a password", Toast.LENGTH_SHORT).show();
            }
        });

        sign_up_button.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
        });

        forgotTextView.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
        });
    }

    private void signin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "Sign in not successful", Toast.LENGTH_SHORT).show();
            }
        });
    }
}