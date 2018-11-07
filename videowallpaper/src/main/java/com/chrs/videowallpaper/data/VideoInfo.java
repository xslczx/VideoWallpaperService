package com.chrs.videowallpaper.data;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoInfo implements Parcelable {

    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
    private String fileId;
    private String displayName;
    private String filePath;

    public VideoInfo() {
    }

    protected VideoInfo(Parcel in) {
        fileId = in.readString();
        displayName = in.readString();
        filePath = in.readString();
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileId);
        dest.writeString(displayName);
        dest.writeString(filePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
