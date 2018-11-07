package com.chrs.videowallpaper.constant;

public interface StateCode {

    int RESP_SUCCESS = 200;         //桌面设置成功
    int RESP_FAILED_NULL = 400;     //视频文件为空
    int RESP_FAILED_ERROR = 403;    //视频播放异常
    int RESP_FAILED_INIT = -1;      //引擎无播放控件

    int EVENT_SINGLE_CLICK = 665;   //单击手势
    int EVENT_DOUBLE_CLICK = 666;   //双击手势
    int EVENT_LONG_CLICK = 667;     //长按手势
    int EVENT_SCROLL = 668;         //滑动手势
}
