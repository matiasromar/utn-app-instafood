package com.utnapp.instafood;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.utnapp.instafood.Models.Publication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

public class ImagesManager {
    private Context context;

    public ImagesManager(Context context) {
        this.context = context;
    }

    public void saveImage(String description, String city, Bitmap image) {
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
    }

    public ArrayList<Publication> getImages() {
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
}
