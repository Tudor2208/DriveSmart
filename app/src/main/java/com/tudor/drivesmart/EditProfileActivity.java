package com.tudor.drivesmart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity {

    ListView editProfileListView;
    Button changePasswordButton;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> accountInfo = new ArrayList<>();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FirebaseAuth auth;
    FirebaseUser user;

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toast.makeText(getApplicationContext(), R.string.long_press_edit, Toast.LENGTH_SHORT).show();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editProfileListView = findViewById(R.id.list_view_profile);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accountInfo);
        editProfileListView.setAdapter(adapter);

        String username = sharedPreferences.getString("username_key", null);
        accountInfo.add(String.format("%s: %s", getString(R.string.username), username));

        String emailAddress = user.getEmail();
        accountInfo.add(String.format("%s: %s", getString(R.string.email), emailAddress));

        FirebaseUserMetadata userMetadata = user.getMetadata();
        assert userMetadata != null;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long registerTimestamp = userMetadata.getCreationTimestamp();
        Date registerDate = new Date(registerTimestamp);
        String formattedRegisterDate = sdf.format(registerDate);
        accountInfo.add(String.format("%s: %s", getString(R.string.register_date), formattedRegisterDate));

        long lastSignInTimestamp = userMetadata.getLastSignInTimestamp();
        Date lastSignIn = new Date(lastSignInTimestamp);
        String formattedLastSignInDate = sdf.format(lastSignIn);
        accountInfo.add(String.format("%s: %s", getString(R.string.last_login), formattedLastSignInDate));
        adapter.notifyDataSetChanged();

        editProfileListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case 0:
                    showEditUsernameDialog(username);
                    break;
                case 1:
                    showEditEmailAddressDialog();
                    break;
            }
            return true;
        });

        changePasswordButton = findViewById(R.id.change_password_button);
        changePasswordButton.setOnClickListener(view -> showChangePasswordDialog());
    }

    private void showEditUsernameDialog(String currentUsername) {
        EditText editTextField = new EditText(this);
        editTextField.setText(currentUsername);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_username)
                .setMessage(R.string.edit_username_message)
                .setView(editTextField)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    String newUsername = editTextField.getText().toString();
                    if (isValidUsername(newUsername)) {
                        editUsername(newUsername);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.invalid_username, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private boolean isValidUsername(String username) {
        return !username.isEmpty() && !username.contains(" ") && username.length() <= 10;
    }

    private void editUsername(String username) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(user.getUid())
                .child("username")
                .setValue(username);
        editor.putString("username_key", username);
        editor.apply();

        accountInfo.set(0, String.format("%s: %s", getString(R.string.username), username));
        adapter.notifyDataSetChanged();
    }

    private void showEditEmailAddressDialog() {
        EditText editTextField = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.change_email)
                .setMessage(R.string.change_email_message)
                .setView(editTextField)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    String emailAddress = editTextField.getText().toString();
                    user.updateEmail(emailAddress).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), R.string.changed_email_1, Toast.LENGTH_LONG).show();
                                    Toast.makeText(getApplicationContext(), R.string.changed_email_2, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), R.string.email_already_used, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void showChangePasswordDialog() {
        EditText editTextField = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.change_password)
                .setMessage(R.string.change_password_message)
                .setView(editTextField)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    String newPassword = editTextField.getText().toString();
                    changePassword(newPassword);
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void changePassword(String newPassword) {
        Matcher matcher = pattern.matcher(newPassword);
        if (matcher.matches()) {
            getCurrentPassword().thenAccept(currentPassword -> {
                if (currentPassword != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), currentPassword);
                    user.reauthenticate(credential)
                            .addOnSuccessListener(aVoid -> {
                                user.updatePassword(newPassword)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(getApplicationContext(), R.string.password_successfully_updated, Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getApplicationContext(), R.string.failed_password_update, Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), R.string.failed_reauthentication, Toast.LENGTH_SHORT).show();
                            });
                }
            });
        } else {
            showWeakPasswordDialog();
        }
    }

    private CompletableFuture<String> getCurrentPassword() {
        CompletableFuture<String> future = new CompletableFuture<>();

        EditText editTextField = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.current_password)
                .setMessage(R.string.current_password_message)
                .setView(editTextField)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    String currentPassword = editTextField.getText().toString();
                    future.complete(currentPassword);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    future.complete(null);
                })
                .create();

        dialog.show();

        return future;
    }

    private void showWeakPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.weak_password)
                .setMessage(R.string.password_rules)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                .show();
    }
}