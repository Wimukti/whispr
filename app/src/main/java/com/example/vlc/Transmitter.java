package com.example.vlc;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Arrays;

public class Transmitter extends AppCompatActivity {

    private EditText editText;
    private Button sendButton;
    private SeekBar dotSpeed;
    private CameraManager cameraManager;
    private String cameraId;
    private TextView transmissionRate;
    private long dotSpeedValue = 50;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmitter);

        editText = findViewById(R.id.edit_text_field);
        sendButton = findViewById(R.id.send_button);
        dotSpeed = findViewById(R.id.seekBar);
        transmissionRate = findViewById(R.id.transmission_rate);
        databaseHelper = new DatabaseHelper(this);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Change dotSpeed using SeekBar
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
                String message = editText.getText().toString();
                String morseCode = encodeMorseCode(message);
                transmitMorseCode(morseCode);
                databaseHelper.insertMessage(message, true);
            }
        });
    }

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

    private void transmitMorseCode(String morseCode) {
        Handler handler = new Handler();
        String morseCodeWithEnd = "-.-.- " + morseCode + " .-.-";
        long dotDuration = dotSpeedValue;
        for (int i = 0; i < morseCodeWithEnd.length(); i++) {
            char character = morseCodeWithEnd.charAt(i);
            long dashDuration = 3 * dotDuration;
            switch (character) {
                case '.':
                    turnOnFlashlight(dotDuration);
                    break;
                case '-':
                    turnOnFlashlight(dashDuration);
                    break;
                case ' ':
                    try {
                        long wordSpaceDuration = 7 * dotDuration;
                        Thread.sleep(wordSpaceDuration);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            try {
                long letterSpaceDuration = 3 * dotDuration;
                Thread.sleep(letterSpaceDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void turnOnFlashlight(long duration) {
        try {
            cameraManager.setTorchMode(cameraId, true);
            Thread.sleep(duration);
        } catch (CameraAccessException | InterruptedException e) {
            e.printStackTrace();
        }
        turnOffFlashlight();
    }

    private void turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}