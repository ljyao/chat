package com.chat.adapter;

import java.util.ArrayList;

import com.chat.util.SDCardImageLoader;
import com.chat.util.SDCardImageLoader.CompressParam;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PreviewPhotoAdapter extends PagerAdapter {
    
    private ArrayList<String> mPhotoLists;
    
    private ArrayList<ImageView> views;
    
    private Context mContext;
    
    public PreviewPhotoAdapter(ArrayList<String> photoLists, Context context) {
        mPhotoLists = photoLists;
        mContext = context;
        SDCardImageLoader cardImageLoader = new SDCardImageLoader(mContext);
        views = new ArrayList<ImageView>();
        for (String filePath : photoLists) {
            ImageView imageView = new ImageView(mContext);
            imageView.setTag(filePath);
            CompressParam param = new CompressParam();
            param.type = CompressParam.TYPE_SCREEN;
            param.filePath = filePath;
            cardImageLoader.loadImage(param, imageView);
            views.add(imageView);
        }
        
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView(views.get(position));
        return views.get(position);
        
    }
    
    @Override
    public int getCount() {
        return mPhotoLists == null ? 0 : mPhotoLists.size();
    }
    
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }
}
