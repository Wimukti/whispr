package com.example.vlc.messaging;

import static com.example.vlc.utils.Constants.*;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.vlc.R;
import com.example.vlc.utils.DatabaseHelper;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Receiver extends CameraActivity {

    private CameraBridgeViewBase cameraBridgeViewBase;
    private TextView labelField;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receiver);
        getPermission();

        cameraBridgeViewBase = findViewById(R.id.camera_view);
        labelField = findViewById(R.id.recieved_message);
        databaseHelper = new DatabaseHelper(this);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            private boolean isFlashlightOn = false;
            private long prevTime = 0;
            private int flashlightOnFrameCount = 0;
            private int flashlightOffFrameCount = 0;
            private final StringBuilder morseCodeBuffer = new StringBuilder();
            private int blankFrameCount = 0;
            private List<String> wordSequence = new ArrayList<>();
            private List<MatOfPoint> contours;
            private long currentTime;

            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat gray = new Mat();
                cvtColor(inputFrame.rgba(), gray, COLOR_RGBA2GRAY);
                // Convert input frame to grayscale
                Mat binary = new Mat();
                threshold(gray, binary, LOWER_BINARY_THRESHOLD, HIGHER_BINARY_THRESHOLD, THRESH_BINARY);
                // Apply threshold to convert to binary image
                contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                findContours(binary, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);             // Find contours in the binary image
                currentTime = System.currentTimeMillis();

                if(isReceivingLight()) {
                    handleIsReceiving();
                } else {
                    handleNotReceiving();
                }

                if (prevTime == 0) {
                    prevTime = currentTime;
                }

                if (!isFlashlightOn) {
                    decodeMorseCodeLightOff();
                }
                return inputFrame.rgba();
            }

            boolean isReceivingLight() {
                boolean isReceiving = false;
                for (MatOfPoint contour : contours) {
                    double area = contourArea(contour);
                    if (area > CONTOUR_AREA) {
                        isReceiving = true;
                    }
                }
                return isReceiving;
            }
            void handleNotReceiving() {
                if(isFlashlightOn) {
                    handleFlashlightOn();
                } else {
                    handleFlashLightOff();
                }
                if (morseCodeBuffer.toString().length() == 1) prevTime = currentTime;
                flashlightOnFrameCount = 0;
                isFlashlightOn = false;
            }

            public void handleIsReceiving() {
                flashlightOnFrameCount++;
                isFlashlightOn = true;
                flashlightOffFrameCount = 0;
                blankFrameCount = 0;
            }

            void handleFlashlightOn() {
                Log.d(flashlightOnFrameCount + "", "illuminatedFrameCount: " + flashlightOnFrameCount);
                if (flashlightOnFrameCount >= DASH_FRAME_COUNT) {
                    morseCodeBuffer.append(DASH);
                }
                else if (flashlightOnFrameCount >= DOT_FRAME_COUNT) {
                    morseCodeBuffer.append(DOT);
                }
                else morseCodeBuffer.append("");
            }

            void handleFlashLightOff() {
                if (!morseCodeBuffer.toString().equals("")) {
                    flashlightOffFrameCount++;
                    if (flashlightOffFrameCount >= 8) {
                        blankFrameCount++;
                        if (blankFrameCount >= 26) {
                            morseCodeBuffer.append('_');
                            blankFrameCount = 0;
                        }
                        else if (blankFrameCount == 1 && !morseCodeBuffer.toString().endsWith("_")) {
                            morseCodeBuffer.append(' ');
                            String[] characterList = (morseCodeBuffer.toString().length() >= 6) ? morseCodeBuffer.substring(6).split(" ") : new String[0];
                            List<String> decodedCharacterList = new ArrayList<>();
                            for (String character : characterList) {
                                if (character.contains("_")) decodedCharacterList.add(" ");
                                decodedCharacterList.add(decodeMorseCode(character.replaceAll("_", " ")));
                            }
                            if (decodedCharacterList.size() > 4) {
                                String message = String.join("", decodedCharacterList);
                                databaseHelper.insertMessage(message, false);
                            }
                            labelField.setText(String.join("", decodedCharacterList));
                        }
                    }
                }
            }

            void decodeMorseCodeLightOff() {
                // Decode Morse code
                String morseCode = morseCodeBuffer.toString().trim();
                Log.d(morseCode, "morseCode: " + morseCode);
                if (morseCode.endsWith(ENDING_MORSE_CODE)) {
                    List<String> morseCodeSequence;
                    if (morseCode.contains("-.-.-")) {
                        morseCode = morseCode.substring(6, morseCode.length() - 5);
                        morseCodeSequence = Arrays.asList(morseCode.split("_"));
                        for (String morseCodeWord : morseCodeSequence) {
                            wordSequence.add(decodeMorseCode(morseCodeWord));
                        }
                        Log.d(wordSequence.toString(), "wordList: " + wordSequence.toString());
                        String message = String.join(" ", wordSequence);
                        databaseHelper.insertMessage(message, false);
                        labelField.setText(message);
                    }
                    morseCodeBuffer.setLength(0);
                    wordSequence = new ArrayList<>();
                    prevTime = 0;
                }
            }
        });

        if (OpenCVLoader.initDebug()) {
            System.out.println("OpenCV loaded successfully");
            cameraBridgeViewBase.enableView();
        } else {
            System.out.println("OpenCV not loaded");
        }
    }



    // Decode Morse code
    private String decodeMorseCode(String morseCode) {
        String[] morseCodes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", " "};
        String[] characters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", " "};
        StringBuilder builder = new StringBuilder();
        String[] codeWords = morseCode.split(" ");
        for (String codeWord : codeWords) {
            int index = Arrays.asList(morseCodes).indexOf(codeWord);
            if (index != -1) {
                builder.append(characters[index]);
            }
        }
        return builder.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);

    }

    void getPermission() {
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==3 && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                getPermission();
            }
        }
    }
}

/*
    Here is a breakdown of the code:
    - The code defines a class named `Receiver` in the package `com.example.vlc`.
    - The class extends the `CameraActivity` class provided by the OpenCV library.
    - The class has several member variables including `cameraBridgeViewBase`, `labelField`, and `databaseHelper`.
    - In the `onCreate` method, the layout is set, and permissions are requested for the camera.
    - The `cameraBridgeViewBase` is initialized and a `CvCameraViewListener2` is set on it.
    - The listener implements several methods related to camera view callbacks.
    - In the `onCameraFrame` method, the camera frame is processed to decode Morse code.
    - The frame is converted to grayscale and thresholded to create a binary image.
    - Contours are then extracted from the binary image using the `findContours` method.
    - The code analyzes the contours to determine if a flashlight is on or off based on the number of illuminated frames.
    - Morse code sequences are decoded from the on/off patterns of the flashlight.
    - The decoded messages are stored in a database and displayed on a text view.
    - The `decodeMorseCode` method is used to convert Morse code sequences to characters.
    - The `onResume`, `onPause`, and `onDestroy` methods handle the lifecycle of the camera view.
    - The `getPermission` method checks and requests camera permission.
    - The `onRequestPermissionsResult` method handles the result of the permission request.

    Please note that the code assumes the existence of layout resources and the `DatabaseHelper` class, which are not included in the provided code snippet.
*/