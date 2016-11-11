package com.utnapp.instafood.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utnapp.instafood.Adapters.ImageGridAdapter;
import com.utnapp.instafood.Api.MyCallback;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.JsonModels.PublicationJson;
import com.utnapp.instafood.R;

import java.io.IOException;
import java.util.ArrayList;

public class ImagesGridFragment extends Fragment {
    private static final String ARG_PARAM_VIEW = "view";
    private static final String ARG_PARAM_CITY = "City";

    public static final String VIEW_MIS_PUBLICACIONES = "Mis Publicaciones";
    public static final String VIEW_FEEDS = "Feeds";

    private String viewType;
    private String city;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Publication> content;
    private View mView;

    public ImagesGridFragment() {
    }

    public static ImagesGridFragment newInstance(String view, String city) {
        ImagesGridFragment fragment = new ImagesGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_VIEW, view);
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
            viewType = getArguments().getString(ARG_PARAM_VIEW);
            
            if(!viewType.equals(VIEW_MIS_PUBLICACIONES) && !viewType.equals(VIEW_FEEDS)){
                Toast.makeText(getActivity(), "Ha ocurrido un error", Toast.LENGTH_LONG).show();
                return;
            }
            city = getArguments().getString(ARG_PARAM_CITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_images_grid, container, false);
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

    public void toggleView() {
        if(viewType.equals(VIEW_MIS_PUBLICACIONES)){
            viewType = VIEW_FEEDS;
            mListener.changeTitle(VIEW_FEEDS);
        } else {
            viewType = VIEW_MIS_PUBLICACIONES;
            mListener.changeTitle(VIEW_MIS_PUBLICACIONES);
        }
        getUpdatedContent();
    }

    public void UpdateContent(String view) {
        if(view.equals(VIEW_MIS_PUBLICACIONES)){
            viewType = VIEW_MIS_PUBLICACIONES;
            mListener.changeTitle(VIEW_MIS_PUBLICACIONES);
        } else {
            viewType = VIEW_FEEDS;
            mListener.changeTitle(VIEW_FEEDS);
        }
        getUpdatedContent();
    }

    public interface OnFragmentInteractionListener {
        void showPublication(Publication item);
        void showAddView(View view);
        void toggleView(View view);
        void hideLoadingIcon();
        void showLoadingIcon();
        void changeTitle(String viewTitle);
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

        View addBtn = mView.findViewById(R.id.addBtn);
        if(viewType.equals(VIEW_MIS_PUBLICACIONES)){
            addBtn.setVisibility(View.VISIBLE);
        } else {
            addBtn.setVisibility(View.GONE);
        }
    }

    private void configureGrid(GridView UIgridView) {
        ImageGridAdapter gridAdapter = new ImageGridAdapter(getActivity(), R.layout.layout_grid_image, content);
        UIgridView.setAdapter(gridAdapter);
        UIgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Publication item = (Publication) parent.getItemAtPosition(position);
                mListener.showPublication(item);
                if(v.findViewById(R.id.like).getVisibility() == View.VISIBLE){
                    v.findViewById(R.id.like).setVisibility(View.GONE);
                } else {
                    v.findViewById(R.id.like).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getUpdatedContent() {
        mListener.showLoadingIcon();
        final PublicationsManager publicationsManager = new PublicationsManager(getActivity());
        if(viewType.endsWith(VIEW_MIS_PUBLICACIONES)){
            content = publicationsManager.getLocalImages();
            configureView();
            mListener.hideLoadingIcon();
        } else {
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
                            showErrorShowingContent();
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
                        showErrorShowingContent();
                        }
                    });
                }

                @Override
                public void unhandledError(Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        mListener.hideLoadingIcon();
                        showErrorShowingContent();
                        }
                    });
                }
            });
        }
    }

    private void showErrorShowingContent() {
        Toast.makeText(getActivity(), "Ha ocurrido un error obteniendo los feeds", Toast.LENGTH_SHORT).show();
        toggleView();
    }
}
