package com.utnapp.instafood.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.utnapp.instafood.Fragments.ImagesGridFragment;
import com.utnapp.instafood.Fragments.ImagesGridFragment.OnFragmentInteractionListener;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

public class MainActivity extends BaseActivity implements OnFragmentInteractionListener{
    private View UIinitialView;
    private View UIfragmentContainer;
    private ImagesGridFragment imagesGridFragment;

    protected MainActivity() {
        super(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UIinitialView = findViewById(R.id.initialView);
        UIfragmentContainer = findViewById(R.id.fragment_container);
    }

    public void startLoginActivity(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void showPublished(View view) {
        UIinitialView.setVisibility(View.GONE);
        UIfragmentContainer.setVisibility(View.VISIBLE);

        imagesGridFragment = ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_FEEDS, "TESTING");

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, imagesGridFragment).commit();
    }

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
    public void hideLoadingIcon() {
        super.hideLoadingIcon();
    }
}
