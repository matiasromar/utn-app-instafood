package com.utnapp.instafood.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.utnapp.instafood.Fragments.FeedsGridFragment;
import com.utnapp.instafood.Fragments.MisPublicacionesGridFragment;

public class ContentFragmentAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 1;

    private final FeedsGridFragment feeds;
    private final MisPublicacionesGridFragment misPublicaciones;

    public ContentFragmentAdapter(FragmentManager fragmentManager, Context context, String currentCity) {
        super(fragmentManager);
        NUM_ITEMS = 2;

        feeds = FeedsGridFragment.newInstance(currentCity);
        misPublicaciones = MisPublicacionesGridFragment.newInstance();
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
}

