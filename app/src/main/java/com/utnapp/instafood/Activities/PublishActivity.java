package com.utnapp.instafood.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.R;

import java.io.File;
import java.io.FileNotFoundException;

public class PublishActivity extends LocationActivity {

    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_CAMERA = 2;

    private static final String SAVED_SELECTED_IMAGE_KEY = "SAVED_SELECTED_IMAGE_KEY";
    private static final String SAVED_CITY_KEY = "SAVED_CITY_KEY";

    private Bitmap selectedImage;
    private String city;

    protected PublishActivity() {
        super(
                "Instafood necesita conocer tu ubicación para poder agregarla a tus publicaciones y compartirla a personas que estén cerca tuyo, ayudalos a disfrutar a ellos también lo copado que estas comiendo ;)",
                10000,
                5000,
                LocationRequest.PRIORITY_LOW_POWER
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        if (savedInstanceState != null) {
            selectedImage = CommonUtilities.StringToBitMap(savedInstanceState.getString(SAVED_SELECTED_IMAGE_KEY));
            city = savedInstanceState.getString(SAVED_CITY_KEY);
        }

        if (selectedImage != null) {
            setSelectedPicture();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (handleImageSelectionRequest(requestCode, resultCode, data)) return;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selectedImage != null) {
            savedInstanceState.putString(SAVED_SELECTED_IMAGE_KEY, CommonUtilities.BitMapToString(selectedImage));
        }
        savedInstanceState.putString(SAVED_CITY_KEY, city);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onLocationChanged(Location location) {
        city = getCity(location);
    }

    public void publish(View view) {
        final String description = ((EditText) findViewById(R.id.description)).getText().toString();

        if (selectedImage == null) {
            findViewById(R.id.errorMsg).setVisibility(View.VISIBLE);
            return;
        } else {
            findViewById(R.id.errorMsg).setVisibility(View.GONE);
        }

        if (city == null) {
            Toast.makeText(this, "Ha ocurrido un error al obtener su ubicación, por favor intente nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        final PublicationsManager publicationsManager = new PublicationsManager(this);
//        publicationsManager.saveImage(description, city, selectedImage, new MyCallback() {
//            @Override
//            public void success(String responseBody) {
        publicationsManager.saveLocally(description, city, selectedImage);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
//            }
//
//            @Override
//            public void error(String responseBody) {
//                Toast.makeText(PublishActivity.this, "Ha ocurrido un error al intentar publicar. Por favor intente nuevamente.", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void unhandledError(Exception e) {
//                Toast.makeText(PublishActivity.this, "Ha ocurrido un error al intentar publicar. Por favor intente nuevamente.", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public void selectFromGallery(View view) {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), REQUEST_PICK_IMAGE);
    }

    public void capturePicture(View view) {
        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment
                .getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(f));

        startActivityForResult(intent,
                REQUEST_CAMERA);
    }

    public void editPicture(View view) {
        view.setVisibility(View.GONE);
        findViewById(R.id.selectImageContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel_edit_button).setVisibility(View.VISIBLE);
    }

    public void cancelEditPicture(View view) {
        view.setVisibility(View.GONE);
        findViewById(R.id.selectImageContainer).setVisibility(View.GONE);
        findViewById(R.id.edit_button).setVisibility(View.VISIBLE);
    }

    private boolean handleImageSelectionRequest(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && (requestCode == REQUEST_PICK_IMAGE || requestCode == REQUEST_CAMERA)) {
            if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {

                Uri uri = data.getData();

                try {
                    selectedImage = getResizedImage(uri, 500, this);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Hubo un error al seleccionar la imagen", Toast.LENGTH_SHORT).show();
                    return true;
                }

                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(selectedImage);
            }

            if (requestCode == REQUEST_CAMERA) {
                File f = new File(Environment.getExternalStorageDirectory().toString());

                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }

                if (!f.exists()) {
                    Toast.makeText(getBaseContext(), "Ha ocurrido un error sacando la foto", Toast.LENGTH_LONG).show();
                    return true;
                }

                try {
                    selectedImage = getResizedImage(Uri.fromFile(f), 500, this);

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
                } catch (Exception e) {
                    Toast.makeText(this, "Hubo un error al tomar la fotografía", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            setSelectedPicture();
        }
        return false;
    }

    private void setSelectedPicture() {
        findViewById(R.id.selectImageContainer).setVisibility(View.GONE);
        findViewById(R.id.imageSelectedContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel_edit_button).setVisibility(View.GONE);
        findViewById(R.id.edit_button).setVisibility(View.VISIBLE);
        findViewById(R.id.errorMsg).setVisibility(View.GONE);
        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
        imageView.setImageBitmap(selectedImage);
    }

    private Bitmap getResizedImage(Uri uri, int requiredSize, Context context) throws FileNotFoundException {
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
