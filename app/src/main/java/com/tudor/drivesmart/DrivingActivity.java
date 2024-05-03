package com.tudor.drivesmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class DrivingActivity extends AppCompatActivity {

    static final int CAMERA_REQUEST_CODE = 101;
    TextureView textureView;
    CameraManager cameraManager;
    CameraDevice cameraDevice;
    Handler handler;
    ImageView imageView;
    Bitmap bitmap;
    Yolov5TFLiteDetector detector;
    Paint boxPaint = new Paint();
    Paint textPaint = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);
        checkAndRequestPermissions();

        HandlerThread videoThread = new HandlerThread("videoThread");
        videoThread.start();
        handler = new Handler(videoThread.getLooper());

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        textureView = findViewById(R.id.textureView);
        imageView = findViewById(R.id.imageView);

        detector = new Yolov5TFLiteDetector();
        detector.setModelFile("best-fp16.tflite");
        detector.initialModel(this);

        textPaint.setColor(Color.RED);
        textPaint.setTextSize(40);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5);
        boxPaint.setAntiAlias(true);

        setupTextureView();
    }

    private void setupTextureView() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                openCamera();
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
                ArrayList<Recognition> detections = detector.detect(bitmap);
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);

                for(Recognition recognition: detections){
                    if(recognition.getConfidence() > 0.4){
                        RectF location = recognition.getLocation();
                        canvas.drawRect(location, boxPaint);
                        canvas.drawText(recognition.getLabelName() + ":" + recognition.getConfidence(), location.left, location.top, textPaint);
                    }
                }
                imageView.setImageBitmap(mutableBitmap);
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Log.e("DrivingActivity", "Camera permission not granted.");
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
