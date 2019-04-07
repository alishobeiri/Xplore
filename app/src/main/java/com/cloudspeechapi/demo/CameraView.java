package com.cloudspeechapi.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CameraView extends AppCompatActivity {
    // FROM: language
    String[] listItems;
    Button mBtn;
    TextView mTextView;
    // TO: language
    String[] listItemsTo;
    Button mBtnTo;
    TextView mTextViewTo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        mBtn = findViewById(R.id.from_button);
        mTextView = findViewById(R.id.from_text);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // list of languages
                listItems=new String[]{"English", "French", "Spanish"};
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CameraView.this);
                mBuilder.setTitle("Pick your language:");
                mBuilder.setIcon(R.drawable.ic_language_black_24dp);
                mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mTextView.setText(listItems[i]);
                        if (listItems[i].equals("English")) {
                            Configuration.FROM_LANG = "en";
                        } else if (listItems[i].equals("Spanish")){
                            Configuration.FROM_LANG = "es";
                        } else if (listItems[i].equals("French")){
                            Configuration.FROM_LANG = "fr";
                        }

                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                // show the alert dialog
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

        // Start TO: language conversion
        mBtnTo = findViewById(R.id.to_button);
        mTextViewTo = findViewById(R.id.to_text);

        mBtnTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // list languages to convert
                listItemsTo=new String[]{"English", "French", "Spanish"};
                AlertDialog.Builder mBuilderTo = new AlertDialog.Builder(CameraView.this);
                mBuilderTo.setTitle("Convert To:");
                mBuilderTo.setIcon(R.drawable.ic_language_black_24dp);
                mBuilderTo.setSingleChoiceItems(listItemsTo, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mTextViewTo.setText(listItemsTo[i]);
                        if (listItems[i].equals("English")) {
                            Configuration.TO_LANG = "en";
                        } else if (listItems[i].equals("Spanish")){
                            Configuration.TO_LANG = "es";
                        } else if (listItems[i].equals("French")){
                            Configuration.TO_LANG = "fr";
                        }

                        dialogInterface.dismiss();
                    }
                });

                mBuilderTo.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog mDialogTo = mBuilderTo.create();
                mDialogTo.show();
            }
        });



    }

    // Camera Button - Second Activity Launch
    public void launchSecondActivity(View view) {
        Intent intent = new Intent(this, SpeechConversation.class);
        startActivity(intent);
    }
}
