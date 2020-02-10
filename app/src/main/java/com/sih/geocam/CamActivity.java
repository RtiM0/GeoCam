package com.sih.geocam;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.VideoResult;
import com.snatik.storage.Storage;
import com.yashovardhan99.timeit.Stopwatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CamActivity extends AppCompatActivity implements Stopwatch.OnTickListener {

    private Button rec;
    private CameraView camera;
    private int flag = 0;
    private Storage storage;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean canStartRecording = false;
    private Stopwatch stopwatch;
    private long uniTime;
    private JSONArray data = new JSONArray();
    private File convention;
    private String CONVENTION_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        camera = findViewById(R.id.camerer);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        stopwatch = new Stopwatch();
        stopwatch.setOnTickListener(this);

        storage = new Storage(getApplicationContext());
        storage.createDirectory(storage.getExternalStorageDirectory() + File.separator + "GeoCam");
        CONVENTION_PATH = storage.getInternalFilesDirectory() + File.separator + "convention.txt";

        if (!storage.isDirectoryExists(CONVENTION_PATH)) {
            convention = new File(CONVENTION_PATH);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(convention);
                fileOutputStream.write("0".getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        camera.setLifecycleOwner(this);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                String convent = storage.readTextFile(CONVENTION_PATH);
                int conv = Integer.parseInt(convent) + 1;
                storage.move(result.getFile().getAbsolutePath(), (storage.getExternalStorageDirectory() + File.separator + "GeoCam" + File.separator + conv + ".mp4"));
                File saver = new File(storage.getExternalStorageDirectory() + File.separator + "GeoCam", conv + ".json");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(saver);
                    fileOutputStream.write(data.toString().getBytes());
                    fileOutputStream.close();
                    convention = new File(CONVENTION_PATH);
                    FileOutputStream fileOutputStream1 = new FileOutputStream(convention);
                    fileOutputStream1.write((conv + "").getBytes());
                    fileOutputStream1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                createLocationRequest();
            }

            @Override
            public void onVideoRecordingStart() {
                super.onVideoRecordingStart();
                if (canStartRecording) {
                    stopwatch.start();
                    startLocationUpdates();
                } else {
                    Toast.makeText(CamActivity.this, "Failed to make Location Request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onVideoRecordingEnd() {
                super.onVideoRecordingEnd();
                stopwatch.stop();
                stopLocationUpdates();
                canStartRecording = false;
            }
        });
        createLocationRequest();
        rec = findViewById(R.id.recbtn);
        rec.setOnClickListener(v -> {
            if (flag == 0) {
                camera.takeVideo(new File(getFilesDir(), "video.mp4"));
                flag = 1;
            } else {
                camera.stopVideo();
                flag = 0;
            }
        });
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            Toast.makeText(CamActivity.this, "Successfully Initialized Location Client", Toast.LENGTH_SHORT).show();
            canStartRecording = true;
        });
        task.addOnFailureListener(this, e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case CommonStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(CamActivity.this, 1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    break;
            }
        });
    }
    private void startLocationUpdates() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    JSONObject tempData = new JSONObject();
                    double locationLatitude = location.getLatitude();
                    double locationLongitude = location.getLongitude();
                    long time = uniTime;
                    try{
                        tempData.put("time",time);
                        tempData.put("latitude",locationLatitude);
                        tempData.put("longitude",locationLongitude);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    data.put(tempData);
                    Toast.makeText(CamActivity.this, "Rec for Time "+time, Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getApplicationContext(), "location permission required !!", Toast.LENGTH_SHORT).show();
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onTick(Stopwatch stopwatch) {
        uniTime = (stopwatch.getElapsedTime()/1000);
    }
}