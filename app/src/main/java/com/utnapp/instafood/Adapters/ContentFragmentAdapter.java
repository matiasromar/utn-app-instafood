package com.utnapp.instafood.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.utnapp.instafood.Fragments.ImagesGridFragment;

/**
 * Created by mromar on 11/28/16.
 */

public class ContentFragmentAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 1;
    private final Context c;
    private String currentCity;

    public ContentFragmentAdapter(FragmentManager fragmentManager, Context context, int item_count) {
        super(fragmentManager);
        NUM_ITEMS = item_count;
        c = context;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return ImagesGridFragment.newInstance(ImagesGridFragment.VIEW_FEEDS, currentCity);
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Tab " + String.valueOf(position + 1);
    }
}

