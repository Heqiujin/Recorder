package com.example.rrrrrrecord.utils;

import android.content.ContentValues;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rrrrrrecord.MainActivity;
import com.example.rrrrrrecord.R;
import com.example.rrrrrrecord.entity.AudioEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Recorder {
    private MediaRecorder recorder;
    private String fileName;
    private boolean isRecording = false;
    private boolean isPause = false;
    private static final String TAG = "Recorder";
    private final List<String> audioList;
    private final Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime;
    private long elapsedTime;
    private static TextView timeTextView;
    private String time;

    public Recorder(List<String> audioList, TextView timeTextView) {
        this.audioList = audioList;
        Recorder.timeTextView = timeTextView; // 这里要注意, 设置为静态
        timerHandler = new Handler(Looper.getMainLooper());
    }

    public Recorder(List<String> audioList) {
        this(audioList, null);
    }

    public synchronized void startRecording(MainActivity activity) {
        LocalDateTime currentDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDateTime.now();
        }
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd hh-mm-ss");
        }
        String dateString = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd hh-mm-ss"));
        }

        fileName = Environment.getExternalStorageDirectory() + "/" + dateString + ".3ga";
        Log.d(TAG, "startRecording: " + fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            startTime = System.currentTimeMillis();
            elapsedTime = 0;
            Toast.makeText(activity, "录音开始", Toast.LENGTH_SHORT).show();
            startTimer();
        } catch (IOException e) {
            Log.e(TAG, "startRecording: IOException", e);
        }
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                int minutes = (int) (elapsed / 60000);
                int seconds = (int) (elapsed % 60000) / 1000;
                int displayMilliseconds = (int) (elapsed % 1000) / 10;
                time = String.format("%02d:%02d:%02d", minutes, seconds, displayMilliseconds);

                if (timeTextView != null) {
                    timeTextView.setText(time);
                }
                timerHandler.postDelayed(this, 100);
            }
        };
        timerHandler.post(timerRunnable);
    }

    public synchronized void pauseRecording() {
        if (isRecording) {
            recorder.pause();
            isPause = true;
            elapsedTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "pauseRecording: " + elapsedTime);
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    public synchronized void resumeRecording() {
        if (isPause) {
            recorder.resume();
            isPause = false;
            isRecording=true;
            startTime = System.currentTimeMillis() - elapsedTime;
            timerHandler.post(timerRunnable);
            Log.d("kkkkkkkkkk", "有吗哈哈哈哈: ");
        }
    }

    public synchronized void stopTimer() {
        // 停止计时器并重置时间显示
        timerHandler.removeCallbacks(timerRunnable);
        isPause=true;
        timeTextView.setText("00:00:00"); // 重置时间显示
    }

    public synchronized void stopRecording(MainActivity activity) {
        if (isRecording) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                isRecording = false;
                stopTimer(); // 调用停止计时器的方法

                AudioEntity audioEntity = new AudioEntity(fileName, "录音", "audio/mpeg");
                audioList.add(String.valueOf(audioEntity));

                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.DATA, fileName);
                values.put(MediaStore.Audio.Media.TITLE, audioEntity.getTitle());
                values.put(MediaStore.Audio.Media.MIME_TYPE, audioEntity.getMimeType());

                Uri uri = activity.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    Toast.makeText(activity, "录音已保存", Toast.LENGTH_SHORT).show();
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "stopRecording: RuntimeException", e);
            } finally {
                // 在这里不需要再重置时间，因为在stopTimer中已经处理过了
            }
        }
    }


    public synchronized boolean isRecording() {
        return isRecording;
    }

    public synchronized boolean isPaused() {
        return isPause;
    }

    public synchronized void release() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        timerHandler.removeCallbacks(timerRunnable);
    }
}