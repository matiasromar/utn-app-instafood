package com.utnapp.instafood.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.utnapp.instafood.Fragments.ImagesGridFragment;
import com.utnapp.instafood.Fragments.LoginFragment;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ImagesGridFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener{

    private ProgressDialog progress;
    private View UIfragmentContainer;
    private ImagesGridFragment imagesGridFragment;
    private Toolbar toolbar;

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
            imagesGridFragment.GoToPublish();
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

    public void showLoadingIcon() {
        if(progress == null){
            progress = new ProgressDialog(this);
        }
        progress.setTitle("");
        progress.setMessage(getString(R.string.loadingMsg));
        progress.show();
    }

    public void hideLoadingIcon() {
        if(progress != null){
            progress.dismiss();
        }
    }
}
