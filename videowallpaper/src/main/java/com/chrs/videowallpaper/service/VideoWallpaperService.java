package com.chrs.videowallpaper.service;

import android.app.WallpaperManager;
import android.content.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;
import com.chrs.videowallpaper.constant.Const;
import com.chrs.videowallpaper.constant.StateCode;
import com.chrs.videowallpaper.data.VideoInfo;

import java.io.File;
import java.io.IOException;

public class VideoWallpaperService extends WallpaperService {

    @Nullable
    private VideoEngine mVideoEngine;
    @Nullable
    private VideoInfo mVideoInfo;
    private boolean mIsVolume;
    private boolean mIsToast;
    private boolean mIsFirst = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mVideoEngine == null || mVideoEngine.mHolder == null) {
            startSetWallpaper();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开启视频播放
     */
    private void startSetWallpaper() {
        clearDefaultWallpaper();
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, VideoWallpaperService.class));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 清除默认壁纸
     */
    public void clearDefaultWallpaper() {
        try {
            WallpaperManager.getInstance(this).clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建引擎
     *
     * @return Engine
     */
    @Override
    public Engine onCreateEngine() {
        mVideoEngine = new VideoEngine();
        return mVideoEngine;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearDefaultWallpaper();
        mVideoEngine = null;
    }

    /**
     * 视频播放引擎
     */
    class VideoEngine extends Engine {

        @Nullable
        private SurfaceHolder mHolder;
        @Nullable
        private MediaPlayer mPlayer;
        @Nullable
        private GestureDetector mGestureDetector;
        private GestureDetector.OnGestureListener mGestureListener = new GestureDetector
                .SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                sendResp(StateCode.EVENT_LONG_CLICK);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                sendResp(StateCode.EVENT_SINGLE_CLICK);
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                sendResp(StateCode.EVENT_DOUBLE_CLICK);
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                    float distanceY) {
                sendResp(StateCode.EVENT_SCROLL);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        };

        /**
         * 广播接收数据,来自外部的请求
         */
        private BroadcastReceiver mVideoReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    Log.i("logger", "VideoWallpaperService onReceive action =" + action);
                    if (!TextUtils.equals(action, Const.ACTION_VIDEO_REQUEST)) {
                        return;
                    }

                    mVideoInfo = intent.getParcelableExtra(Const.EXTRA_FILE_INFO);
                    mIsVolume = intent.getBooleanExtra(Const.EXTRA_VOLUME, false);
                    mIsToast = intent.getBooleanExtra(Const.EXTRA_TOAST, false);
                    if (mVideoInfo != null && mVideoInfo.getFilePath() != null && new File(
                            mVideoInfo.getFilePath()).exists()) {
                        startVideo(new File(mVideoInfo.getFilePath()), mIsVolume);
                    } else {
                        Log.e("logger", "VideoWallpaperService onReceive file not exists");
                        sendResp(StateCode.RESP_FAILED_NULL);
                        if (mIsToast) {
                            Toast.makeText(VideoWallpaperService.this, "无有效视频文件",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e("logger", "VideoWallpaperService onReceive intent null");
                }
            }
        };

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (mGestureDetector != null) {
                mGestureDetector.onTouchEvent(event);
            }
        }

        /**
         * 发送响应广播
         *
         * @param value StateCode
         */
        private void sendResp(int value) {
            Intent respIntent = new Intent();
            respIntent.setAction(Const.ACTION_VIDEO_RESPONSE);
            respIntent.setPackage(getPackageName());
            Bundle bundle = new Bundle();
            bundle.putInt(Const.EXTRA_RESPONSE, value);
            bundle.putParcelable(Const.EXTRA_FILE_INFO, mVideoInfo);
            respIntent.putExtras(bundle);
            sendBroadcast(respIntent);
            Log.i("logger", "VideoWallpaperService send StateCode=" + value);
        }

        /**
         * 开始播放视频
         *
         * @param file     视频文件
         * @param isVolume 是否开启声音
         */
        private void startVideo(File file, boolean isVolume) {
            if (mHolder != null) {
                try {
                    if (mPlayer != null) {
                        mPlayer.release();
                    }
                    mPlayer = new MediaPlayer();
                    mPlayer.setSurface(mHolder.getSurface());
                    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            if (mIsFirst) {
                                mIsFirst = false;
                            } else {
                                sendResp(StateCode.RESP_SUCCESS);
                                if (mIsToast) {
                                    Toast.makeText(VideoWallpaperService.this, "桌面设置成功",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                            mp.start();
                        }
                    });
                    mPlayer.setDataSource(file.getAbsolutePath());
                    mPlayer.setVolume(isVolume ? 1.0f : 0, isVolume ? 1.0f : 0);
                    mPlayer.setLooping(true);
                    mPlayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResp(StateCode.RESP_FAILED_ERROR);
                    if (mIsToast) {
                        Toast.makeText(VideoWallpaperService.this, "无法播放该文件", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            } else {
                sendResp(StateCode.RESP_FAILED_INIT);
                if (mIsToast) {
                    Toast.makeText(VideoWallpaperService.this, "初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * 创建引擎
         *
         * @param surfaceHolder SurfaceHolder
         */
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Const.ACTION_VIDEO_REQUEST);
            registerReceiver(mVideoReceive, filter);
            setTouchEventsEnabled(true);
            mGestureDetector = new GestureDetector(getApplicationContext(), mGestureListener);
        }

        /**
         * 创建播放器
         *
         * @param holder SurfaceHolder
         */
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mHolder = holder;
            if (mVideoInfo != null && mVideoInfo.getFilePath() != null && new File(
                    mVideoInfo.getFilePath()).exists()) {
                startVideo(new File(mVideoInfo.getFilePath()), mIsVolume);
            }
        }

        /**
         * 可见性改变
         *
         * @param visible 是否可见
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (mPlayer != null) {
                if (visible) {
                    mPlayer.start();
                } else {
                    mPlayer.pause();
                }
            }
        }

        /**
         * 销毁播放器
         *
         * @param holder SurfaceHolder
         */
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mHolder = null;
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        }

        /**
         * 销毁引擎
         */
        @Override
        public void onDestroy() {
            super.onDestroy();
            mGestureDetector = null;
            unregisterReceiver(mVideoReceive);
        }
    }
}
