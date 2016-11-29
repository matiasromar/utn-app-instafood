package com.utnapp.instafood.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
import com.utnapp.instafood.Fragments.ImagesGridFragment;
import com.utnapp.instafood.Adapters.ContentFragmentAdapter;
import com.utnapp.instafood.Fragments.PublishFragment;
import com.utnapp.instafood.R;
import com.utnapp.instafood.SlidingTab.SlidingTabLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ImagesGridFragment.OnFragmentInteractionListener, PublishFragment.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private boolean requestingLocationUpdates = false;
    private boolean locationPermissionsGranted = false;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private PublishFragment publishFragment;

    private ProgressDialog progress;
    private Toolbar toolbar;

    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 3;

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;

    private String currentCity;

    ContentFragmentAdapter adapterViewPager;

    View fragmentContainer;
    ViewPager vpPager;
    SlidingTabLayout slidingTabLayout;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //PublicationsManager m = new PublicationsManager(this);
        //m.deleteFeeds();

        checkLocationPermissions();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (locationRequest == null) {
            locationRequest = createLocationRequestAndValidatesSettings();
        }

        configureDrawer();

        fragmentContainer = findViewById(R.id.fragment_container);
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mPager = (ViewPager) findViewById(R.id.pager);
    }

    @Override
    protected void onStart() {
        if (googleApiClient != null && (currentCity == null || currentCity.isEmpty())) {
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocationUpdates();

        configureDrawer();
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
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(adapterViewPager != null && adapterViewPager.isShowingSlider()){
                adapterViewPager.closeSlider();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(publishFragment != null && requestCode == REQUEST_CAMERA){
            publishFragment.handleCameraRequest(resultCode, data);
            return;
        }
        if(publishFragment != null && requestCode ==REQUEST_PICK_IMAGE){
            publishFragment.handleImageSelectionRequest(resultCode, data);
            return;
        }

        if(requestCode == REQUEST_CHECK_LOCATION_SETTINGS){
            if(resultCode == Activity.RESULT_OK ){
                requestingLocationUpdates = true;
                initLocationUpdates();
            } else {
                showError(getString(R.string.location_key_to_service));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if(publishFragment != null){
                    publishFragment.handleStorageRequestResult(grantResults);
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionsGranted = true;
                    initLocationUpdates();
                } else {
                    showError(getString(R.string.location_key_to_service));
                }
                break;
            }
        }
    }

    @Override
    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //REGION: Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//TODO Por si hay settings contextuales
//        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            Toast.makeText(this, "Settings Pending", Toast.LENGTH_SHORT).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            if(this.isInternetAvailable()){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(intent);
            } else  {
                Toast.makeText(this, "No puede ingresar a su cuenta sin conexión. Reintente más tarde.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_logout) {
            SharedPreferences sharedPref = getSharedPreferences(this.getString(R.string.prefKey_UserSharedPreferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user_email", null);
            editor.putString("user_displayName", null);
            editor.putString("user_googleId", null);
            editor.putString("user_photoUrl", null);
            editor.commit();
            configureDrawer();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //END-REGION: Menu

    //REGION Location
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
                showError(getString(R.string.google_serv_need_update));
                break;
            case ConnectionResult.NETWORK_ERROR:
                showError(getString(R.string.check_internet_connection));
                break;
            default:
                showToastUnrecognizedLocationError();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.hideLoadingIcon();
        currentCity = getCity(location);
        if(currentCity != null && !currentCity.isEmpty()){
            googleApiClient.disconnect();
            enableFeeds();
        }
    }
    //END-REGION Location

    //REGION: ImagesGridFragment.OnFragmentInteractionListener
    @Override
    public void showAddView(View view) {
        if(adapterViewPager != null){
            if(!this.isInternetAvailable()){
                Toast.makeText(this, "No se pueden hacer publicaciones sin conexión a internet.", Toast.LENGTH_SHORT).show();
                return;
            }

            slidingTabLayout.setVisibility(View.GONE);
            vpPager.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);

            publishFragment = PublishFragment.newInstance(currentCity);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, publishFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    //END_REGION: ImagesGridFragment.OnFragmentInteractionListener

    //REGION: PublishFragment.OnFragmentInteractionListener
    @Override
    public void publish(View view) {
        publishFragment.publish(view);
    }

    @Override
    public void cancelEditPicture(View view) {
        publishFragment.cancelEditPicture(view);
    }

    @Override
    public void editPicture(View view) {
        publishFragment.editPicture(view);
    }

    @Override
    public void selectFromGallery(View view) {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), REQUEST_PICK_IMAGE);
    }

    @Override
    public void capturePicture(View view) {
        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment
                .getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(f));

        startActivityForResult(intent,
                REQUEST_CAMERA);
    }

    @Override
    public void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_STORAGE);
    }

    @Override
    public void finishPublish() {
        publishFragment = null;
        adapterViewPager.UpdateContent();

        slidingTabLayout.setVisibility(View.VISIBLE);
        vpPager.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
    }
    //END_REGION: PublishFragment.OnFragmentInteractionListener

    //REGION - Loading
    @Override
    public void showLoadingIcon(String message) {
        if(progress == null){
            progress = new ProgressDialog(this);
        }
        progress.setTitle("");
        progress.setMessage(message);
        progress.show();
    }

    @Override
    public void hideLoadingIcon() {
        if(progress != null){
            progress.dismiss();
        }
    }
    //ENDREGION - Loading

    //REGION - Private stuff
    private void configureDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sharedPref = getSharedPreferences(this.getString(R.string.prefKey_UserSharedPreferences), Context.MODE_PRIVATE);
        String displayName = sharedPref.getString("user_displayName", null);

        View headerView = navigationView.getHeaderView(0);
        Menu menu = navigationView.getMenu();
        TextView UIname = (TextView) headerView.findViewById(R.id.name);
        MenuItem UIlogin = menu.findItem(R.id.nav_login);
        MenuItem UIlogout = menu.findItem(R.id.nav_logout);
        if(displayName != null){
            UIname.setText("Hola " + displayName + "!");
            UIname.setVisibility(View.VISIBLE);
            UIlogout.setVisible(true);
            UIlogin.setVisible(false);
        } else {
            UIname.setVisibility(View.GONE);
            UIlogin.setVisible(true);
            UIlogout.setVisible(false);
        }
    }

    private String getCity(Location location) {
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
                String locationAccessExplanation = "Instafood necesita conocer tu ubicación para poder agregarla a tus publicaciones y compartirla a personas que estén cerca tuyo, ayudalos a disfrutar a ellos también lo copado que estas comiendo ;)";
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
        long locationRequestInterval = 10000;
        newLocationRequest.setInterval(locationRequestInterval);
        long locationRequestFastestInterval = 5000;
        newLocationRequest.setFastestInterval(locationRequestFastestInterval);
        int locationRequestPriority = LocationRequest.PRIORITY_LOW_POWER;
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
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_LOCATION_SETTINGS);
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
            //JUAN - PARA PODER EMULAR - NO BORRAR NI MODIFICAR
            //this.showLoadingIcon("Obteniendo ubicación...");
            //noinspection MissingPermission
            //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            currentCity = "TESTING";
            googleApiClient.disconnect();
            enableFeeds();
            //JUAN - FIN - PARA PODER EMULAR - NO BORRAR NI MODIFICAR
        }
    }

    private void showToastUnrecognizedLocationError() {
        showError(getString(R.string.unknown_location_error));
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void enableFeeds() {
        adapterViewPager = new ContentFragmentAdapter(getSupportFragmentManager(), this, currentCity);
        vpPager.setAdapter(adapterViewPager);

        slidingTabLayout.setDistributeEvenly();
        slidingTabLayout.setViewPager(vpPager);
        slidingTabLayout.setTabSelected(0);
    }
}
