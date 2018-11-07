package com.chrs.videowallpaper.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import com.chrs.videowallpaper.constant.Const;
import com.chrs.videowallpaper.data.VideoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 广播接收器,来自Service的回调
 */
public class VideoRespReceive extends BroadcastReceiver {

    private static final List<OnVideoWallpaperRespListener> mListeners = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int stateCode = bundle.getInt(Const.EXTRA_RESPONSE, 0);
                Log.i("logger", "VideoRespReceive receive StateCode=" + stateCode);
                VideoInfo videoInfo = bundle.getParcelable(Const.EXTRA_FILE_INFO);
                for (OnVideoWallpaperRespListener mListener : mListeners) {
                    if (mListener != null) {
                        mListener.onVideoWallpaperResp(videoInfo, stateCode);
                    }
                }
            } else {
                Log.e("logger", "VideoRespReceive onReceive bundle null");
            }
        } else {
            Log.e("logger", "VideoRespReceive onReceive intent null");
        }
    }

    /**
     * 添加设置视频桌面响应监听
     *
     * @param listener OnVideoWallpaperRespListener
     */
    public void addOnVideoWallpaperRespListener(@Nullable OnVideoWallpaperRespListener listener) {
        if (listener != null) {
            mListeners.add(listener);
        }
    }

    /**
     * 移除设置视频桌面响应监听
     *
     * @param listener OnVideoWallpaperRespListener
     */
    public void removeOnVideoWallpaperRespListener(
            @Nullable OnVideoWallpaperRespListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    /**
     * 移除所有设置视频桌面响应监听
     */
    public void removeAllOnVideoWallpaperRespListener() {
        mListeners.clear();
    }

    /**
     * 设置视频桌面响应监听
     */
    public interface OnVideoWallpaperRespListener {

        void onVideoWallpaperResp(@Nullable VideoInfo videoInfo, int stateCode);
    }
}

