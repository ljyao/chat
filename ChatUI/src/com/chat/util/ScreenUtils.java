package com.chat.util;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenUtils {
    private static int screenW;
    
    private static int screenH;
    
    private static float screenDensity;
    
    private static void init(Activity mActivity) {
        DisplayMetrics metric = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenW = metric.widthPixels;
        screenH = metric.heightPixels;
        screenDensity = metric.density;
    }
    
    public static int getScreenW(Activity mActivity) {
        if (screenW == 0) {
            init(mActivity);
        }
        return screenW;
    }
    
    public static int getScreenH(Activity mActivity) {
        if (screenH == 0) {
            init(mActivity);
        }
        return screenH;
    }
    
    public static float getScreenDensity(Activity mActivity) {
        if (screenDensity == 0) {
            init(mActivity);
        }
        return screenDensity;
    }
    
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * 
     * @param mActivity
     */
    public static int dp2px(float dpValue, Activity mActivity) {
        return (int) (dpValue * getScreenDensity(mActivity) + 0.5f);
    }
    
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * 
     * @param mActivity
     */
    public static int px2dp(float pxValue, Activity mActivity) {
        return (int) (pxValue / getScreenDensity(mActivity) + 0.5f);
    }
}
