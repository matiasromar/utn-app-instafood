package com.utnapp.instafood.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.utnapp.instafood.ImagesManager;
import com.utnapp.instafood.R;

import java.io.File;
import java.io.FileNotFoundException;

public class PublishActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
    }

    public void Publish(View view) {
        String description = ((EditText)findViewById(R.id.description)).getText().toString();

        if(selectedImage == null){
            findViewById(R.id.errorMsg).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.errorMsg).setVisibility(View.GONE);
        }

        ImagesManager imagesManager = new ImagesManager(this);
        imagesManager.saveImage(description, getCity(), selectedImage);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private String getCity() {
        //TODO getCity
        return "";
    }

    public void selectFromGallery(View view) {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    public void capturePicture(View view) {
        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment
                .getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(f));

        startActivityForResult(intent,
                CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && (requestCode == PICK_IMAGE_REQUEST || requestCode == CAMERA_REQUEST)) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {

                Uri uri = data.getData();

                try {
                    selectedImage = getResizedImage(uri, 500, this);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Hubo un error al seleccionar la imagen", Toast.LENGTH_SHORT).show();
                    return;
                }

                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(selectedImage);
            }

            if (requestCode == CAMERA_REQUEST) {
                File f = new File(Environment.getExternalStorageDirectory().toString());

                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }

                if (!f.exists()) {
                    Toast.makeText(getBaseContext(), "Ha ocurrido un error sacando la foto", Toast.LENGTH_LONG).show();
                    return;
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
                    return;
                }
            }

            findViewById(R.id.selectImageContainer).setVisibility(View.GONE);
            findViewById(R.id.imageSelectedContainer).setVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
            imageView.setImageBitmap(selectedImage);
        }
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
