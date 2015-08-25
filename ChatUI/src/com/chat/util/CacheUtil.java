package com.chat.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.chat.util.SDCardImageLoader.CompressParam;

public class CacheUtil {
    
    private static LruCache<CompressParam, Bitmap> imageCache;
    
    public static LruCache<CompressParam, Bitmap> getInstance() {
        if (imageCache == null) {
            synchronized (CacheUtil.class) {
                if (imageCache == null) {
                    // 获取应用程序最大可用内存
                    int maxMemory = (int) Runtime.getRuntime().maxMemory();
                    int cacheSize = maxMemory / 4;
                    // 设置图片缓存大小为程序最大可用内存的1/8
                    imageCache = new LruCache<CompressParam, Bitmap>(cacheSize) {
                        @Override
                        protected int sizeOf(CompressParam key, Bitmap value) {
                            return value.getRowBytes() * value.getHeight();
                        }
                    };
                }
            }
        } 
        return imageCache;
        
    }
    
    private CacheUtil() {
    }
}
