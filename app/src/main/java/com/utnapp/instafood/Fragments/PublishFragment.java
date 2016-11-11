package com.utnapp.instafood.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.utnapp.instafood.Api.MyCallback;
import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.R;

import java.io.File;
import java.io.FileNotFoundException;

public class PublishFragment extends Fragment {
    private static final String ARG_PARAM_CITY = "CITY";
    private OnFragmentInteractionListener mListener;

    private static final String SAVED_SELECTED_IMAGE_KEY = "SAVED_SELECTED_IMAGE_KEY";
    private static final String SAVED_CITY_KEY = "SAVED_CITY_KEY";

    private final String storageAccessExplanation = "Instafood necesita acceso a su almacenamiento para poder publicar la imagen seleccionada.";

    private Bitmap selectedImage;
    private String city;
    private boolean storagePermissionsGranted = false;

    private View myView;

    public PublishFragment() {
        // Required empty public constructor
    }


    public static PublishFragment newInstance(String city) {
        PublishFragment fragment = new PublishFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_CITY, city);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            city = getArguments().getString(ARG_PARAM_CITY);
        }

        if (!storagePermissionsGranted && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), storageAccessExplanation, Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestStoragePermissions();
                    }
                }, CommonUtilities.WAIT_LENGTH_LONG);
            } else {
                requestStoragePermissions();
            }
        } else {
            storagePermissionsGranted = true;
        }

        if (savedInstanceState != null) {
            selectedImage = CommonUtilities.StringToBitMap(savedInstanceState.getString(SAVED_SELECTED_IMAGE_KEY));
            city = savedInstanceState.getString(SAVED_CITY_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_publish, container, false);
        if (selectedImage != null) {
            setSelectedPicture();
        }
        return myView;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selectedImage != null) {
            savedInstanceState.putString(SAVED_SELECTED_IMAGE_KEY, CommonUtilities.BitMapToString(selectedImage));
        }
        savedInstanceState.putString(SAVED_CITY_KEY, city);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void publish(final View view) {
        view.setEnabled(false);
        final String description = ((EditText) myView.findViewById(R.id.description)).getText().toString();

        if (selectedImage == null) {
            Toast.makeText(getActivity(), "Por favor seleccione una imagen", Toast.LENGTH_LONG).show();
            return;
        }

        if (city == null) {
            Toast.makeText(getActivity(), getString(R.string.unknown_location_error), Toast.LENGTH_SHORT).show();
            return;
        }

        final PublicationsManager publicationsManager = new PublicationsManager(getActivity());
        mListener.showLoadingIcon();
        publicationsManager.saveImageAsync(description, city, selectedImage, new MyCallback() {
            @Override
            public void success(String responseBody) {
                publicationsManager.saveLocally(description, city, selectedImage);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                        mListener.hideLoadingIcon();
                        mListener.finishPublish();
                    }
                });
            }

            @Override
            public void error(String responseBody) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                        mListener.hideLoadingIcon();
                        Toast.makeText(getActivity(), "Ha ocurrido un error al intentar publicar. Por favor intente nuevamente.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void unhandledError(Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                        mListener.hideLoadingIcon();
                        Toast.makeText(getActivity(), "Ha ocurrido un error al intentar publicar. Por favor intente nuevamente.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void editPicture(View view) {
        view.setVisibility(View.GONE);
        myView.findViewById(R.id.selectImageContainer).setVisibility(View.VISIBLE);
        myView.findViewById(R.id.cancel_edit_button).setVisibility(View.VISIBLE);
    }

    public void cancelEditPicture(View view) {
        view.setVisibility(View.GONE);
        myView.findViewById(R.id.selectImageContainer).setVisibility(View.GONE);
        myView.findViewById(R.id.edit_button).setVisibility(View.VISIBLE);
    }

    public void handleStorageRequestResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            storagePermissionsGranted = true;
        } else {
            Toast.makeText(getActivity(), R.string.storage_key_to_service, Toast.LENGTH_LONG).show();
        }
    }

    public void handleImageSelectionRequest(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                selectedImage = getResizedImage(uri, 200, getActivity());
            } catch (FileNotFoundException e) {
                Toast.makeText(getActivity(), R.string.error_selecting_image, Toast.LENGTH_SHORT).show();
                return;
            }

            if(selectedImage != null){
                ImageView imageView = (ImageView) myView.findViewById(R.id.selectedImage);
                imageView.setImageBitmap(selectedImage);
            }

            setSelectedPicture();
        } else {
            Toast.makeText(getActivity(), R.string.error_selecting_image, Toast.LENGTH_SHORT).show();
        }
    }

    public void handleCameraRequest(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            File f = new File(Environment.getExternalStorageDirectory().toString());

            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;
                    break;
                }
            }

            if (!f.exists()) {
                Toast.makeText(getActivity(), R.string.error_capturing_picture, Toast.LENGTH_LONG).show();
            }

            try {
                selectedImage = getResizedImage(Uri.fromFile(f), 200, getActivity());

                if(selectedImage != null){
                    int rotate = 0;
                    try {
                        ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                        int orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotate = 270;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotate = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotate = 90;
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotate);
                    selectedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.error_capturing_picture, Toast.LENGTH_SHORT).show();
            }

            setSelectedPicture();
        } else {
            Toast.makeText(getActivity(), R.string.error_capturing_picture, Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnFragmentInteractionListener {
        void publish(View view);
        void cancelEditPicture(View view);
        void editPicture(View view);
        void selectFromGallery(View view);
        void capturePicture(View view);
        void requestStoragePermissions();
        void showLoadingIcon();
        void hideLoadingIcon();
        void finishPublish();
    }

    private void setSelectedPicture() {
        myView.findViewById(R.id.selectImageContainer).setVisibility(View.GONE);
        myView.findViewById(R.id.imageSelectedContainer).setVisibility(View.VISIBLE);
        myView.findViewById(R.id.cancel_edit_button).setVisibility(View.GONE);
        myView.findViewById(R.id.edit_button).setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) myView.findViewById(R.id.selectedImage);
        imageView.setImageBitmap(selectedImage);
    }

    private void requestStoragePermissions() {
        mListener.requestStoragePermissions();
    }

    private Bitmap getResizedImage(Uri uri, int requiredSize, Context context) throws FileNotFoundException {
        if(!storagePermissionsGranted){
            Toast.makeText(getActivity(), storageAccessExplanation, Toast.LENGTH_SHORT).show();
            return null;
        }

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < requiredSize
                    || height_tmp / 2 < requiredSize) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o2);
    }
}
