package com.utnapp.instafood.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.utnapp.instafood.Fragments.ImagesGridFragment;
import com.utnapp.instafood.Fragments.LoginFragment;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

public class MainActivity extends BaseActivity implements ImagesGridFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener{
    private View UIfragmentContainer;
    private ImagesGridFragment imagesGridFragment;

    protected MainActivity() {
        super(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UIfragmentContainer = findViewById(R.id.fragment_container);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        configureDrawer();

        UIfragmentContainer.setVisibility(View.VISIBLE);
        setTitle(ImagesGridFragment.VIEW_FEEDS);
        imagesGridFragment = ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_FEEDS, "TESTING");
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, imagesGridFragment).commit();
    }

    //REGION: DRAWER
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerOptions;

    private static final String LOGIN_OPTION = "Log In";
    private static final String LOGOUT_OPTION = "Log Out";

    private void configureDrawer() {
        SharedPreferences sharedPref = getSharedPreferences(this.getString(R.string.prefKey_UserSharedPreferences), Context.MODE_PRIVATE);

        String displayName = sharedPref.getString("user_displayName", null);
        if(displayName != null){
            mDrawerOptions = new String[]{ "Hola " + displayName + "!", LOGOUT_OPTION};
        } else {
            mDrawerOptions = new String[]{ LOGIN_OPTION  };
        }

        mDrawerList.setAdapter(new ArrayAdapter<>(this,R.layout.drawer_list_item, mDrawerOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        String option = mDrawerOptions[position];

        switch (option){
            case LOGIN_OPTION:
                LoginFragment fragment = LoginFragment.newInstance();
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                mDrawerList.setItemChecked(position, true);
                setTitle(mDrawerOptions[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case LOGOUT_OPTION:
                SharedPreferences sharedPref = getSharedPreferences(this.getString(R.string.prefKey_UserSharedPreferences), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("user_email", null);
                editor.putString("user_displayName", null);
                editor.putString("user_googleId", null);
                editor.putString("user_photoUrl", null);
                editor.commit();
                configureDrawer();
                break;
        }
    }
    //END_REGION: DRAWER

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
    public void showLoadingIcon() {
        super.showLoadingIcon();
    }

    @Override
    public void changeTitle(String viewTitle) {
        setTitle(viewTitle);
    }

    @Override
    public void hideLoadingIcon() {
        super.hideLoadingIcon();
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
}
