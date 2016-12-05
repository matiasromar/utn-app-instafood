package com.utnapp.instafood.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.utnapp.instafood.Adapters.ImageGridAdapter;
import com.utnapp.instafood.Adapters.ScreenSlidePagerAdapter;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

import java.util.ArrayList;

public class MisPublicacionesGridFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ArrayList<Publication> content;
    private View mView;

    private View mainView;

    public static MisPublicacionesGridFragment newInstance() {
        MisPublicacionesGridFragment fragment = new MisPublicacionesGridFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.mis_publicaciones_grid, container, false);
        mainView = mView.findViewById(R.id.mainView);
        getUpdatedContent();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        configureView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void UpdateContent() {
        getUpdatedContent();
    }

    public interface OnFragmentInteractionListener {
        void showAddView(View view);
        void hideLoadingIcon();
        void showLoadingIcon(String message);
        boolean isInternetAvailable();
        void showSlider(ArrayList<Publication> content, int position);
    }

    private void configureView() {
        View UIerrorMsg = mView.findViewById(R.id.noContent);
        GridView UIgridView = (GridView) mView.findViewById(R.id.gridView);

        if (content != null && !content.isEmpty()) {
            UIerrorMsg.setVisibility(TextView.GONE);
            UIgridView.setVisibility(TextView.VISIBLE);

            configureGrid(UIgridView);
        } else {
            UIerrorMsg.setVisibility(TextView.VISIBLE);
            UIgridView.setVisibility(View.GONE);
        }
    }

    private void configureGrid(GridView UIgridView) {
        ImageGridAdapter gridAdapter = new ImageGridAdapter(getActivity(), R.layout.layout_grid_image, content);
        UIgridView.setAdapter(gridAdapter);
        UIgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                mListener.showSlider(content, position);
            }
        });
    }

    private void getUpdatedContent() {
        final PublicationsManager publicationsManager = new PublicationsManager(getActivity());

        mListener.showLoadingIcon("Cargando Publicaciones...");
        content = publicationsManager.getLocalImages();
        configureView();
        mListener.hideLoadingIcon();
    }
}
