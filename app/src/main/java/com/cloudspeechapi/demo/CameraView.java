package com.cloudspeechapi.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CameraView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
    }

    // Camera Button - Second Activity Launch
    public void launchSecondActivity(View view) {
        Intent intent = new Intent(this, SpeechConversation.class);
        startActivity(intent);
    }
}
