package com.s23010155.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PollDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "civic_polls.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_GOV_PERF = "gov_performance_votes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NIC = "nic";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month"; // 1-12
    public static final String COLUMN_VOTE = "vote"; // 1 = Good, -1 = Bad
    public static final String COLUMN_DISTRICT = "district";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String SQL_CREATE_PERF =
            "CREATE TABLE " + TABLE_GOV_PERF + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NIC + " TEXT NOT NULL, " +
                    COLUMN_YEAR + " INTEGER NOT NULL, " +
                    COLUMN_MONTH + " INTEGER NOT NULL, " +
                    COLUMN_VOTE + " INTEGER NOT NULL, " +
                    COLUMN_DISTRICT + " TEXT, " +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (" + COLUMN_NIC + ", " + COLUMN_YEAR + ", " + COLUMN_MONTH + ") ON CONFLICT REPLACE" +
                    ");";

    public PollDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PERF);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_GOV_PERF + " ADD COLUMN " + COLUMN_DISTRICT + " TEXT");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOV_PERF);
                onCreate(db);
            }
        }
    }

    public void upsertGovPerfVote(String nic, int year, int month, int vote, String district) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NIC, nic);
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_VOTE, vote);
        values.put(COLUMN_DISTRICT, district);
        db.insertWithOnConflict(TABLE_GOV_PERF, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int getUserVote(String nic, int year, int month) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_GOV_PERF, new String[]{COLUMN_VOTE},
                COLUMN_NIC + " = ? AND " + COLUMN_YEAR + " = ? AND " + COLUMN_MONTH + " = ?",
                new String[]{nic, String.valueOf(year), String.valueOf(month)}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        }
    }

    public int[] getMonthTotals(int year, int month) {
        SQLiteDatabase db = getReadableDatabase();
        int likes = 0;
        int dislikes = 0;
        try (Cursor c = db.query(TABLE_GOV_PERF, new String[]{"SUM(CASE WHEN " + COLUMN_VOTE + " = 1 THEN 1 ELSE 0 END)",
                "SUM(CASE WHEN " + COLUMN_VOTE + " = -1 THEN 1 ELSE 0 END)"},
                COLUMN_YEAR + " = ? AND " + COLUMN_MONTH + " = ?",
                new String[]{String.valueOf(year), String.valueOf(month)}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                likes = c.getInt(0);
                dislikes = c.getInt(1);
            }
        }
        return new int[]{likes, dislikes};
    }

    public java.util.Map<String, int[]> getDistrictTotals(int year, int month) {
        SQLiteDatabase db = getReadableDatabase();
        java.util.Map<String, int[]> map = new java.util.HashMap<>();
        String sql = "SELECT " + COLUMN_DISTRICT + ", " +
                "SUM(CASE WHEN " + COLUMN_VOTE + " = 1 THEN 1 ELSE 0 END) AS good, " +
                "SUM(CASE WHEN " + COLUMN_VOTE + " = -1 THEN 1 ELSE 0 END) AS bad " +
                "FROM " + TABLE_GOV_PERF + " WHERE " + COLUMN_YEAR + "=? AND " + COLUMN_MONTH + "=? " +
                "AND " + COLUMN_DISTRICT + " IS NOT NULL GROUP BY " + COLUMN_DISTRICT;
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(year), String.valueOf(month)})) {
            while (c != null && c.moveToNext()) {
                String district = c.getString(0);
                int good = c.getInt(1);
                int bad = c.getInt(2);
                map.put(district, new int[]{good, bad});
            }
        }
        return map;
    }
}


