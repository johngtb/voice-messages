package com.gypsian.android.samples.voicemessages;

import android.app.ProgressDialog;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MessengerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MessengerActivity";

    private Button mRecordBtn;

    private TextView mStatusTv;

    private MediaRecorder mRecorder;

    private String mLocalFilePath = null;

    private StorageReference mFirebaseStorage;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseStorage = FirebaseStorage.getInstance().getReference();

        mProgress = new ProgressDialog(this);

        // Record to the external cache directory for visibility
        mLocalFilePath = getExternalCacheDir().getAbsolutePath();
        mLocalFilePath += "/audiorecordtest.3gp";

        setContentView(R.layout.activity_messenger);

        mRecordBtn = (Button) findViewById(R.id.btn_record);

        mStatusTv = (TextView) findViewById(R.id.tv_status);

        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mLocalFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        mStatusTv.setText(getString(R.string.record_started));
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mStatusTv.setText(getString(R.string.record_finished));

        uploadAudio();
    }

    private void uploadAudio() {

        mStatusTv.setText(getString(R.string.upload_started));
        mProgress.setMessage("Uploading...");
        mProgress.show();

        StorageReference firebasePath = mFirebaseStorage.child("VoiceMessages").child("Audio").child("new_audio.3gp");

        Uri localUri = Uri.fromFile(new File(mLocalFilePath));

        firebasePath.putFile(localUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgress.dismiss();
                mStatusTv.setText(getString(R.string.upload_finished));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgress.dismiss();
                Toast.makeText(MessengerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                mStatusTv.setText(e.getMessage());
            }
        });
    }
}
