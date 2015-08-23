package com.chat.util;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.chatui.R;

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
    private LruCache<String, Bitmap> imageCache;
    
    private long counter = 0;
    
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    
    private static final int KEEP_ALIVE = 1;
    
    private ExecutorService executorService;
    
    private Handler handler;
    
    private int screenW, screenH;
    
    private BlockingQueue<Runnable> blockingDeque;
    
    public SDCardImageLoader(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
        
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
        
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        
        // 设置图片缓存大小为程序最大可用内存的1/8
        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        
    }
    
    /**
     * @param smallRate
     * @param filePath
     * @param callback
     * @return
     */
    public Bitmap loadDrawable(final boolean isDp, final int smallRate, final String filePath, final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        
        if (imageCache.get(filePath) != null) {
            return imageCache.get(filePath);
        }
        // 如果缓存没有则读取SD卡
        MyRunnable runnable = new MyRunnable(isDp, smallRate, filePath, callback);
        executorService.submit(runnable);
        return null;
    }
    
    public class MyRunnable implements Runnable, Comparable<MyRunnable> {
        
        private final long priority;
        
        boolean isDp;
        
        int smallRate;
        
        String filePath;
        
        ImageCallback callback;
        
        @Override
        public int compareTo(MyRunnable another) {
            return priority > another.priority ? 1 : -1;
        }
        
        public MyRunnable(final boolean isDp, final int smallRate, final String filePath, final ImageCallback callback) {
            this.callback = callback;
            this.filePath = filePath;
            this.isDp = isDp;
            this.smallRate = smallRate;
            counter++;
            priority = counter;
        }
        
        @Override
        public void run() {
            
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, opt);
                
                // 获取到这个图片的原始宽度和高度
                int picWidth = opt.outWidth;
                int picHeight = opt.outHeight;
                
                // 读取图片失败时直接返回
                if (picWidth == 0 || picHeight == 0) {
                    return;
                }
                
                // 初始压缩比例
                opt.inSampleSize = smallRate;
                if (!isDp) {
                    
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
                else {
                    int w = ScreenUtils.dp2px(smallRate);
                    opt.inSampleSize = Math.round((float) picWidth / (float) w);
                }
                
                // 这次再真正地生成一个有像素的，经过缩放了的bitmap
                opt.inJustDecodeBounds = false;
                final Bitmap bmp = BitmapFactory.decodeFile(filePath, opt);
                // 存入map
                imageCache.put(filePath, bmp);
                
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
    
    /**
     * 异步读取SD卡图片，并按指定的比例进行压缩（最大不超过屏幕像素数）
     * 
     * @param smallRate
     *            压缩比例，不压缩时输入1，此时将按屏幕像素数进行输出
     * @param filePath
     *            图片在SD卡的全路径
     * @param imageView
     *            组件
     */
    public void loadImage(boolean isDp, int smallRate, final String filePath, final ImageView imageView) {
        
        Bitmap bmp = loadDrawable(isDp, smallRate, filePath, new ImageCallback() {
            
            @Override
            public void imageLoaded(Bitmap bmp) {
                if (imageView.getTag().equals(filePath)) {
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
            if (imageView.getTag().equals(filePath)) {
                imageView.setImageBitmap(bmp);
            }
        }
        else {
            imageView.setImageResource(R.drawable.ic_chat_empty_photo);
        }
        
    }
    
    // 对外界开放的回调接口
    public interface ImageCallback {
        // 注意 此方法是用来设置目标对象的图像资源
        public void imageLoaded(Bitmap imageDrawable);
    }
}
