package com.utnapp.instafood.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.Managers.PublicationsManager;
import com.utnapp.instafood.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PublishActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    private static final long LENGTH_LONG = 3500;
    private static final long LENGTH_SHORT = 2000;

    private static final long MIN_TIME_BW_UPDATES = 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50;

    private static final String SAVED_SELECTED_IMAGE_KEY = "SAVED_SELECTED_IMAGE_KEY";
    private static final String SAVED_CITY_KEY = "SAVED_CITY_KEY";

    private Bitmap selectedImage;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        if (savedInstanceState != null) {
            selectedImage = CommonUtilities.StringToBitMap(savedInstanceState.getString(SAVED_SELECTED_IMAGE_KEY));
            city = savedInstanceState.getString(SAVED_CITY_KEY);
        }

        if(selectedImage != null){
            setSelectedPicture();
        }

        if(city == null || city.isEmpty()){
            configureLocationServices();
        }
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

            setSelectedPicture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureLocationServices();
                } else {
                    Toast.makeText(this, "Tu ubicación es clave para nuestro servicio, lo sentimos pero no podemos publicar sin ella :( ", Toast.LENGTH_LONG).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PublishActivity.this.finish();
                        }
                    }, LENGTH_LONG);
                }
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(selectedImage != null)
        {
            savedInstanceState.putString(SAVED_SELECTED_IMAGE_KEY, CommonUtilities.BitMapToString(selectedImage));
        }
        savedInstanceState.putString(SAVED_CITY_KEY, city);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
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

    private String getCity(Location location) {
        if (location != null) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                return null;
            }

            if (addresses.size() > 0)
                return addresses.get(0).getLocality();
        }

        Toast.makeText(this, "Ha habido un error en conseguir su ubicación, por favor reintente nuevamente", Toast.LENGTH_SHORT).show();
        return null;
    }

    private void configureLocationServices() {
        Location location = null;

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Instafood necesita conocer tu ubicación para poder agregarla a tus publicaciones y compartirla a personas que estén cerca tuyo, ayudalos a disfrutar a ellos también lo copado que estas comiendo ;)", Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestLocationPermissions();
                    }
                }, LENGTH_LONG);
            } else {
                requestLocationPermissions();
            }
        } else {
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        city = getCity(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    }
                }
            }
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
