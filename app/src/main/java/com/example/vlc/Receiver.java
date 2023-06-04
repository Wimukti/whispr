package com.example.vlc;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

    CameraBridgeViewBase cameraBridgeViewBase;
    Button button;
    TextView labelField;
    DatabaseHelper databaseHelper;

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
            private int illuminatedFrameCount = 0;
            private int darkFrameCount = 0;
            private final StringBuilder morseCodeBuilder = new StringBuilder();
            private int spaceCount = 0;
            private List<String> morseCodeList = new ArrayList<>();
            private List<String> wordList = new ArrayList<>();
            private static final long DOT_DURATION = 40;
            private boolean isReceiving = false;

            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                // Convert input frame to grayscale
                Mat gray = new Mat();
                cvtColor(inputFrame.rgba(), gray, COLOR_RGBA2GRAY);

                // Apply threshold to convert to binary image
                Mat binary = new Mat();
                threshold(gray, binary, 145, 255, THRESH_BINARY);

                // Find contours in the binary image
                List<MatOfPoint> contours = new ArrayList<>();

                Mat hierarchy = new Mat();
                findContours(binary, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

                long currentTime = System.currentTimeMillis();

                // find max contours is area is greater than 400000

                for (MatOfPoint contour : contours) {
                    double area = contourArea(contour);
                    if (area > 400000) {
                        isReceiving = true;
                    }
                }

                if(isReceiving) {
                    illuminatedFrameCount++;
                    isFlashlightOn = true;
                    darkFrameCount = 0;
                    spaceCount = 0;
                    isReceiving = false;
                } else {
                    if(isFlashlightOn) {
                        Log.d(illuminatedFrameCount + "", "illuminatedFrameCount: " + illuminatedFrameCount);
                        if (illuminatedFrameCount >= 3) {
                            morseCodeBuilder.append('-');
                        }
                        else if (illuminatedFrameCount >= 1) {
                            morseCodeBuilder.append('.');
                        }
                        else morseCodeBuilder.append("");
                    }
                    else {
                        if (!morseCodeBuilder.toString().equals("")) {
                            darkFrameCount++;
                            if (darkFrameCount >= 8) {
                                spaceCount++;
                                if (spaceCount >= 26) {
                                    morseCodeBuilder.append('_');
                                    spaceCount = 0;
                                }
                                else if (spaceCount == 1 && !morseCodeBuilder.toString().endsWith("_")) {
                                    morseCodeBuilder.append(' ');
                                    String[] characterList = (morseCodeBuilder.toString().length() >= 6) ? morseCodeBuilder.toString().substring(6).split(" ") : new String[0];
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
                    if (morseCodeBuilder.toString().length() == 1) prevTime = currentTime;
                    illuminatedFrameCount = 0;
                    isFlashlightOn = false;
                }

                if (prevTime == 0) {
                    prevTime = currentTime;
                }

                if (!isFlashlightOn) {
                    // Decode Morse code
                    String morseCode = morseCodeBuilder.toString().trim();
                    Log.d(morseCode, "morseCode: " + morseCode);
                    if (morseCode.endsWith(" .-.-")) {
                        if (morseCode.contains("-.-.-")) {
                            morseCode = morseCode.substring(6, morseCode.length() - 5);
                            morseCodeList = Arrays.asList(morseCode.split("_"));
                            for (String morseCodeWord : morseCodeList) {
                                wordList.add(decodeMorseCode(morseCodeWord));
                            }
                            Log.d(wordList.toString(), "wordList: " + wordList.toString());
                            String message = String.join(" ", wordList);
                            databaseHelper.insertMessage(message, false);
                            labelField.setText(message);
                        }
                        morseCodeBuilder.setLength(0);
                        wordList = new ArrayList<>();
                        morseCodeList = new ArrayList<>();
                        prevTime = 0;
                    }
                }
                return inputFrame.rgba();
            }
        });

        if (OpenCVLoader.initDebug()) {
            System.out.println("OpenCV loaded successfully");
            cameraBridgeViewBase.enableView();
        } else {
            System.out.println("OpenCV not loaded");
        }
    }



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