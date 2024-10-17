package com.example.rrrrrrecord.adapter;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrrrrrecord.R;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    public ArrayList<AudioViewHolder> holders = new ArrayList<>();
    private final List<String> audioList;
    public Handler handler1 = new Handler();
    private MediaPlayer mediaPlayer;
    public Runnable runnable;
    private String TAG = "AudioAdapter";
    private boolean isPlaying = false;
    private int currentlyPlayingPosition = -1; // 记录当前播放的音频位置
    private boolean isPause = false;

    public AudioAdapter(List<String> audioList) {
        this.audioList = audioList;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        AudioViewHolder audioViewHolder = new AudioViewHolder(view);
        holders.add(audioViewHolder);
        return audioViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        String audioPath = audioList.get(position);
        holder.bind(audioPath);

        // 设置进度条的初始状态
        ProgressBar progressBar = holder.itemView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        // 由于使用 MediaPlayer 可能会有异常，所以在这里不预先创建 MediaPlayer
        int duration = 0; // 先设定时长为0，以防止未能成功获取
        try {
            // 设置数据源为 Uri，避免 prepare() 失败
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(holder.itemView.getContext(), Uri.parse(audioPath));
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source or preparing media player: " + e.getMessage());
            return; // 如果设置数据源失败，则直接返回
        }

        String formattedTime = convertMillisToTime(duration);
        TextView textView = holder.itemView.findViewById(R.id.audio_text_3);
        textView.setText(formattedTime);

        holder.playImg.setOnClickListener(v -> {
            if (currentlyPlayingPosition == position) {
                // 如果当前点击的音频是正在播放的音频
                if (isPlaying) {
                    if (!isPause) {
                        pauseAudio(holder);
                    } else {
                        resumeAudio(holder);
                    }
                }
            } else {
                // 如果点击的是不同的音频
                if (isPlaying) {
                    stopCurrentAudio(); // 停止当前播放的音频
                }
                playAudio(audioPath, holder, position);
            }
        });
    }

    private void playAudio(String audioPath, AudioViewHolder holder, int position) {
        ProgressBar progressBar = holder.itemView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        Log.d("1111111", "进度条有了吗: ");

        try {
            // 创建 MediaPlayer 实例并设置数据源为 Uri
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(holder.itemView.getContext(), Uri.parse(audioPath));
            mediaPlayer.prepare();
            mediaPlayer.start();
            holder.playImg.setImageResource(R.drawable.pause_3); // 设置为暂停图标
            isPlaying = true;
            isPause = false;
            currentlyPlayingPosition = position; // 更新当前播放位置

            Log.d(TAG, "Playing audio from: " + audioPath);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 播放完毕时执行的方法
                holder.playImg.setImageResource(R.drawable.start_2); // 设置为播放图标
                handler1.removeCallbacksAndMessages(runnable);
                progressBar.setVisibility(View.GONE);
                isPlaying = false;
            });


                // 每隔一段时间更新当前播放时间
            runnable = new Runnable() {
                @Override
                public void run() {
                    // 获取当前播放位置
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int maxPosition = mediaPlayer.getDuration();
                    Log.d("current_time", "run: " + currentPosition);
                    progressBar.setMax(maxPosition);
                    progressBar.setProgress(currentPosition);

                    String formattedTime = convertMillisToTime(currentPosition);
                    TextView textView = holder.itemView.findViewById(R.id.audio_text_3);
                    textView.setText(formattedTime);
                    // 每100毫秒更新一次
                    handler1.postDelayed(this, 300);
                }
            };

            handler1.post(runnable);
        } catch (IOException e) {
            Log.e(TAG, "Error playing audio from: " + audioPath, e);
        }
    }

    public static String convertMillisToTime(long millis) {
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        // 格式化为 HH:MM:SS
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void pauseAudio(AudioViewHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            holder.playImg.setImageResource(R.drawable.start_2); // 设置为播放图标
            isPause = true;
        }
    }

    private void resumeAudio(AudioViewHolder holder) {
        if (isPause) {
            mediaPlayer.start();
            holder.playImg.setImageResource(R.drawable.pause_3); // 设置为暂停图标
            isPause = false;
        }
    }

    private void stopCurrentAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            isPause = true;
            currentlyPlayingPosition = -1; // 重置播放位置
            handler1.removeCallbacksAndMessages(runnable);
        }

        for (AudioViewHolder audioViewHolder : holders) {
            audioViewHolder.playImg.setImageResource(R.drawable.start_2); // 设置为播放图标
            ProgressBar progressBar = audioViewHolder.itemView.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
            handler1.removeCallbacksAndMessages(runnable);
            TextView textView = audioViewHolder.itemView.findViewById(R.id.audio_text_3);
            textView.setEnabled(false); // 禁用 TextView（如果需要）
        }
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public ImageView playImg;
        public TextView audioDuration;
        public TextView audioTime;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.audio_text);
            playImg = itemView.findViewById(R.id.play_img);
            audioDuration = itemView.findViewById(R.id.audio_text_3);
            audioTime = itemView.findViewById(R.id.audio_text_2);
        }

        public void bind(String audioPath) {
            if (audioPath != null && !audioPath.isEmpty()) {
                textView.setText(audioPath);
            }
        }
    }

    public void releaseMediaPlayer() {
        stopCurrentAudio(); // 确保释放MediaPlayer
        handler1.removeCallbacksAndMessages(null); // 移除所有回调和消息
    }
}
