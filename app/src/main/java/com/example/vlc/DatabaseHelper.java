package com.example.vlc;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "message_history.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and column names
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IS_SENT = "is_sent";

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

    public long insertMessage(String msg, boolean isSent) {
        SQLiteDatabase db = this.getWritableDatabase();
        Message message = new Message(System.currentTimeMillis(), System.currentTimeMillis(), msg, isSent);
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_IS_SENT, isSent ? 1 : 0);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
                @SuppressLint("Range") boolean isSent = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SENT)) == 1;
                Message message = new Message(id, timestamp, content, isSent);
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messages;
    }
}
