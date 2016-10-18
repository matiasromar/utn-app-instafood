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

import com.utnapp.instafood.Activities.PublishActivity;
import com.utnapp.instafood.Adapters.ImageGridAdapter;
import com.utnapp.instafood.ImagesManager;
import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

import java.util.ArrayList;

public class ImagesGridFragment extends Fragment {
    private static final String ARG_PARAM_VIEW = "view";
    
    public static final String VIEW_MIS_PUBLICACIONES = "Mis Publicaciones";
    private static final String VIEW_FEEDS = "Feeds";
    public static final int RESULT_PUBLISH = 1;

    private String viewType;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Publication> content;

    public ImagesGridFragment() {
    }

    public static ImagesGridFragment newInstance(String view) {
        ImagesGridFragment fragment = new ImagesGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_VIEW, view);
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
                Toast toast = Toast.makeText(getActivity(), "Ha ocurrido un error", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }

        content = getUpdatedContent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_images_grid, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        configureView();
    }

    private void configureView() {
        View UIerrorMsg = getView().findViewById(R.id.noContent);
        GridView UIgridView = (GridView) getView().findViewById(R.id.gridView);

        if (!content.isEmpty()) {
            UIerrorMsg.setVisibility(TextView.GONE);
            UIgridView.setVisibility(TextView.VISIBLE);

            configureGrid(UIgridView);
        } else {
            UIerrorMsg.setVisibility(TextView.VISIBLE);
            UIgridView.setVisibility(View.GONE);
        }

        View addBtn = getView().findViewById(R.id.addBtn);
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
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImagesGridFragment.RESULT_PUBLISH) {
            if (resultCode == Activity.RESULT_OK) {
                Update();
            }
        }
    }

    public void GoToPublish() {
        Intent intent = new Intent(getActivity(), PublishActivity.class);
        startActivityForResult(intent, RESULT_PUBLISH);
    }

    public void Update() {
        content = getUpdatedContent();

        configureView();
    }

    private ArrayList<Publication> getUpdatedContent() {
        if(viewType.endsWith(VIEW_MIS_PUBLICACIONES)){
            ImagesManager imagesManager = new ImagesManager(getActivity());
            return imagesManager.getImages();
        } else {
            return new ArrayList<>();
        }
    }

    public interface OnFragmentInteractionListener {
        void showPublication(Publication item);
        void showAddView(View view);
    }
}
