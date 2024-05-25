package com.tudor.drivesmart;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TrafficAlertsActivity extends AppCompatActivity {

    ListView trafficSignsListView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> trafficSigns = new ArrayList<>();
    private List<String> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_alerts);

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        trafficSignsListView = findViewById(R.id.traffic_signs_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trafficSigns);
        trafficSignsListView.setAdapter(adapter);

        labels = getLabelsFromAssets();
        labels.forEach(label -> {
            boolean toBeAnnounced = sharedPreferences.getBoolean(label, true);
            String formattedString = !toBeAnnounced ? String.format("%s: OFF", label) : String.format("%s: ON", label);
            trafficSigns.add(formattedString);
        });

        trafficSignsListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            showChangeStatusDialog(i);
            return true;
        });

        adapter.notifyDataSetChanged();
    }

    private void showChangeStatusDialog(int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sign_custom_dialog, null);
        builder.setView(dialogView);

        ImageView dialogImage = dialogView.findViewById(R.id.dialog_image);

        String label = labels.get(i);
        boolean toBeAnnounced = !sharedPreferences.getBoolean(label, true);

        @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(label, "drawable", getPackageName());
        dialogImage.setImageResource(resourceId);

        TextView dialogText = dialogView.findViewById(R.id.dialog_text);

        String signName = label.replace("_", " ");
        String title;

        if (toBeAnnounced) {
            dialogText.setText(R.string.confirm_enable_sign);
            title = String.format("%s \"%s\"", getString(R.string.enable), signName);
        } else {
            dialogText.setText(R.string.confirm_disable_sign);
            title = String.format("%s \"%s\"", getString(R.string.disable), signName);
        }

        builder.setTitle(title);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> changeSignStatus(i, toBeAnnounced));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changeSignStatus(int i, boolean toBeAnnounced) {
        String label = labels.get(i);

        String formattedString = !toBeAnnounced ? String.format("%s: OFF", label) : String.format("%s: ON", label);
        trafficSigns.set(i, formattedString);
        adapter.notifyDataSetChanged();

        editor.putBoolean(label, toBeAnnounced);
        editor.apply();
    }

    private List<String> getLabelsFromAssets() {
        AssetManager assetManager = getAssets();
        List<String> labels = new ArrayList<>();

        try {
            InputStream inputStream = assetManager.open("labels.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
        } catch (IOException ex) {
            Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
        }
        return labels;
    }

}