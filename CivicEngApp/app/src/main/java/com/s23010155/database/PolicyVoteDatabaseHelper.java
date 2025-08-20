package com.s23010155.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PolicyVoteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "civic_policy_votes.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_POLICY_VOTES = "policy_votes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POLICY_ID = "policy_id"; // string id
    public static final String COLUMN_NIC = "nic";
    public static final String COLUMN_VOTE = "vote"; // 1=Agree, -1=Disagree
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_POLICY_VOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_POLICY_ID + " TEXT NOT NULL, " +
                    COLUMN_NIC + " TEXT NOT NULL, " +
                    COLUMN_VOTE + " INTEGER NOT NULL, " +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE(" + COLUMN_POLICY_ID + "," + COLUMN_NIC + ") ON CONFLICT REPLACE" +
                    ");";

    public PolicyVoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLICY_VOTES);
        onCreate(db);
    }

    public void upsertVote(String policyId, String nic, int vote) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_POLICY_ID, policyId);
        v.put(COLUMN_NIC, nic);
        v.put(COLUMN_VOTE, vote);
        db.insertWithOnConflict(TABLE_POLICY_VOTES, null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int getUserVote(String policyId, String nic) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_POLICY_VOTES, new String[]{COLUMN_VOTE},
                COLUMN_POLICY_ID + " = ? AND " + COLUMN_NIC + " = ?",
                new String[]{policyId, nic}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getInt(0);
            return 0;
        }
    }

    public int[] getTotals(String policyId) {
        SQLiteDatabase db = getReadableDatabase();
        int agree = 0;
        int disagree = 0;
        try (Cursor c = db.query(TABLE_POLICY_VOTES,
                new String[]{"SUM(CASE WHEN " + COLUMN_VOTE + " = 1 THEN 1 ELSE 0 END)",
                        "SUM(CASE WHEN " + COLUMN_VOTE + " = -1 THEN 1 ELSE 0 END)"},
                COLUMN_POLICY_ID + " = ?", new String[]{policyId}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                agree = c.getInt(0);
                disagree = c.getInt(1);
            }
        }
        return new int[]{agree, disagree};
    }
}


