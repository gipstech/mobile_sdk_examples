package com.gipstech.indoorlocationexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.gipstech.*;

public class MainActivity extends AppCompatActivity {
    final static String DEV_KEY = ""; // PUT YOUR DEV KEY HERE
    final static String BUILDING_ID = ""; // PUT THE ID OF THE BUILDING HERE

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.mainTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
        };

        GiPStech.init(this, DEV_KEY);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 0);
        } else {
            startLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!allGranted(grantResults)) {
            textView.append("Please grant all permissions to start location");
            return;
        }

        startLocation();
    }

    boolean allGranted(int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }

        for (int val : grantResults) {
            if (val != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    void performFastCalibration() {
        GiPStech.getCalibrationManager().beginCalibration(CalibrationManager.CALIBRATION_TYPE_FAST, new ProgressCallback<Void>() {
            @Override
            public void onResult(Void result) {
                textView.append("Calibration completed\n");
                GiPStech.getIndoorLocalizer().registerListener(listener);
            }

            @Override
            public void onProgress(int progress) {
                textView.append("Calibration progress: " + progress + "%\n");
                if (progress >= 25) {
                    GiPStech.getCalibrationManager().endCalibration();
                }
            }

            @Override
            public void onError(GiPStechError error) {
                textView.append(error.getMessage() + "\n");
            }
        });
    }

    void startLocation() {
        GiPStech.getIndoorLocalizer().selectBuilding(BUILDING_ID, new ProgressCallback<Void>() {
            @Override
            public void onResult(Void result) {
                textView.append("Selection completed\n");
                GiPStech.getIndoorLocalizer().registerListener(listener);
            }

            @Override
            public void onProgress(int progress) {
                textView.append("Selection progress: " + progress + "%\n");
            }

            @Override
            public void onError(GiPStechError error) {
                textView.append(error.getMessage() + "\n");
            }
        });
    }

    IndoorLocalizer.Listener listener = new IndoorLocalizer.Listener() {
        @Override
        public void onRegistrationUpdate(int percentage) {
            textView.append("Registration progress: " + percentage + "%\n");
        }

        @Override
        public void onRegistrationComplete() {
            textView.append("Registration completed\n");
        }

        @Override
        public void onLocationUpdate(IndoorLocation location) {
            textView.append("Location: " + location.getLongitude() + " " + location.getLatitude() + "\n");
        }

        @Override
        public boolean onFloorChange(Floor floor) {
            textView.append("Floor: " + floor.getName() + "\n");
            return true;
        }

        @Override
        public void onAttitudeUpdate(Attitude attitude) {
            textView.append("Heading: " + attitude.getHeading() + "\n");
        }

        @Override
        public void onError(GiPStechError error) {
            textView.append(error.getMessage() + "\n");

            if (GiPStechError.FAST_CALIBRATION_REQUIRED == error.getCode()) {
                performFastCalibration();
            }
        }
    };
}