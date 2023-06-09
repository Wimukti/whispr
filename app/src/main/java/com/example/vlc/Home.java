package com.example.vlc;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vlc.messaging.Receiver;
import com.example.vlc.messaging.Transmitter;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //This is the button to navigate to the transmitter page and receive page
        Button sendButton = findViewById(R.id.send_button);
        Button receiverButton = findViewById(R.id.receiver_button);
        Button historyButton = findViewById(R.id.history_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Transmitter.class);
                startActivity(intent);
            }
        });

        receiverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Receiver.class);
                startActivity(intent);
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, History.class);
                startActivity(intent);
            }
        });

    }
}