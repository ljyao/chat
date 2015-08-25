package com.chat.util;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.chatui.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

/**
 * 从SDCard异步加载图片
 */
public class SDCardImageLoader {
    // 缓存
    private  LruCache<CompressParam, Bitmap> imageCache;
    
    private long counter = 0;
    
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    
    private static final int KEEP_ALIVE = 1;
    
    private ExecutorService executorService;
    
    private Handler handler;
    
    private int screenW, screenH;
    
    private BlockingQueue<Runnable> blockingDeque;
    
    private Context mContext;
    
    public SDCardImageLoader(Context context) {
        this.screenW = ScreenUtils.getScreenW((Activity) context);
        this.screenH = ScreenUtils.getScreenH((Activity) context);
        mContext = context;
        handler = new Handler();
        
        blockingDeque = new PriorityBlockingQueue<Runnable>(1000, new Comparator<Runnable>() {
            @Override
            public int compare(Runnable lhs, Runnable rhs) {
                if (lhs instanceof MyRunnable && rhs instanceof MyRunnable) {
                    MyRunnable l = (MyRunnable) lhs;
                    MyRunnable r = (MyRunnable) rhs;
                    return r.compareTo(l);
                }
                return 0;
                
            }
        });
        
        executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, blockingDeque);
        
       imageCache=CacheUtil.getInstance();
        
    }
    
    /**
     * @param smallSize
     * @param filePath
     * @param callback
     * @return
     */
    public Bitmap loadDrawable(CompressParam compressParam, final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        
        if (imageCache.get(compressParam) != null) {
            return imageCache.get(compressParam);
        }
        // 如果缓存没有则读取SD卡
        MyRunnable runnable = new MyRunnable(compressParam, callback);
        executorService.submit(runnable);
        return null;
    }
    
    public class MyRunnable implements Runnable, Comparable<MyRunnable> {
        
        private final long priority;
        
        CompressParam mParam;
        
        ImageCallback callback;
        
        @Override
        public int compareTo(MyRunnable another) {
            return priority > another.priority ? 1 : -1;
        }
        
        public MyRunnable(CompressParam compressParam, final ImageCallback callback) {
            this.callback = callback;
            mParam = compressParam;
            counter++;
            priority = counter;
        }
        
        @Override
        public void run() {
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mParam.filePath, opt);
                
                // 获取到这个图片的原始宽度和高度
                int picWidth = opt.outWidth;
                int picHeight = opt.outHeight;
                
                // 读取图片失败时直接返回
                if (picWidth == 0 || picHeight == 0) {
                    return;
                }
                if (mParam.type == CompressParam.TYPE_NOMAL) {
                    opt.inSampleSize = 1;
                }
                else if (mParam.type == CompressParam.TYPE_DP) {
                    int w = ScreenUtils.dp2px(mParam.smallSize, (Activity) mContext);
                    opt.inSampleSize = Math.round((float) picWidth / (float) w);
                }
                else if (mParam.type == CompressParam.TYPE_SCREEN) {
                    // 根据屏的大小和图片大小计算出缩放比例
                    if (picWidth > picHeight) {
                        if (picWidth > screenW)
                            opt.inSampleSize = picWidth / screenW;
                    }
                    else {
                        if (picHeight > screenH)
                            opt.inSampleSize = picHeight / screenH;
                    }
                }
                // 这次再真正地生成一个有像素的，经过缩放了的bitmap
                opt.inJustDecodeBounds = false;
                final Bitmap bmp = BitmapFactory.decodeFile(mParam.filePath, opt);
                // 存入map
                imageCache.put(mParam, bmp);
                
                handler.post(new Runnable() {
                    public void run() {
                        callback.imageLoaded(bmp);
                    }
                });
                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        
    }
    

    public void loadImage(final CompressParam compressParam, final ImageView imageView) {
        
        Bitmap bmp = loadDrawable(compressParam, new ImageCallback() {
            
            @Override
            public void imageLoaded(Bitmap bmp) {
                if (imageView.getTag().equals(compressParam.filePath)) {
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                    }
                    else {
                        imageView.setImageResource(R.drawable.ic_chat_empty_photo);
                    }
                }
            }
        });
        
        if (bmp != null) {
            if (imageView.getTag().equals(compressParam.filePath)) {
                imageView.setImageBitmap(bmp);
            }
        }
        else {
            imageView.setImageResource(R.drawable.ic_chat_empty_photo);
        }
    }
    
    public static class CompressParam {
        /**
         * TYPE_NOMAL:不压缩
         * 
         * @since Ver 1.1
         */
        public static final int TYPE_NOMAL = 1;
        
        /**
         * TYPE_DP:压缩成smallSize dp宽度
         * 
         * @since Ver 1.1
         */
        public static final int TYPE_DP = 2;
        
        /**
         * TYPE_SCREEN:按屏幕比例压缩
         * 
         * @since Ver 1.1
         */
        public static final int TYPE_SCREEN = 3;
        
        public int type;
        
        public int smallSize = 1;
        
        public String filePath = "";
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof CompressParam)) {
                return false;
            }
            CompressParam param = (CompressParam) o;
            if (type == param.type && smallSize == param.smallSize && filePath.equals(param.filePath)) {
                return true;
            }
            else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return type + smallSize + filePath.hashCode();
        }
    }
    
    // 对外界开放的回调接口
    public interface ImageCallback {
        // 注意 此方法是用来设置目标对象的图像资源
        public void imageLoaded(Bitmap imageDrawable);
    }
}
