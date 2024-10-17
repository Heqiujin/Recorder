package com.example.rrrrrrecord.entity;

public class AudioEntity {
    private String filePath;
    private String title;
    private String mimeType;

    public AudioEntity(String filePath, String title, String mimeType) {
        this.filePath = filePath;
        this.title = title;
        this.mimeType = mimeType;
    }



    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return "AudioEntity{" +
                "filePath='" + filePath + '\'' +
                ", title='" + title + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}