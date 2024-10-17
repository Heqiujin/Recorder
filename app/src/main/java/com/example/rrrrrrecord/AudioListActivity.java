package com.example.rrrrrrecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rrrrrrecord.adapter.AudioAdapter;

import java.util.ArrayList;
import java.util.List;


public class AudioListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AudioAdapter audioAdapter;
    private List<String> audioList;
    public ProgressBar progressBar;
    private ImageView turnLeft;
//    private TextView turnLeft;
    private String TAG ;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_list);

        // 初始化 RecyclerView 和音频列表
        recyclerView = findViewById(R.id.recyclerView); // 通过 ID 找到 RecyclerView
        audioList = new ArrayList<>(); // 创建一个新的 ArrayList 实例
        audioAdapter = new AudioAdapter(audioList); // 初始化音频适配器
//        progressBar=findViewById(R.id.progress_bar);
//        progressBar.setVisibility(View.VISIBLE);

        turnLeft=findViewById(R.id.icon_left);
        turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent turnLeft = new Intent(AudioListActivity.this, MainActivity.class);
                startActivity(turnLeft);
            }
        });

        // 设置 RecyclerView 的布局管理器为线性布局
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(audioAdapter); // 设置适配器以显示音频列表

        loadAudioFiles(); // 调用方法以加载音频文件

    }
    //释放资源
    @Override
    protected void onDestroy(){
        super.onDestroy();
        audioAdapter.releaseMediaPlayer();
    }

    // 加载音频文件的方法
    private void loadAudioFiles() {
        // 定义要查询的列，这里仅查询音频文件的数据路径
        String[] projection = {MediaStore.Audio.Media.DATA};
        // 查询外部音频内容的 URI，返回 Cursor
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
            Log.d(TAG, "loadAudioFiles: " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            if (cursor != null && cursor.moveToFirst()) { // 检查cursor不为空且有数据
                do {
                    String audioPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    Log.d("AudioListActivity", "Found audio file: " + audioPath); // Log found audio files
                    audioList.add(audioPath);
                    Log.d(TAG, "loadAudioFiles: " + audioPath);
                } while (cursor.moveToNext());
            } else {
                Log.d("AudioListActivity", "No audio files found.");
            }
        } catch (Exception e) {
            Log.e("AudioListActivity", "Error loading audio files", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // 确保 cursor 被关闭
            }
        }
    }
}