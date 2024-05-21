package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    TextView forgotTextView;
    Button login_button, sign_up_button;
    CheckBox rememberMeCheckBox;
    FirebaseAuth auth;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.login_email_edit_text);
        passwordEditText = findViewById(R.id.login_password_edit_text);
        forgotTextView = findViewById(R.id.forgot_text_view);
        login_button = findViewById(R.id.login_button);
        sign_up_button = findViewById(R.id.login_signup_button);
        rememberMeCheckBox = findViewById(R.id.remember_me_check);

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean("checkbox_key", false);

        if (isRemembered) {
            String email = sharedPreferences.getString("email_key", "");
            String password = sharedPreferences.getString("password_key", "");
            login(email, password, true);
        }

        login_button.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            boolean isChecked = rememberMeCheckBox.isChecked();

            if (!email.isEmpty() && !password.isEmpty()) {
                login(email, password, isChecked);
            } else {
                Toast.makeText(getApplicationContext(), R.string.fill_in_all_fields, Toast.LENGTH_SHORT).show();
            }
        });

        sign_up_button.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SignupActivity.class)));

        forgotTextView.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));
    }

    private void login(String email, String password, boolean isChecked) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                editor = sharedPreferences.edit();
                editor.putString("email_key", email);
                editor.putString("password_key", password);
                editor.putBoolean("checkbox_key", isChecked);
                editor.apply();

                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
            }
        });
    }
}