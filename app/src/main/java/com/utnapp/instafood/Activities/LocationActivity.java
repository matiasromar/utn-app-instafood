package com.utnapp.instafood.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class LocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 99;

    private boolean requestingLocationUpdates = false;
    private boolean locationPermissionsGranted = false;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private final String locationAccessExplanation;
    private final long locationRequestInterval;
    private final long locationRequestFastestInterval;
    private final int locationRequestPriority;
    private ProgressDialog progress;

    protected LocationActivity(String locationAccessExplanation, long locationRequestInterval, long locationRequestFastestInterval, int locationRequestPriority) {
        this.locationAccessExplanation = locationAccessExplanation;
        this.locationRequestInterval = locationRequestInterval;
        this.locationRequestFastestInterval = locationRequestFastestInterval;
        this.locationRequestPriority = locationRequestPriority;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLocationPermissions();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if(locationRequest == null){
            locationRequest = createLocationRequestAndValidatesSettings();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS){
            if(resultCode == Activity.RESULT_OK ){
                requestingLocationUpdates = true;
                initLocationUpdates();
            } else {
                finishActivityWithError(getString(R.string.location_key_to_service));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionsGranted = true;
                    initLocationUpdates();
                } else {
                    finishActivityWithError(getString(R.string.location_key_to_service));
                }
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        if(googleApiClient != null){
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        initLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        showToastUnrecognizedLocationError();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        switch (connectionResult.getErrorCode()){
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                finishActivityWithError(getString(R.string.google_serv_need_update));
                break;
            case ConnectionResult.NETWORK_ERROR:
                finishActivityWithError(getString(R.string.check_internet_connection));
                break;
            default:
                showToastUnrecognizedLocationError();
                break;
        }
    }

    protected String getCity(Location location) {
        if (location != null) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                return null;
            }

            if (addresses.size() > 0)
                return addresses.get(0).getLocality();
        }

        showToastUnrecognizedLocationError();
        return null;
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, locationAccessExplanation, Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestLocationPermissions();
                    }
                }, CommonUtilities.WAIT_LENGTH_LONG);
            } else {
                requestLocationPermissions();
            }
        } else {
            locationPermissionsGranted = true;
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    private LocationRequest createLocationRequestAndValidatesSettings() {
        LocationRequest newLocationRequest = new LocationRequest();
        newLocationRequest.setInterval(locationRequestInterval);
        newLocationRequest.setFastestInterval(locationRequestFastestInterval);
        newLocationRequest.setPriority(locationRequestPriority);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(newLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        requestingLocationUpdates = true;
                        initLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(LocationActivity.this, REQUEST_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            showToastUnrecognizedLocationError();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        showToastUnrecognizedLocationError();
                        break;
                }
            }
        });

        return newLocationRequest;
    }

    private void initLocationUpdates() {
        if(googleApiClient != null && googleApiClient.isConnected() && locationRequest != null && locationPermissionsGranted && requestingLocationUpdates){
            //noinspection MissingPermission
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private void showToastUnrecognizedLocationError() {
        finishActivityWithError(getString(R.string.unknown_location_error));
    }

    public void finishActivityWithError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LocationActivity.this.finish();
            }
        }, CommonUtilities.WAIT_LENGTH_LONG);
    }

    protected void showLoadingIcon() {
        if(progress == null){
            progress = new ProgressDialog(this);
        }
        progress.setTitle("");
        progress.setMessage(getString(R.string.loadingMsg));
        progress.show();
    }

    protected void hideLoadingIcon() {
        if(progress != null){
            progress.dismiss();
        }
    }
}
