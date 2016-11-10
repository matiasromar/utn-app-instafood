package com.utnapp.instafood.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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

import com.google.android.gms.location.LocationRequest;
import com.utnapp.instafood.Fragments.ImagesGridFragment;
import com.utnapp.instafood.Fragments.LoginFragment;
import com.utnapp.instafood.Fragments.PublishFragment;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

import java.io.File;

public class MainActivity extends LocationActivity
        implements NavigationView.OnNavigationItemSelectedListener, ImagesGridFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener, PublishFragment.OnFragmentInteractionListener{

    private View UIfragmentContainer;

    private ImagesGridFragment imagesGridFragment;
    private PublishFragment publishFragment;

    private ProgressDialog progress;
    private Toolbar toolbar;

    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_CAMERA = 2;

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;

    protected MainActivity() {
        super(
                "Instafood necesita conocer tu ubicación para poder agregarla a tus publicaciones y compartirla a personas que estén cerca tuyo, ayudalos a disfrutar a ellos también lo copado que estas comiendo ;)",
                10000,
                5000,
                LocationRequest.PRIORITY_LOW_POWER
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UIfragmentContainer = findViewById(R.id.fragment_container);
        UIfragmentContainer.setVisibility(View.VISIBLE);
        setTitle(ImagesGridFragment.VIEW_FEEDS);
        imagesGridFragment = ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_FEEDS, "TESTING");
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, imagesGridFragment).commit();

        configureDrawer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
            LoginFragment fragment = LoginFragment.newInstance();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
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
        }
    }

    //REGION: ImagesGridFragment.OnFragmentInteractionListener
    @Override
    public void showPublication(Publication item) {
        //TODO showPublication
        Toast toast = Toast.makeText(this, "Pendiente fragment para mostrar imagen", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void showAddView(View view) {
        if(imagesGridFragment != null){
            //imagesGridFragment.GoToPublish();
            publishFragment = PublishFragment.newInstance();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, publishFragment)
                    .commit();
        }
    }

    @Override
    public void toggleView(View view) {
        if(imagesGridFragment != null){
            imagesGridFragment.toggleView();
        }
    }

    @Override
    public void changeTitle(String viewTitle) {
        setTitle(viewTitle);
    }
    //END_REGION: ImagesGridFragment.OnFragmentInteractionListener

    //REGION: LoginFragment.OnFragmentInteractionListener
    @Override
    public void finishLogin() {
        configureDrawer();
        setTitle(ImagesGridFragment.VIEW_MIS_PUBLICACIONES);
        imagesGridFragment = ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_MIS_PUBLICACIONES, "TESTING");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, imagesGridFragment).commit();
    }
    //END_REGION: LoginFragment.OnFragmentInteractionListener

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
        File f = new File(android.os.Environment
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
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, imagesGridFragment)
                .commit();

        publishFragment = null;
    }
    //END_REGION: PublishFragment.OnFragmentInteractionListener

    //Location Activity
    @Override
    public void onLocationChanged(Location location) {
        if(publishFragment != null){
            publishFragment.updateCity(getCity(location));
        }
    }
    //END - Location Activity

    @Override
    public void showLoadingIcon() {
        if(progress == null){
            progress = new ProgressDialog(this);
        }
        progress.setTitle("");
        progress.setMessage(getString(R.string.loadingMsg));
        progress.show();
    }

    @Override
    public void hideLoadingIcon() {
        if(progress != null){
            progress.dismiss();
        }
    }

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
}
