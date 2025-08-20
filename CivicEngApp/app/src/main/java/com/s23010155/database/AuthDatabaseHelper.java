package com.s23010155.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AuthDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "civic_auth.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NIC = "nic";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_NIC + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public AuthDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_NIC + " TEXT");
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_nic ON " + TABLE_USERS + "(" + COLUMN_NIC + ")");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
            }
        }
    }

    public boolean registerUser(String email, String password, String nic) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NIC, nic);
        long rowId = -1;
        try {
            rowId = db.insert(TABLE_USERS, null, values);
        } catch (Exception ignored) {
        }
        return rowId != -1;
    }

    public boolean isUserExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null
        )) {
            return cursor != null && cursor.moveToFirst();
        }
    }

    public boolean validateCredentials(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{email, password},
                null,
                null,
                null
        )) {
            return cursor != null && cursor.moveToFirst();
        }
    }

    public boolean isNicExists(String nic) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_NIC + " = ?",
                new String[]{nic},
                null,
                null,
                null
        )) {
            return cursor != null && cursor.moveToFirst();
        }
    }
}


