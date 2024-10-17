package com.example.rrrrrrecord.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
public class AudioUtils {
    private static final String TAG = "AudioUtils";
    public static List<String> loadAudioFiles(ContentResolver contentResolver) {
        List<String> audioList = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String audioPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    Log.d(TAG, "Found audio file: " + audioPath);
                    audioList.add( audioPath);
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No audio files found.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading audio files", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return audioList;
    }
}