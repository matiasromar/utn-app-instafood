package com.utnapp.instafood.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.utnapp.instafood.Fragments.ImagesGridFragment;

public class ContentFragmentAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 1;
    private final Context c;

    private final ImagesGridFragment feeds;
    private final ImagesGridFragment misPublicaciones;

    private String currentCity;

    public ContentFragmentAdapter(FragmentManager fragmentManager, Context context, String currentCity) {
        super(fragmentManager);
        NUM_ITEMS = 2;
        this.currentCity = currentCity;
        c = context;

        feeds = ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_FEEDS, currentCity);
        misPublicaciones = ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_MIS_PUBLICACIONES, currentCity);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
       if(position == 1) {
           return misPublicaciones;
        } else {
           return feeds;
       }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 1) {
            return "Mis Publicaciones";
        } else {
            return "FEEDS";
        }
    }

    public void UpdateContent() {
        feeds.UpdateContent();
        misPublicaciones.UpdateContent();
    }

    public void closeSlider() {
        if(feeds.isShowingSlider()){
            feeds.closeSlider();
        } else {
            if(misPublicaciones.isShowingSlider()){
                misPublicaciones.closeSlider();
            }
        }
    }

    public boolean isShowingSlider() {
        return feeds.isShowingSlider() || misPublicaciones.isShowingSlider();
    }
}

