package com.s23010155.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IssueDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "civic_issues.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_ISSUES = "issues";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DISTRICT = "district";
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_PHOTO_URI = "photo_uri";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ISSUES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DISTRICT + " TEXT, " +
                    COLUMN_LOCATION_NAME + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_PHOTO_URI + " TEXT, " +
                    COLUMN_STATUS + " TEXT DEFAULT 'Pending', " +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public IssueDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ISSUES + " ADD COLUMN " + COLUMN_DISTRICT + " TEXT");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ISSUES);
                onCreate(db);
            }
        }
    }
} 