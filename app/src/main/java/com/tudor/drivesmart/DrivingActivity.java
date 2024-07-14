package com.tudor.drivesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DrivingActivity extends AppCompatActivity {

    TextureView textureView;
    TextView classTextView;
    CameraManager cameraManager;
    CameraDevice cameraDevice;
    Handler handler;
    ImageView imageView;
    Bitmap bitmap;
    Yolov5TFLiteDetector detector;
    TrafficSignSoundManager soundManager;
    Paint boxPaint = new Paint(), textPaint = new Paint();
    Button finishTripButton;
    DatabaseReference databaseReference;
    FirebaseUser user;
    SharedPreferences sharedPreferences;
    Location startLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    private static final long DELAY = 10000;
    private final HashMap<String, Long> lastPlayed = new HashMap<>();
    private TurnPredictionModel turnPredictionModelResnet;
    private String modelType;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);
        checkAndRequestPermissions();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sharedPreferences = getSharedPreferences("saveData", Context.MODE_PRIVATE);

        HandlerThread videoThread = new HandlerThread("videoThread");
        videoThread.start();
        handler = new Handler(videoThread.getLooper());

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        textureView = findViewById(R.id.textureView);
        classTextView = findViewById(R.id.class_text_view);
        imageView = findViewById(R.id.imageView);

        detector = new Yolov5TFLiteDetector();
        detector.setModelFile("yolo.tflite");
        detector.initialModel(this);

        turnPredictionModelResnet = new TurnPredictionModel(this, "turn_prediction_cnn.tflite");

        Intent intent = getIntent();
        modelType = intent.getStringExtra("model");

        textPaint.setColor(Color.RED);
        textPaint.setTextSize(40);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5);
        boxPaint.setAntiAlias(true);

        soundManager = new TrafficSignSoundManager(this);
        finishTripButton = findViewById(R.id.finish_trip_button);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        finishTripButton.setOnClickListener(view -> showConfirmFinishJourneyDialog());

        setupTextureView();
    }

    @SuppressLint("MissingPermission")
    private void finishJourney() {
        Intent intent = getIntent();

        long startDrivingTime = intent.getLongExtra("startDrivingTime", 0);
        long endDrivingTime = System.currentTimeMillis();
        long duration = endDrivingTime - startDrivingTime;

        CompletableFuture<Location> currentLocationFuture = getCurrentLocation();
        currentLocationFuture.thenAccept(endLocation -> {
            if (user != null) {
                DatabaseReference userTrips = databaseReference.child("Users").child(user.getUid()).child("Trips");
                DatabaseReference newTrip = userTrips.push();
                newTrip.child("startTime").setValue(startDrivingTime);
                newTrip.child("endTime").setValue(endDrivingTime);
                newTrip.child("duration").setValue(duration);
                newTrip.child("startLat").setValue(startLocation.getLatitude());
                newTrip.child("startLong").setValue(startLocation.getLongitude());
                newTrip.child("endLat").setValue(endLocation.getLatitude());
                newTrip.child("endLong").setValue(endLocation.getLongitude());
            }

            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            finish();
        }).exceptionally(e -> {
            Log.e("finishJourney", "Failed to get last location", e);
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
            finish();
            return null;
        });
    }

    @SuppressLint("MissingPermission")
    private CompletableFuture<Location> getCurrentLocation() {
        CompletableFuture<Location> future = new CompletableFuture<>();

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        future.complete(location);
                    } else {
                        future.completeExceptionally(new Exception("Location is null"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    private void showConfirmFinishJourneyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit)
                .setMessage(R.string.finish_trip_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> finishJourney())
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                .show();
    }

    @Override
    public void onBackPressed() {
        showConfirmFinishJourneyDialog();
    }

    @Override
    protected void onDestroy() {
        soundManager.shutdown();
        super.onDestroy();
    }

    private void setupTextureView() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                openCamera();

                CompletableFuture<Location> currentLocation = getCurrentLocation();
                currentLocation.thenAccept(location -> startLocation = location);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                // Handle texture size change if needed
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return true; // Return true to release the SurfaceTexture
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                bitmap = textureView.getBitmap();

                switch(modelType) {
                    case "0":
                        detectTrafficSigns();
                        break;
                    case "1":
                        makeTurnPrediction();
                        break;
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void makeTurnPrediction() {
        float[][][][] input = ImageUtils.preprocessImage(bitmap);
        Pair<String, Float> predictionResnet = turnPredictionModelResnet.predict(input);

        String predictedClassResnet = predictionResnet.first;

        if (predictedClassResnet.equals("LEFT")) {
            classTextView.setText(getString(R.string.left));
        } else if (predictedClassResnet.equals("RIGHT")) {
            classTextView.setText(getString(R.string.right));
        } else {
            classTextView.setText(getString(R.string.straight));
        }

        imageView.setImageBitmap(bitmap);
    }

    @SuppressLint("DiscouragedApi")
    private void detectTrafficSigns() {
        ArrayList<Recognition> recognitions = detector.detect(bitmap);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        for(Recognition recognition: recognitions) {
            if(recognition.getConfidence() > 0.5) {

                @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(recognition.getLabelName() + "_sign", "string", getPackageName());
                RectF location = recognition.getLocation();
                canvas.drawRect(location, boxPaint);
                canvas.drawText(getString(resourceId), location.left, location.top, textPaint);

                long currentTime = System.currentTimeMillis();
                String labelName = recognition.getLabelName();

                if ((!lastPlayed.containsKey(labelName) || currentTime - lastPlayed.get(labelName) > DELAY) && sharedPreferences.getBoolean(labelName, true)) {
                    resourceId = getResources().getIdentifier(recognition.getLabelName(), "string", getPackageName());
                    soundManager.announce(getString(resourceId));
                    lastPlayed.put(labelName, currentTime);
                }
            }
        }
        imageView.setImageBitmap(mutableBitmap);
    }


    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), R.string.camera_permission_settings, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CompletableFuture<Location> currentLocation = getCurrentLocation();
                currentLocation.thenAccept(location -> startLocation = location);
            } else {
                Toast.makeText(getApplicationContext(), R.string.location_permission_settings, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    private void openCamera() {
        CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d("DrivingActivity", "Camera has been opened successfully.");
                cameraDevice = camera;

                SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                Surface surface = new Surface(surfaceTexture);

                CaptureRequest.Builder captureRequest;
                try {
                    captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                } catch (CameraAccessException e) {
                    Log.d("camera", e.getMessage());
                    throw new RuntimeException(e);
                }
                captureRequest.addTarget(surface);

                try {
                    cameraDevice.createCaptureSession(List.of(surface), new CameraCaptureSession.StateCallback(){
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(captureRequest.build(), null, null);
                            } catch (CameraAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    }, handler);
                } catch (CameraAccessException e) {
                    Log.d("camera", e.getMessage());
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.d("DrivingActivity", "Camera disconnected.");
                camera.close();
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.e("DrivingActivity", "Error opening camera: " + error);
                camera.close();
            }
        };

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraManager.getCameraIdList()[0], stateCallback, handler);
            } else {
                Log.e("DrivingActivity", "Camera permission is not granted.");
            }
        } catch (CameraAccessException e) {
            Log.e("DrivingActivity", "Failed to access camera.", e);
        }
    }
}
