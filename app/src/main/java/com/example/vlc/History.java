package com.example.vlc;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlc.utils.DatabaseHelper;
import com.example.vlc.utils.Message;

import java.util.ArrayList;
import java.util.List;

import static com.example.vlc.utils.Constants.*;

public class History extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.messageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);

        // Retrieve messages from the database
        retrieveMessages();

        // Set up the adapter for the RecyclerView
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
    }

    private void retrieveMessages() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
            @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
            @SuppressLint("Range") boolean isSent = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SENT)) == 1;

            Message message = new Message(id, timestamp, content, isSent);
            messageList.add(message);
        }

        cursor.close();
    }
}
