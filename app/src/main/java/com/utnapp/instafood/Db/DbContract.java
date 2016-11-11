package com.utnapp.instafood.Db;

import android.provider.BaseColumns;

public final class DbContract {
    public DbContract() {}

    public static abstract class Publications implements BaseColumns {
        public static String COLUMN_NAME_DESCRIPTION = "Description";
        public static String COLUMN_NAME_CITY = "City";
        public static String COLUMN_NAME_LOCAL_IMAGE = "LocalImageName";

        public static String getTableName() {
            return "Publications";
        }

        public static String getCreateTableSentence() {
            return "CREATE TABLE " + getTableName() + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    COLUMN_NAME_CITY + " TEXT NOT NULL, " +
                    COLUMN_NAME_LOCAL_IMAGE + " TEXT NOT NULL" + ")";
        }

        public static String getDropTableSentence() {
            return "DROP TABLE IF EXISTS " + getTableName();
        }

        public static String[] getInitTableSentences() {
            return new String[] {};
        }
    }

    public static abstract class Likes implements BaseColumns {
        public static String COLUMN_NAME_PUBLICATION_ID = "PublicationId";
        public static String COLUMN_NAME_LIKES = "Likes";

        public static String getTableName() {
            return "Likes";
        }

        public static String getCreateTableSentence() {
            return "CREATE TABLE " + getTableName() + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COLUMN_NAME_PUBLICATION_ID + " TEXT NOT NULL, " +
                    COLUMN_NAME_LIKES + " INT" + ")";
        }

        public static String getDropTableSentence() {
            return "DROP TABLE IF EXISTS " + getTableName();
        }

        public static String[] getInitTableSentences() {
            return new String[] {};
        }
    }
}
