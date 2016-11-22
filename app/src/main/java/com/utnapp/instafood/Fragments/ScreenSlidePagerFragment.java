package com.utnapp.instafood.Fragments;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.utnapp.instafood.Managers.LikesManager;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

public class ScreenSlidePagerFragment extends Fragment {
    public static final String ARG_PUBLICATION = "publication";

    private Publication item;

    public static ScreenSlidePagerFragment create(Publication publication) {
        ScreenSlidePagerFragment fragment = new ScreenSlidePagerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PUBLICATION, publication);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            item = getArguments().getParcelable(ARG_PUBLICATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
        imageView.setImageBitmap(item.image);
        TextView descImage = (TextView) rootView.findViewById(R.id.descImage);
        descImage.setText(item.description);
        TextView UIlocation = (TextView) rootView.findViewById(R.id.location);
        UIlocation.setText(item.city);

        final FloatingActionButton favBtn = (FloatingActionButton) rootView.findViewById(R.id.favBtn);
        if(item.liked){
            favBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.black));
        } else {
            favBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
        }

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikesManager likesManager = new LikesManager(getActivity());
                if(item.liked){
                    favBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                    likesManager.removeLike(item.id);
                    item.liked = false;
                } else {
                    favBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.black));
                    likesManager.addLike(item.id);
                    item.liked = true;
                }
            }
        });

        return rootView;
    }
}
