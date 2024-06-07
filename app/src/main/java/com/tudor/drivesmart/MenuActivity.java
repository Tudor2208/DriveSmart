package com.tudor.drivesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MenuActivity extends AppCompatActivity {

    TextView helloTextView, usernameTextView;
    ImageButton logoutButton;
    Button startDrivingButton, myJourneysButton, settingsButton;
    FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        helloTextView = findViewById(R.id.hello_text_view);
        helloTextView.setText(helloTextView.getText() + ",");

        usernameTextView = findViewById(R.id.username_text_view);
        setUsername();

        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> showLogoutDialog());

        startDrivingButton = findViewById(R.id.start_driving_button);
        startDrivingButton.setOnClickListener(view -> showStartDrivingDialog());

        myJourneysButton = findViewById(R.id.my_journeys_button);
        myJourneysButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MyJourneysActivity.class)));

        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));
    }

    private CompletableFuture<String> getModel() {
        String[] models = {getString(R.string.detect_traffic_signs), getString(R.string.make_turn_prediction)};
        CompletableFuture<String> future = new CompletableFuture<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_model);
        builder.setItems(models, (dialog, which) -> {
            future.complete(String.valueOf(which));
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return future;
    }

    private void startDriving() {
        Intent intent = new Intent(getApplicationContext(), DrivingActivity.class);
        long currentTime = System.currentTimeMillis();
        intent.putExtra("startDrivingTime", currentTime);

        CompletableFuture<String> modelFuture = getModel();
        modelFuture.thenAccept(model -> {
            intent.putExtra("model", model);
            startActivity(intent);
        });
    }

    private void showStartDrivingDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_new_trip)
                .setMessage(R.string.new_trip_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> startDriving())
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.dialog_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    auth.signOut();
                    editor.remove("email_key");
                    editor.remove("password_key");
                    editor.remove("checkbox_key");
                    editor.remove("username_key");

                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                .show();
    }

    private void setUsername() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = reference.child("Users").child(uid).child("username");

            userRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                        usernameTextView.setText(username + "!");
                        editor.putString("username_key", username);
                        editor.apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("FirebaseDatabase", "loadUserName:onCancelled", databaseError.toException());
                }
            });
        }
    }
}