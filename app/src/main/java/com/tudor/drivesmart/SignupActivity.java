package com.tudor.drivesmart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    EditText usernameEditText, passwordEditText, emailEditText, confirmPasswordEditText;
    Button signupButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        usernameEditText = findViewById(R.id.signup_username_edit_text);
        emailEditText = findViewById(R.id.signup_email_edit_text);
        passwordEditText = findViewById(R.id.signup_password_edit_text);
        confirmPasswordEditText = findViewById(R.id.signup_confirm_password_edit_text);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String password2 = confirmPasswordEditText.getText().toString();

            if (!email.isEmpty() && !username.isEmpty() && !password.isEmpty() && ! password2.isEmpty()) {
                if (isValidUsername(username)) {
                    if (password.equals(password2)) {
                        Matcher matcher = pattern.matcher(password);
                        if (matcher.matches()) {
                            signup(username, email, password);
                        } else {
                            showWeakPasswordDialog();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.invalid_username, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), R.string.fill_in_all_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidUsername(String username) {
        return !username.isEmpty() && !username.contains(" ") && username.length() <= 10;
    }

    private void showWeakPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.weak_password)
                .setMessage(R.string.password_rules)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                .show();
    }

    private void signup(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            reference.child("Users").child(user.getUid()).child("username").setValue(username);
                            Toast.makeText(getApplicationContext(), R.string.validate_email, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(getApplicationContext(), R.string.email_already_used, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}