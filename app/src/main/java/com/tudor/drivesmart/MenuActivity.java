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
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {

    TextView helloTextView, usernameTextView;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageButton logoutButton;
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

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = reference.child("Users").child(uid).child("userName");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.getValue().toString();
                        usernameTextView.setText(username + "!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("FirebaseDatabase", "loadUserName:onCancelled", databaseError.toException());
                }
            });
        }

        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> {
            showDialogMessage();
        });
    }

    private void showDialogMessage() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.logout)
                .setMessage(R.string.dialog_logout)
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    auth.signOut();
                    editor.remove("email_key");
                    editor.remove("password_key");
                    editor.remove("checkbox_key");
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }).show();
        alertDialog.create();
    }
}