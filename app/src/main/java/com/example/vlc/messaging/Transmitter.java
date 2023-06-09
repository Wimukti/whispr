package com.example.vlc.messaging;

import static com.example.vlc.utils.Constants.*;

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

import com.example.vlc.R;
import com.example.vlc.utils.DatabaseHelper;

import java.util.Arrays;

public class Transmitter extends AppCompatActivity {

    private EditText editText; // EditText for user input
    private CameraManager cameraManager; // Manages the camera functionality
    private String cameraId; // ID of the camera
    private TextView transmissionRate; // Displays the transmission rate
    private long dotSpeedValue = DOT_SPEED_INITIAL_VALUE; // Current dot speed in milliseconds
    DatabaseHelper databaseHelper; // Helper class for database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmitter);

        // Button to send the message
        Button sendButton = findViewById(R.id.send_button); // Reference to the send button in the layout
        // SeekBar to adjust the dot speed
        SeekBar dotSpeed = findViewById(R.id.seekBar); // Reference to the SeekBar in the layout

        editText = findViewById(R.id.edit_text_field); // Reference to the EditText in the layout
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
            int progressChangedValue = DOT_SPEED_INITIAL_VALUE;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i + DOT_SPEED_INITIAL_VALUE;
                String transmissionRateMs = "Transmission Rate: " + progressChangedValue + "ms";
                transmissionRate.setText(transmissionRateMs);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dotSpeedValue = progressChangedValue;
                String dotValueSpeed = "Transmission Rate: " + dotSpeedValue + "ms";
                transmissionRate.setText(dotValueSpeed);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dotSpeedValue = progressChangedValue;
                String dotSpeedValueMs = "Transmission Rate: " + dotSpeedValue + "ms";
                transmissionRate.setText(dotSpeedValueMs);
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
        String morseCodeWithEnd = STARTING_MORSE_CODE + morseCode + ENDING_MORSE_CODE;
        long dotDuration = dotSpeedValue;
        for (int i = 0; i < morseCodeWithEnd.length(); i++) {
            char character = morseCodeWithEnd.charAt(i);
            long dashDuration = DASH_DURATION_MULTIPLIER * dotDuration;
            switch (character) {
                case DOT:
                    turnOnFlashlight(dotDuration); // Turn on the flashlight for dot duration
                    break;
                case DASH:
                    turnOnFlashlight(dashDuration); // Turn on the flashlight for dash duration
                    break;
                case SPACE:
                    try {
                        long wordSpaceDuration = SPACE_DURATION_MULTIPLIER * dotDuration;
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
