package com.utnapp.instafood.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.utnapp.instafood.R;

public class ScreenSlidePagerFragment extends Fragment {
    public static final String ARG_IMAGE = "image";
    public static final String ARG_DESC = "description";

    private String description;
    private Bitmap image;

    public static ScreenSlidePagerFragment create(Bitmap image, String description) {
        ScreenSlidePagerFragment fragment = new ScreenSlidePagerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE, image);
        args.putString(ARG_DESC, description);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            image = getArguments().getParcelable(ARG_IMAGE);
            description = getArguments().getString(ARG_DESC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
        imageView.setImageBitmap(image);
        TextView descImage = (TextView) rootView.findViewById(R.id.descImage);
        descImage.setText(description);

        return rootView;
    }
}
