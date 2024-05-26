package com.tudor.drivesmart;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tudor.drivesmart.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        double startLat = intent.getDoubleExtra("startLat", 0);
        double endLat = intent.getDoubleExtra("endLat", 0);

        double startLong = intent.getDoubleExtra("startLong", 0);
        double endLong = intent.getDoubleExtra("endLong", 0);

        LatLng start = new LatLng(startLat, startLong);
        LatLng end = new LatLng(endLat, endLong);

        mMap.addMarker(new MarkerOptions().position(start).title(getString(R.string.start)));
        mMap.addMarker(new MarkerOptions().position(end).title(getString(R.string.end)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
    }
}