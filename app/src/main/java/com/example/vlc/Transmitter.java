package com.example.vlc;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Arrays;

public class Transmitter extends AppCompatActivity {

    private EditText editText; // EditText for user input
    private Button sendButton; // Button to send the message
    private SeekBar dotSpeed; // SeekBar to adjust the dot speed
    private CameraManager cameraManager; // Manages the camera functionality
    private String cameraId; // ID of the camera
    private TextView transmissionRate; // Displays the transmission rate
    private long dotSpeedValue = 50; // Current dot speed in milliseconds
    DatabaseHelper databaseHelper; // Helper class for database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmitter);

        editText = findViewById(R.id.edit_text_field); // Reference to the EditText in the layout
        sendButton = findViewById(R.id.send_button); // Reference to the send button in the layout
        dotSpeed = findViewById(R.id.seekBar); // Reference to the SeekBar in the layout
        transmissionRate = findViewById(R.id.transmission_rate); // Reference to the TextView for transmission rate
        databaseHelper = new DatabaseHelper(this); // Initialize the database helper
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE); // Get the camera manager service

        try {
            cameraId = cameraManager.getCameraIdList()[0]; // Get the ID of the first camera
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Change dotSpeed using SeekBar
        dotSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 50;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i + 50;
                transmissionRate.setText("Transmission Rate: " + progressChangedValue + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dotSpeedValue = progressChangedValue;
                transmissionRate.setText("Transmission Rate: " + dotSpeedValue + "ms");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dotSpeedValue = progressChangedValue;
                transmissionRate.setText("Transmission Rate: " + dotSpeedValue + "ms");
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString(); // Get the message from EditText
                String morseCode = encodeMorseCode(message); // Convert message to Morse code
                transmitMorseCode(morseCode); // Transmit the Morse code using flashlight
                databaseHelper.insertMessage(message, true); // Insert the message into the database
            }
        });
    }

    // Encode the message to Morse code
    private String encodeMorseCode(String message) {
        String[] morseCodes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", " "};
        String[] characters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", " "};
        StringBuilder builder = new StringBuilder();
        message = message.toUpperCase();
        for (int i = 0; i < message.length(); i++) {
            char character = message.charAt(i);
            int index = Arrays.asList(characters).indexOf(Character.toString(character));
            if (index != -1) {
                builder.append(morseCodes[index]);
                if (i != message.length() - 1) {
                    builder.append(" ");
                }
            }
        }
        return builder.toString();
    }

    // Transmit the Morse code using flashlight
    private void transmitMorseCode(String morseCode) {
        String morseCodeWithEnd = "-.-.- " + morseCode + " .-.-";
        long dotDuration = dotSpeedValue;
        for (int i = 0; i < morseCodeWithEnd.length(); i++) {
            char character = morseCodeWithEnd.charAt(i);
            long dashDuration = 3 * dotDuration;
            switch (character) {
                case '.':
                    turnOnFlashlight(dotDuration); // Turn on the flashlight for dot duration
                    break;
                case '-':
                    turnOnFlashlight(dashDuration); // Turn on the flashlight for dash duration
                    break;
                case ' ':
                    try {
                        long wordSpaceDuration = 7 * dotDuration;
                        Thread.sleep(wordSpaceDuration); // Pause for word space duration
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            try {
                long letterSpaceDuration = 3 * dotDuration;
                Thread.sleep(letterSpaceDuration); // Pause for letter space duration
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Turn on the flashlight for the specified duration
    private void turnOnFlashlight(long duration) {
        try {
            cameraManager.setTorchMode(cameraId, true); // Turn on the flashlight
            Thread.sleep(duration); // Keep the flashlight on for the specified duration
        } catch (CameraAccessException | InterruptedException e) {
            e.printStackTrace();
        }
        turnOffFlashlight(); // Turn off the flashlight
    }

    // Turn off the flashlight
    private void turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, false); // Turn off the flashlight
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
