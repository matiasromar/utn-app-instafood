package com.utnapp.instafood.Managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.utnapp.instafood.Activities.MainActivity;
import com.utnapp.instafood.Db.DbContract;
import com.utnapp.instafood.Db.DbHelper;
import com.utnapp.instafood.Models.Publication;

import java.util.ArrayList;
import java.util.UUID;

public class LikesManager {
    private Context context;

    public LikesManager(Context context) {
        this.context = context;
    }

    public int getLikes(String publicationId) {
        if(this.existDataForPublication(publicationId)){
            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String[] projection = new String[4];

            projection[0] = DbContract.Likes.COLUMN_NAME_LIKES;

            String selection = DbContract.Likes.COLUMN_NAME_PUBLICATION_ID + "=?";

            String[] selectionArgs = {publicationId};

            String table = DbContract.Likes.getTableName();

            String groupBy = null;

            String having = null;

            String sortOrder = "";

            Cursor cursor = db.query(
                    table,
                    projection,
                    selection,
                    selectionArgs,
                    groupBy,
                    having,
                    sortOrder
            );

            int likes = 0;
            if (cursor.moveToFirst()) {
                do {
                    likes = cursor.getInt(0);
                    break;
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            dbHelper.close();

            return likes;
        } else {
            return 0;
        }
    }

    public void addLike(String publicationId) {
        if(this.existDataForPublication(publicationId)){
            int likes = getLikes(publicationId);

            String updateCmd = "UPDATE "+  DbContract.Likes.getTableName() + " SET " +
                    DbContract.Likes.COLUMN_NAME_LIKES + "=" + (likes + 1) +
                    " WHERE " +
                    DbContract.Likes.COLUMN_NAME_PUBLICATION_ID + "=" + "\'" + publicationId + "\'";

            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(updateCmd);
            db.close();
            dbHelper.close();
        } else {
            String insertCmd = "INSERT INTO "+  DbContract.Likes.getTableName() + " VALUES (" +
                    "NULL" + "," +
                    "\'" + publicationId + "\'" + "," +
                    "1" +
                    ");";

            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(insertCmd);
            db.close();
            dbHelper.close();
        }
    }

    private boolean existDataForPublication(String publicationId) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = new String[1];

        projection[0] = DbContract.Likes._ID;

        String selection = DbContract.Likes.COLUMN_NAME_PUBLICATION_ID + "=?";

        String[] selectionArgs = {publicationId};

        String table = DbContract.Likes.getTableName();

        String groupBy = null;

        String having = null;

        String sortOrder = "";

        Cursor cursor = db.query(
                table,
                projection,
                selection,
                selectionArgs,
                groupBy,
                having,
                sortOrder
        );

        return cursor.getCount() > 0;
    }

    public void removeLike(String publicationId) {
        if(this.existDataForPublication(publicationId)){
            int likes = getLikes(publicationId);

            if(likes == 0){
                return;
            }

            String updateCmd = "UPDATE "+  DbContract.Likes.getTableName() + " SET " +
                    DbContract.Likes.COLUMN_NAME_LIKES + "=" + (likes - 1) +
                    " WHERE " +
                    DbContract.Likes.COLUMN_NAME_PUBLICATION_ID + "=" + "\'" + publicationId + "\'";

            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(updateCmd);
            db.close();
            dbHelper.close();
        }
    }
}
