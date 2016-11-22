package com.utnapp.instafood.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import com.utnapp.instafood.Api.MyCallback;
import com.utnapp.instafood.JsonModels.PublicationJson;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.Models.Publication;
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

    private View mainView;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

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
            city = getArguments().getString(ARG_PARAM_CITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_images_grid, container, false);
        mainView = mView.findViewById(R.id.mainView);
        mPager = (ViewPager) mView.findViewById(R.id.pager);
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
        } else {
            viewType = VIEW_MIS_PUBLICACIONES;
        }
        getUpdatedContent();
    }

    public void UpdateContent() {
        getUpdatedContent();
    }

    public boolean isShowingSlider() {
        return mPager.getVisibility() == View.VISIBLE;
    }

    public void closeSlider() {
        mPager.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
        mPager.setAdapter(null);
        getUpdatedContent();
    }

    public interface OnFragmentInteractionListener {
        void showAddView(View view);
        void toggleView(View view);
        void hideLoadingIcon();
        void showLoadingIcon(String message);
        void changeTitle(String viewTitle);
        boolean isInternetAvailable();
    }

    private void configureView() {
        View UIerrorMsg = mView.findViewById(R.id.noContent);
        GridView UIgridView = (GridView) mView.findViewById(R.id.gridView);

        if (content != null && !content.isEmpty()) {
            UIerrorMsg.setVisibility(TextView.GONE);
            UIgridView.setVisibility(TextView.VISIBLE);

            configureGrid(UIgridView);
            configureSliderView();
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
                mainView.setVisibility(View.GONE);
                mPager.setVisibility(View.VISIBLE);
                mPager.setCurrentItem(position);
            }
        });
    }

    private void getUpdatedContent() {
        final PublicationsManager publicationsManager = new PublicationsManager(getActivity());
        if(!mListener.isInternetAvailable()){
            if(viewType.equals(VIEW_FEEDS)){
                Toast.makeText(getActivity(), "No se pueden mostrar los feeds sin conexión a internet.", Toast.LENGTH_SHORT).show();
            }
            viewType = VIEW_MIS_PUBLICACIONES;
        }

        if(viewType.endsWith(VIEW_MIS_PUBLICACIONES)){
            mListener.showLoadingIcon("Cargando Publicaciones...");
            mListener.changeTitle(VIEW_MIS_PUBLICACIONES);
            content = publicationsManager.getLocalImages();
            configureView();
            mListener.hideLoadingIcon();
        } else {
            mListener.showLoadingIcon("Cargando Feeds...");
            mListener.changeTitle(VIEW_FEEDS);
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
    }

    private void configureSliderView() {
        mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActivity().invalidateOptionsMenu();
            }
        });
    }

    private void showErrorShowingFeeds() {
        Toast.makeText(getActivity(), "Ha ocurrido un error obteniendo los feeds", Toast.LENGTH_SHORT).show();
        toggleView();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
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
}
