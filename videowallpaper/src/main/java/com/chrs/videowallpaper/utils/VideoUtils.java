package com.chrs.videowallpaper.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.annotation.RequiresApi;
import com.chrs.videowallpaper.constant.Const;
import com.chrs.videowallpaper.data.VideoInfo;
import com.chrs.videowallpaper.receive.VideoRespReceive;
import com.chrs.videowallpaper.service.VideoWallpaperService;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * 获取本地视频的工具类
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class VideoUtils {

    private static final long DEFAULT_SEND_TIME = 300;
    public static long sSendTime = DEFAULT_SEND_TIME;
    private static VideoRespReceive sReceive = new VideoRespReceive();

    /**
     * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    /**
     * 获取指定路径中的视频文件
     *
     * @param list 装扫描出来的视频文件实体类
     * @param file 指定的文件
     */
    public static void getVideoFile(final List<VideoInfo> list, File file) {// 获得视频文件
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // sdCard找到视频名称
                String name = file.getName();
                int i = name.indexOf('.');
                if (i != -1) {
                    name = name.substring(i);//获取文件后缀名
                    if (name.equalsIgnoreCase(".mp4")  //忽略大小写
                            || name.equalsIgnoreCase(".3gp")
                            || name.equalsIgnoreCase(".wmv")
                            || name.equalsIgnoreCase(".ts")
                            || name.equalsIgnoreCase(".rmvb")
                            || name.equalsIgnoreCase(".mov")
                            || name.equalsIgnoreCase(".m4v")
                            || name.equalsIgnoreCase(".avi")
                            || name.equalsIgnoreCase(".m3u8")
                            || name.equalsIgnoreCase(".3gpp")
                            || name.equalsIgnoreCase(".3gpp2")
                            || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv")
                            || name.equalsIgnoreCase(".divx")
                            || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm")
                            || name.equalsIgnoreCase(".asf")
                            || name.equalsIgnoreCase(".ram")
                            || name.equalsIgnoreCase(".mpg")
                            || name.equalsIgnoreCase(".v8")
                            || name.equalsIgnoreCase(".swf")
                            || name.equalsIgnoreCase(".m2v")
                            || name.equalsIgnoreCase(".asx")
                            || name.equalsIgnoreCase(".ra")
                            || name.equalsIgnoreCase(".ndivx")
                            || name.equalsIgnoreCase(".xvid")) {
                        VideoInfo vi = new VideoInfo();
                        vi.setFilePath(file.getAbsolutePath());//文件路径
                        vi.setDisplayName(file.getName());//文件名
                        vi.setFileId(CommonUtils.EncryptMD5(file.getAbsolutePath()));
                        list.add(vi);
                        return true;
                    }
                } else if (file.isDirectory()) {
                    getVideoFile(list, file);
                }
                return false;
            }
        });
    }

    /**
     * 设置视频桌面
     *
     * @param context
     * @param videoInfo 视频文件
     * @param isVolume  是否开启声音
     * @param isToast   是否Toast提示
     */
    public static void setVideoWallpaper(final Context context, final VideoInfo videoInfo,
            final boolean isVolume, final boolean isToast) {
        context.startService(new Intent(context, VideoWallpaperService.class));
        CommonUtils.getHandler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Const.ACTION_VIDEO_REQUEST);
                intent.putExtra(Const.EXTRA_FILE_INFO, videoInfo);
                intent.putExtra(Const.EXTRA_VOLUME, isVolume);
                intent.putExtra(Const.EXTRA_TOAST, isToast);
                context.sendBroadcast(intent);
            }
        }, sSendTime < DEFAULT_SEND_TIME ? DEFAULT_SEND_TIME : sSendTime);
    }

    /**
     * 设置视频桌面
     *
     * @param context
     * @param videoInfo
     * @param isVolume
     */
    public static void setVideoWallpaper(final Context context, final VideoInfo videoInfo,
            final boolean isVolume) {
        setVideoWallpaper(context, videoInfo, isVolume, false);
    }

    /**
     * 添加设置视频桌面响应监听
     *
     * @param listener
     */
    public static void addOnVideoWallpaperRespListener(
            VideoRespReceive.OnVideoWallpaperRespListener listener) {
        sReceive.addOnVideoWallpaperRespListener(listener);
    }

    /**
     * 移除设置视频桌面响应监听
     *
     * @param listener
     */
    public static void removeOnVideoWallpaperRespListener(
            VideoRespReceive.OnVideoWallpaperRespListener listener) {
        sReceive.removeOnVideoWallpaperRespListener(listener);
    }

    /**
     * 移除所有设置视频桌面响应监听
     */
    public static void removeAllOnVideoWallpaperRespListener() {
        sReceive.removeAllOnVideoWallpaperRespListener();
    }

}
