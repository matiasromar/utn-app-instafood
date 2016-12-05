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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utnapp.instafood.Adapters.ImageGridAdapter;
import com.utnapp.instafood.Adapters.ScreenSlidePagerAdapter;
import com.utnapp.instafood.Api.MyCallback;
import com.utnapp.instafood.JsonModels.PublicationJson;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

import java.io.IOException;
import java.util.ArrayList;

public class FeedsGridFragment extends Fragment {
    private static final String ARG_PARAM_CITY = "City";

    private String city;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Publication> content;
    private View mView;

    private View mainView;

    public FeedsGridFragment() {
    }

    public static FeedsGridFragment newInstance(String city) {
        FeedsGridFragment fragment = new FeedsGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_CITY, city);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            city = getArguments().getString(ARG_PARAM_CITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.feeds_grid, container, false);
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
        if(!mListener.isInternetAvailable()){
            Toast.makeText(getActivity(), "No se pueden mostrar los feeds sin conexión a internet.", Toast.LENGTH_SHORT).show();
        }

        mListener.showLoadingIcon("Cargando Feeds...");
        publicationsManager.getFeedsAsync(city, null, new MyCallback() {
            @Override
            public void success(String responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                PublicationJson[] publicationsJsons;
                try {
                    publicationsJsons = mapper.readValue(responseBody, PublicationJson[].class);
                } catch (IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorShowingFeeds();
                        }
                    });
                    return;
                }
                content = Publication.Map(publicationsJsons, getActivity());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    configureView();
                    mListener.hideLoadingIcon();
                    }
                });
            }

            @Override
            public void error(String responseBody) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    mListener.hideLoadingIcon();
                    showErrorShowingFeeds();
                    }
                });
            }

            @Override
            public void unhandledError(Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    mListener.hideLoadingIcon();
                    showErrorShowingFeeds();
                    }
                });
            }
        });
    }

    private void showErrorShowingFeeds() {
        Toast.makeText(getActivity(), "Ha ocurrido un error obteniendo los feeds", Toast.LENGTH_SHORT).show();
    }
}
