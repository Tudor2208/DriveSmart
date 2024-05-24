package com.tudor.drivesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class MyJourneysActivity extends AppCompatActivity {

    ListView listViewJourneys;
    private final ArrayList<String> journeyList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final ArrayList<DataSnapshot> snapshotList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_journeys);

        listViewJourneys = findViewById(R.id.list_view_journeys);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, journeyList);
        listViewJourneys.setAdapter(adapter);

        listViewJourneys.setOnItemClickListener((adapterView, view, i, l) -> {
            DataSnapshot selectedJourneySnapshot = snapshotList.get(i);

            Intent intent = new Intent(getApplicationContext(), JourneySummaryActivity.class);

            Optional<String> journeyName = Optional.ofNullable(selectedJourneySnapshot.child("name").getValue(String.class));
            journeyName.ifPresent(name -> intent.putExtra("name", name));

            intent.putExtra("index", i + 1);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Optional<Long> startDateOptional = Optional.ofNullable(selectedJourneySnapshot.child("startTime").getValue(Long.class));
            startDateOptional.ifPresent(startDate -> {
                Date date = new Date(startDate);
                intent.putExtra("startTime", sdf.format(date));
            });

            Optional<Long> endDateOptional = Optional.ofNullable(selectedJourneySnapshot.child("endTime").getValue(Long.class));
            endDateOptional.ifPresent(endDate -> {
                Date date = new Date(endDate);
                intent.putExtra("endTime", sdf.format(date));
            });

            Optional<Long> durationOptional = Optional.ofNullable(selectedJourneySnapshot.child("duration").getValue(Long.class));
            durationOptional.ifPresent(duration -> intent.putExtra("duration", duration));

            intent.putExtra("key", selectedJourneySnapshot.getKey());
            startActivity(intent);
        });

        listViewJourneys.setOnItemLongClickListener((adapterView, view, i, l) -> {
            DataSnapshot selectedJourneySnapshot = snapshotList.get(i);
            showDeleteJourneyDialog(selectedJourneySnapshot);
            return true;
        });

        fetchJourneys();

        Toast.makeText(getApplicationContext(), R.string.long_press_delete_journey, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteJourneyDialog(DataSnapshot selectedJourneySnapshot) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_journey)
                .setMessage(R.string.delete_trip_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteJourney(selectedJourneySnapshot))
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                .show();
    }

    private void deleteJourney(DataSnapshot selectedJourneySnapshot) {
        selectedJourneySnapshot.getRef().removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.deleted_journey, Toast.LENGTH_SHORT).show();
                snapshotList.remove(selectedJourneySnapshot);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, R.string.failed_item_delete, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchJourneys() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("Trips");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                journeyList.clear();
                int counter = 1;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshotList.add(snapshot);
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) {
                        journeyList.add(String.format("%s #%d\n%s: %s", getString(R.string.journey), counter, getString(R.string.name), name));
                    } else {
                        journeyList.add(String.format("%s #%d", getString(R.string.journey), counter));
                    }
                    counter ++;
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("database", databaseError.getMessage());
            }
        });
    }
}