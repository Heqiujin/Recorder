package com.example.rrrrrrecord;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rrrrrrecord.utils.Recorder;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 200;
    private Recorder recorder; // 使用Recorder类
    private ImageView imgStart, imgStop, imgMore;
    private boolean isRefuse = false;
    List<String> audioList = new ArrayList<>();
    private String TAG;
    private boolean isRecording = false;
    private boolean isPause = false;
    private TextView recorder_state;
    public static TextView timeTextView;
    private Handler handler; // 用于更新UI的Handler
    private Runnable runnable; // 用于定时执行的Runnable

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 找到界面中的按钮
        imgStart = findViewById(R.id.imgStart);
        imgStop = findViewById(R.id.imaStop);
        imgMore = findViewById(R.id.imgMore);
        recorder_state=findViewById(R.id.recorder_state);
        ImageView dot=findViewById(R.id.dot);
        timeTextView=findViewById(R.id.time);

//        handler=new Handler();

        // 初始化Recorder对象
        recorder = new Recorder(audioList, timeTextView);

        // 处理录音按钮的点击事件
        imgStart.setOnClickListener(v -> {
            // 如果当前未录音，开始录音并更新按钮文本
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_PERMISSION);
            }
            if(isRefuse){
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    // 权限未被授予，引导用户去设置
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    // 权限已被授予，可以继续操作
                    if (!recorder.isRecording()) {
                        recorder.startRecording(this);
                        imgStart.setImageResource(R.drawable.pause);
                        imgMore.setImageResource(R.drawable.more_uncheckable);
                        imgStop.setImageResource(R.drawable.stop_checkable);
                        recorder_state.setText("录音中");
                        dot.setVisibility(View.VISIBLE);
                        isRecording=true;
                    } else {
                        // 如果正在录音，暂停录音并更新按钮文本
                        if (!isPause) {
                            recorder.pauseRecording();
                            imgStart.setImageResource(R.drawable.resume);
                            recorder_state.setText("暂停");
                            isPause=true;
                        }else {
                            recorder.resumeRecording();
                            imgStart.setImageResource(R.drawable.pause);
                            recorder_state.setText("录制中");
                            isPause=false;
                        }
                    }
                }
            }
        });

        // 处理停止按钮的点击事件
        imgStop.setOnClickListener(v -> {
            recorder.stopRecording(this);
            imgStart.setImageResource(R.drawable.start);
            imgMore.setImageResource(R.drawable.more_checkable);
            imgStop.setImageResource(R.drawable.stop_uncheckable);
            recorder_state.setText("录音机");
            dot.setVisibility(View.GONE);
            isRecording=false;
        });

        // 启动AudioListActivity
        imgMore.setOnClickListener(v -> {
            if(!recorder.isRecording()) {
                Intent intent = new Intent(MainActivity.this, AudioListActivity.class);
                startActivity(intent);
            }
        });
    }

    // 活动销毁时调用的方法
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放录音器资源
        if (recorder != null) {
            recorder.release();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1024 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) { // 检查应用是否具有外部存储的管理权限
                    isRefuse = false;
                } else {
                    isRefuse = true;
                }
            }
        }
    }
}