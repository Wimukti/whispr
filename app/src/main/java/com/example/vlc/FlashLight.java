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
import android.widget.TextView;
import java.util.Arrays;

public class FlashLight extends AppCompatActivity {

    private EditText editText;
    private Button sendButton;
    private Button receiverButton;
    private EditText dotSpeed;
    private TextView labelField;
    private CameraManager cameraManager;
    private String cameraId;
    private final int fps = 25;
    private long dotSpeedValue = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);

        editText = findViewById(R.id.edit_text_field);
        sendButton = findViewById(R.id.send_button);
        receiverButton = findViewById(R.id.receiver_button);

        labelField = findViewById(R.id.label_field);
        dotSpeed = findViewById(R.id.dot_speed_field);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        dotSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!dotSpeed.getText().toString().isEmpty()) {
                    dotSpeedValue = Long.parseLong(dotSpeed.getText().toString());
                    if (dotSpeedValue < 50 || dotSpeedValue > 150) {
                        dotSpeedValue = 50;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString();
                String morseCode = encodeMorseCode(message);
                labelField.setText(morseCode);
                transmitMorseCode(morseCode);
            }
        });

        receiverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FlashLight.this, MainActivity.class);
                startActivity(intent);
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
        // Append end-of-message prosign to indicate the end of the transmission
//        String endOfMessage = ".-.-";
//        for (int i = 0; i < endOfMessage.length(); i++) {
//            char character = endOfMessage.charAt(i);
//            long dashDuration = 3 * dotDuration;
//            switch (character) {
//                case '.':
//                    turnOnFlashlight(dotDuration);
//                    break;
//                case '-':
//                    turnOnFlashlight(dashDuration);
//                    break;
//            }
//            try {
//                long letterSpaceDuration = 3 * dotDuration;
//                Thread.sleep(letterSpaceDuration);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
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