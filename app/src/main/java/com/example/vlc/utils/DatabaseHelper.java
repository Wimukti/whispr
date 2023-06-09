package com.example.vlc.utils;

import static com.example.vlc.utils.Constants.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // SQL query to create the messages table
    private static final String CREATE_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TIMESTAMP + " INTEGER, " +
                    COLUMN_CONTENT + " TEXT, " + COLUMN_IS_SENT + " INTEGER DEFAULT 0)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the messages table
        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement if needed for future database upgrades
    }

    public void insertMessage(String msg, boolean isSent) {
        SQLiteDatabase db = this.getWritableDatabase();
        Message message = new Message(System.currentTimeMillis(), System.currentTimeMillis(), msg, isSent);
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_IS_SENT, isSent ? 1 : 0);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
    }
}
