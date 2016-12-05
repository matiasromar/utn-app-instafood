package com.utnapp.instafood.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.utnapp.instafood.Fragments.ScreenSlidePagerFragment;
import com.utnapp.instafood.Models.Publication;

import java.util.ArrayList;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<Publication> content;

    public ScreenSlidePagerAdapter(FragmentManager supportFragmentManager, ArrayList<Publication> content) {
        super(supportFragmentManager);

        this.content = content;
    }

    @Override
    public Fragment getItem(int position) {
        Publication publication = content.get(position);
        return ScreenSlidePagerFragment.create(publication);
    }

    @Override
    public int getCount() {
        return content.size();
    }
}