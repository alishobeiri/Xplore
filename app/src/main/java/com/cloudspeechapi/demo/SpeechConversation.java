package com.cloudspeechapi.demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.hardware.Camera;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Face;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera.CameraInfo;

import java.net.URLEncoder;


public class SpeechConversation extends AppCompatActivity implements VoiceView.OnRecordListener {

    private static String TAG = "SpeechConversation";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private TextView mUserSpeechText, mSpeechRecogText;
    private VoiceView mStartStopBtn;

    private CloudSpeechService mCloudSpeechService;
    private VoiceRecorder mVoiceRecorder;
    private boolean mIsRecording = false;

    // Resource caches
    private int mColorHearing;
    private int mColorNotHearing;
    private TextView mStatus;

    private String mSavedText;
    private Handler mHandler;

    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;


    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    public void moveTextPosition(View view) {
//        Log.d("Screen Height", String.valueOf(getScreenHeight()));
//        Log.d("Screen Width", String.valueOf(getScreenWidth()));
        mUserSpeechText.setX(50);
        mUserSpeechText.setY(100);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.speechlayout);
        initViews();

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        camera = Camera.open();

        showCamera = new ShowCamera(this, camera);
        frameLayout.addView(showCamera);


        camera.setFaceDetectionListener(faceDetectionListener);
        camera.startFaceDetection();
    }


    private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
//            Log.d("onFaceDetection", "Number of Faces:" + faces.length);
            Face face = null;
            if (faces.length >= 1) {
                face = faces[0];
            }

            if (face != null) {
                RectF position = new RectF();
                position.set(face.rect);


                Matrix matrix = new Matrix();
                CameraInfo info = new CameraInfo();
                camera.getCameraInfo(0, info);

                // Need mirror for front camera.
                boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
                matrix.setScale(mirror ? -1 : 1, 1);
                // This is the value for android.hardware.Camera.setDisplayOrientation.
                matrix.postRotate(90);
                // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
                // UI coordinates range from (0, 0) to (width, height).
                matrix.postScale(getScreenWidth() / 2000f, getScreenHeight() / 2000f);
                matrix.postTranslate(getScreenWidth() / 2f, getScreenHeight() / 2f);

                matrix.mapRect(position);

                if((position.left - mSpeechRecogText.getWidth() / 3 > getScreenWidth()) ||
                        (0 < getScreenWidth())){
                    mSpeechRecogText.setX(getScreenWidth()/2 - mSpeechRecogText.getWidth() / 4);
                } else {
                    mSpeechRecogText.setX(position.left - mSpeechRecogText.getWidth() / 3);
                }

                if((position.bottom + mSpeechRecogText.getHeight() / 4) > getScreenHeight() ||
                        (position.bottom + mSpeechRecogText.getHeight() / 4) < 0) {
                    mSpeechRecogText.setY(getScreenHeight()/2 + mSpeechRecogText.getHeight() / 4);
                } else {
                    mSpeechRecogText.setY(position.bottom + mSpeechRecogText.getHeight() / 4);
                }
            }


        }
    };


    @Override
    public void onStart() {
        super.onStart();
        // Prepare Cloud Speech API
        bindService(new Intent(this, CloudSpeechService.class), mServiceConnection,
                BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void onStop() {

        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        if (mCloudSpeechService != null) {
            mCloudSpeechService.removeListener(mCloudSpeechServiceListener);
            unbindService(mServiceConnection);
            mCloudSpeechService = null;
        }

        super.onStop();
    }

    private void initViews() {

        mSavedText = "";
        mStartStopBtn = (VoiceView) findViewById(R.id.recordButton);
        mStartStopBtn.setOnRecordListener(this);


        mSpeechRecogText = (TextView) findViewById(R.id.speechRecogText);

        final Resources resources = getResources();
        final Resources.Theme theme = getTheme();
        mColorHearing = ResourcesCompat.getColor(resources, R.color.status_hearing, theme);
        mColorNotHearing = ResourcesCompat.getColor(resources, R.color.status_not_hearing, theme);

        mSpeechRecogText.setText(mSavedText);
        mHandler = new Handler(Looper.getMainLooper());

    }

    private final CloudSpeechService.Listener mCloudSpeechServiceListener = new CloudSpeechService.Listener() {
        @Override
        public void onSpeechRecognized(final String text, final boolean isFinal) {
            if (isFinal) {
                mVoiceRecorder.dismiss();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Google Translate Object
                        GoogleTranslate googleTranslate = new GoogleTranslate();
                        String translatedText = googleTranslate.execute(text, Configuration.FROM_LANG, Configuration.TO_LANG).get();
                        mSpeechRecogText.setTextColor(Color.BLACK);
                        mSpeechRecogText.setText(translatedText);
                    } catch (Exception e) {
                        Log.d("Translated Text >>>>>>", e.toString());
                    }
                }
            });
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mCloudSpeechService = CloudSpeechService.from(binder);
            mCloudSpeechService.addListener(mCloudSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCloudSpeechService = null;
        }

    };

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mCloudSpeechService != null) {
                mCloudSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(final byte[] buffer, int size) {
            if (mCloudSpeechService != null) {
                mCloudSpeechService.recognize(buffer, size);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int amplitude = (buffer[0] & 0xff) << 8 | buffer[1];
                    double amplitudeDb3 = 20 * Math.log10((double) Math.abs(amplitude) / 32768);
                    float radius2 = (float) Math.log10(Math.max(1, amplitudeDb3)) * dp2px(SpeechConversation.this, 20);
                    mStartStopBtn.animateRadius(radius2 * 10);
                }
            });
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mCloudSpeechService != null) {
                mCloudSpeechService.finishRecognizing();
            }
        }

    };

    @Override
    public void onRecordStart() {
        startStopRecording();
    }

    @Override
    public void onRecordFinish() {
        startStopRecording();
    }

    private void startStopRecording() {

        if (mIsRecording) {
            mStartStopBtn.changePlayButtonState(VoiceView.STATE_NORMAL);
            stopVoiceRecorder();
        } else {
            mStartStopBtn.changePlayButtonState(VoiceView.STATE_RECORDING);
            startVoiceRecorder();
        }
    }


//    public String sentimentAnalysis(String text) throws Exception {
//        try (LanguageServiceClient  language = LanguageServiceClient.create()) {
//            Document doc = Document.newBuilder()
//                    .setContent(text)
//                    .setType(Type.PLAIN_TEXT)
//                    .build();
//            AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
//            Sentiment sentiment = response.getDocumentSentiment();
//
//            if (sentiment == null) {
//                return null;
//            }
//            float senMagnitude = sentiment.getMagnitude();
//            float senScore = sentiment.getScore();
//
//            if (senMagnitude < 0.3 && senMagnitude > -0.3) {
//                return "Neutral";
//            }
//            if (senScore > 0.4) {
//                return "Positive";
//            }
//            if (senScore < -0.4) {
//                return "negative";
//            }
//            return "mixed";
//        }
//    }

    private void startVoiceRecorder() {
        mIsRecording = true;
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {

        mIsRecording = false;
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showPermissionMessageDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("This app needs to record audio and recognize your speech")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            }
        }).create();

        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mStatus.setTextColor(hearingVoice ? mColorHearing : mColorNotHearing);
            }
        });
    }

    public static int dp2px(Context context, int dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context
                .getResources().getDisplayMetrics());
        return px;
    }
}
