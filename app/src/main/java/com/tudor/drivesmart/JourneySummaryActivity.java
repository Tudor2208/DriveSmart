package com.tudor.drivesmart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Optional;

public class JourneySummaryActivity extends AppCompatActivity {

    TextView summaryTitleTextView;
    ListView summaryListView;
    private final ArrayList<String> infoList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_summary);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, infoList);

        summaryListView = findViewById(R.id.list_view_summary);
        summaryListView.setAdapter(adapter);

        summaryTitleTextView = findViewById(R.id.summary_title_text_view);

        Intent intent = getIntent();
        int index = intent.getIntExtra("index", 0);
        summaryTitleTextView.setText(String.format("%s #%d", getString(R.string.journey), index));

        Optional<String> nameOptional = Optional.ofNullable(intent.getStringExtra("name"));
        String name = nameOptional.orElse(getString(R.string.undefined_name));

        infoList.add(String.format("%s: %s", getString(R.string.name), name));

        String startTime = intent.getStringExtra("startTime");
        infoList.add(String.format("%s: %s", getString(R.string.start), startTime));

        String endTime = intent.getStringExtra("endTime");
        infoList.add(String.format("%s: %s", getString(R.string.end), endTime));

        long duration = intent.getLongExtra("duration", 0);
        String formattedDuration = formatDuration(duration);

        infoList.add(String.format("%s: %s", getString(R.string.duration), formattedDuration));

        summaryListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            if (i == 0) {
                showEditJourneyNameDialog();
            }
            return true;
        });
    }

    private void showEditJourneyNameDialog() {
        EditText editTextField = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_journey_name)
                .setMessage(R.string.edit_journey_name_message)
                .setView(editTextField)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    String newName = editTextField.getText().toString();
                    handleUserInput(newName);
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void handleUserInput(String newName) {
        String key = getIntent().getStringExtra("key");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Trips").child(key);
        tripRef.child("name").setValue(newName);
        infoList.set(0, String.format("%s: %s", getString(R.string.name), newName));
        adapter.notifyDataSetChanged();
    }

    private String formatDuration(long millis) {
        int seconds = (int) (millis / 1000), minutes = 0, hours = 0, days = 0;

        if (seconds > 59) {
            minutes = seconds / 60;
            seconds = seconds % 60;

            if (minutes > 59) {
                hours = minutes / 60;
                minutes = minutes % 60;
                seconds = 0;

                if (hours > 23) {
                    days = hours / 24;
                    hours = hours % 24;
                    minutes = 0;
                }
            }
        }

        String formatted = "";

        if (days != 0) {
            formatted += days + "d ";
        }

        if (hours != 0) {
            formatted += hours + "h ";
        }

        if (minutes != 0) {
            formatted += minutes + "m ";
        }

        if (seconds != 0) {
            formatted += seconds + "s ";
        }

        return formatted;
    }
}