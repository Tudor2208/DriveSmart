package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText, emailEditText, confirmPasswordEditText;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    Button signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        usernameEditText = findViewById(R.id.username_signup_edit_text);
        emailEditText = findViewById(R.id.email_signup_edit_text);
        passwordEditText = findViewById(R.id.password_signup_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String password2 = confirmPasswordEditText.getText().toString();

            if (!email.isEmpty() && !username.isEmpty() && !password.isEmpty() && ! password2.isEmpty()) {
                if (password.length() < 8) {
                    Toast.makeText(getApplicationContext(), R.string.password_length, Toast.LENGTH_SHORT).show();
                } else {
                    if (password.equals(password2)) {
                        signup(username, email, password);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.fill_in, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void signup(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.child("Users").child(auth.getUid()).child("userName").setValue(username);
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.registration_not_successful, Toast.LENGTH_SHORT).show();
            }
        });
    }
}