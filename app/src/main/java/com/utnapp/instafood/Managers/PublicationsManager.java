package com.utnapp.instafood.Managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.utnapp.instafood.Api.Api;
import com.utnapp.instafood.Api.MyCallback;
import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.Db.DbContract;
import com.utnapp.instafood.Db.DbHelper;
import com.utnapp.instafood.Models.Publication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.Request;

public class PublicationsManager {
    private Context context;
    private Api api;

    public PublicationsManager(Context context) {
        this.context = context;
        this.api = new Api(context);
    }

    public void saveImageAsync(final String description, final String city, final Bitmap image, MyCallback callback) {
        String relativeUrl = "upload-image";
        String jsonContent = "{" +
                    "\"picture\":" + "\"" + CommonUtilities.BitMapToString(image)+ "\"" + "," +
                    "\"description\":" + "\"" + description + "\"" + "," +
                    "\"city\":" + "\"" + city + "\"" +
                "}";

        Request request = api.getPostRequest(relativeUrl, jsonContent, false);

        api.executeAsyncCall(request, false, callback);
    }

    public void saveLocally(String description, String city, Bitmap image) {
        String fileName = UUID.randomUUID().toString();

        saveImageToFile(fileName, image);

        String insertCmd = "INSERT INTO "+  DbContract.Publications.getTableName() + " VALUES (" +
                "NULL" + "," +
                "\'" + description + "\'" + "," +
                "\'" + city + "\'" + "," +
                "\'" + fileName + "\'" +
                ");";

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(insertCmd);
        db.close();
        dbHelper.close();
    }

    public ArrayList<Publication> getLocalImages() {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = new String[4];

        projection[0] = DbContract.Publications._ID;
        projection[1] = DbContract.Publications.COLUMN_NAME_DESCRIPTION;
        projection[2] = DbContract.Publications.COLUMN_NAME_CITY;
        projection[3] = DbContract.Publications.COLUMN_NAME_LOCAL_IMAGE;

        String selection = "";

        String[] selectionArgs = {};

        String table = DbContract.Publications.getTableName();

        String groupBy = null;

        String having = null;

        String sortOrder = DbContract.Publications.COLUMN_NAME_DESCRIPTION;

        Cursor cursor = db.query(
                table,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder
        );

        ArrayList<Publication> publications = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Publication publication = new Publication();
                publication.description = cursor.getString(1);
                publication.image = readImageFromFile(cursor.getString(3));
                publication.city = cursor.getString(2);

                if(publication.image != null){
                    publications.add(publication);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        dbHelper.close();

        return publications;
    }

    private void saveImageToFile(String fileName, Bitmap image) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(CommonUtilities.BitMapToString(image));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Toast.makeText(context, "Hubo un error al tomar la fotografía", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap readImageFromFile(String fileName) {
        try {
            FileInputStream openedFile = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(openedFile);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            return CommonUtilities.StringToBitMap(sb.toString());
        } catch (IOException e) {
            return null;
        }
    }

    public void getFeedsAsync(String city, String lastImageId, MyCallback callback) {
        String relativeUrl = "get-images?city=" + city;
//        if(lastImageId != null && !lastImageId.isEmpty()){
//            relativeUrl += "&lastImageId=" + lastImageId;
//        }

        Request request = api.getGetRequest(relativeUrl, false);

        api.executeAsyncCall(request, false, callback);
    }

    public void deleteFeeds() {
        String relativeUrl = "feeds";

        Request request = api.getDeleteRequest("", relativeUrl, false);

        api.executeAsyncCall(request, false, new MyCallback() {
            @Override
            public void success(String responseBody) {

            }

            @Override
            public void error(String responseBody) {

            }

            @Override
            public void unhandledError(Exception e) {

            }
        });
    }
}
