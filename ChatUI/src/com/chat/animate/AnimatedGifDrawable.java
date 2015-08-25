package com.chat.animate;

import java.io.InputStream;

import com.chat.util.ScreenUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

public class AnimatedGifDrawable extends AnimationDrawable {

	private int mCurrentIndex = 0;
	private UpdateListener mListener;
	private Context mContext;

	public AnimatedGifDrawable(Context context,
			LruCache<String, Bitmap> imageCache, String num,
			InputStream source, UpdateListener listener) {
		mContext = context;
		mListener = listener;
		GifDecoder decoder = new GifDecoder();
		decoder.read(source);
		// Iterate through the gif frames, add each as animation frame
		for (int i = 0; i < decoder.getFrameCount(); i++) {
			String key = num + i;
			Bitmap bitmap;
			if (imageCache.get(key) != null) {
				bitmap = imageCache.get(key);
			} else {
				bitmap = decoder.getFrame(i);
				imageCache.put(key, bitmap);
			}

			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			// Explicitly set the bounds in order for the frames to display
			drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
			addFrame(drawable, decoder.getDelay(i));
			if (i == 0) {
				// Also set the bounds for this container drawable
				setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
			}
		}
	}

	/**
	 * Naive method to proceed to next frame. Also notifies listener.
	 */
	public void nextFrame() {
		mCurrentIndex = (mCurrentIndex + 1) % getNumberOfFrames();
		if (mListener != null)
			mListener.update();
	}

	/**
	 * Return display duration for current frame
	 */
	public int getFrameDuration() {
		return getDuration(mCurrentIndex);
	}

	/**
	 * Return drawable for current frame
	 */
	public Drawable getDrawable() {
		Drawable drawable;
		drawable = getFrame(mCurrentIndex);
		int val = ScreenUtils.dp2px(30,(Activity)mContext);
		drawable.setBounds(0, 0, 80, 80);
		return drawable;
	}

	/**
	 * Interface to notify listener to update/redraw Can't figure out how to
	 * invalidate the drawable (or span in which it sits) itself to force redraw
	 */
	public interface UpdateListener {
		void update();
	}

}