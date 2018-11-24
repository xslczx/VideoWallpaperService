package com.chrs.videowallpaperservice;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.chrs.videowallpaper.constant.StateCode;
import com.chrs.videowallpaper.data.VideoInfo;
import com.chrs.videowallpaper.receive.VideoRespReceive;
import com.chrs.videowallpaper.utils.VideoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<VideoInfo> mVideoInfos = new ArrayList<>();
    /**
     * 适配器
     */
    RecyclerView.Adapter adapter = new RecyclerView.Adapter<MyViewHolder>() {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(
                    View.inflate(parent.getContext(), R.layout.item_rv, null));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final VideoInfo videoInfo = mVideoInfos.get(position);
            Glide.with(MainActivity.this)
                    .load(Uri.fromFile(new File(videoInfo.getFilePath())))
                    .into(holder.mIv);
            holder.mTv.setText(videoInfo.getDisplayName());
            holder.mTv.setTextColor(
                    position % 3 == 0 ? Color.BLUE : position % 3 == 1 ? Color.GRAY : Color.RED);
            //点击缩略图弹出对话框，设置有/无声音的视频桌面
            holder.mIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new MaterialDialog.Builder(v.getContext())
                            .title("声音开启")
                            .titleGravity(GravityEnum.CENTER)
                            .content("是否开启声音？")
                            .negativeText("否")
                            .positiveText("是")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                                    sendReceive(videoInfo, false);
                                }
                            }).onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                @NonNull DialogAction which) {
                            sendReceive(videoInfo, true);
                        }
                    }).show();
                }
            });
            //长按缩略图弹出对话框，是否删除文件
            holder.mIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    new MaterialDialog.Builder(v.getContext())
                            .title("删除文件")
                            .titleGravity(GravityEnum.CENTER)
                            .content("是否删除该文件？")
                            .negativeText("否")
                            .positiveText("是")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                                    boolean delete = new File(videoInfo.getFilePath()).delete();
                                    if (delete) {
                                        mVideoInfos.remove(videoInfo);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(v.getContext(), "文件已删除", Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(v.getContext(), "文件删除失败", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            }).show();
                    return false;
                }
            });
            //点击文件名弹出对话框，修改文件名
            holder.mTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final EditText text = new EditText(v.getContext());
                    new MaterialDialog.Builder(v.getContext())
                            .title("修改文件名")
                            .titleGravity(GravityEnum.CENTER)
                            .customView(text, false)
                            .negativeText("取消")
                            .positiveText("确定")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                                    String name = text.getText().toString().trim();
                                    if (!TextUtils.isEmpty(name)) {
                                        String fuxName = videoInfo.getDisplayName()
                                                .substring(videoInfo.getDisplayName().indexOf("."));
                                        String displayName = name + fuxName;
                                        String filePath = videoInfo.getFilePath().substring(0,
                                                videoInfo.getFilePath().lastIndexOf("/") + 1)
                                                + displayName;
                                        File dest = new File(filePath);
                                        boolean renameTo = new File(videoInfo.getFilePath())
                                                .renameTo(dest);
                                        if (renameTo) {
                                            videoInfo.setDisplayName(displayName);
                                            videoInfo.setFilePath(filePath);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(v.getContext(), "修改失败",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(v.getContext(), "文件名不能为空",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mVideoInfos.size();
        }
    };
    //设置视频桌面响应监听
    private VideoRespReceive.OnVideoWallpaperRespListener onVideoWallpaperRespListener = new VideoRespReceive.OnVideoWallpaperRespListener() {
        @Override
        public void onVideoWallpaperResp(@Nullable VideoInfo videoInfo, int stateCode) {
            switch (stateCode) {
                case StateCode.RESP_SUCCESS:
                    Intent intent = new Intent();
                    // 为Intent设置Action、Category属性
                    intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
                    intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
                    startActivity(intent);
                    break;
                case StateCode.RESP_FAILED_NULL:
                    break;
                case StateCode.RESP_FAILED_ERROR:
                    break;
                case StateCode.RESP_FAILED_INIT:
                    break;
                case StateCode.EVENT_SINGLE_CLICK:
                    break;
                case StateCode.EVENT_DOUBLE_CLICK:
                    break;
                case StateCode.EVENT_LONG_CLICK:
                    break;
                case StateCode.EVENT_SCROLL:
                    break;
            }
            Log.i("logger", "MainActivity onVideoWallpaperResp stateCode:" + stateCode);
        }
    };
    private VideoRespReceive receiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取列表控件
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(adapter);
        //获取外部存储视频文件
        VideoUtils.getVideoFile(mVideoInfos,
                Environment.getExternalStorageDirectory());
        adapter.notifyDataSetChanged();
        receiver = new VideoRespReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BuildConfig.BROADCAST_ACTION);
        registerReceiver(receiver, filter);
        //添加设置视频桌面响应监听（回调有重复调用的情况）
        VideoUtils.addOnVideoWallpaperRespListener(onVideoWallpaperRespListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        //注意：为了防止内存泄露，一定要记得移除监听
        VideoUtils.removeOnVideoWallpaperRespListener(onVideoWallpaperRespListener);
//        VideoUtils.removeAllOnVideoWallpaperRespListener();
    }

    /**
     * 设置视频桌面
     *
     * @param videoInfo
     * @param isVolume
     */
    private void sendReceive(final VideoInfo videoInfo, final boolean isVolume) {
        //控制延迟时间（当壁纸界面为黑屏时可以适当将其设置长一点，看手机好坏了，一般一秒(1000)应该够了）
        VideoUtils.sSendTime = 1500;
        //设置视频桌面（默认Toast为false）
//        VideoUtils.setVideoWallpaper(this,file,isVolume);
        VideoUtils.setVideoWallpaper(this, videoInfo, isVolume, true);
    }

    /**
     * 适配器帮助类
     */
    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIv;
        private TextView mTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            mIv = itemView.findViewById(R.id.iv_item);
            mTv = itemView.findViewById(R.id.tv_item);
        }
    }

}
