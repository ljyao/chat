package com.chat.adapter;

import java.util.ArrayList;
import com.chat.util.SDCardImageLoader;
import com.chat.util.ScreenUtils;
import com.example.chatui.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PhotoWallAdapter extends BaseAdapter {
    private Context context;
    
    private ArrayList<String> imagePathList = null;
    
    private SDCardImageLoader loader;
    
    private SparseBooleanArray selectionMap;
    
    public PhotoWallAdapter(Context context, ArrayList<String> imagePathList) {
        this.context = context;
        this.imagePathList = imagePathList;
        
        loader = new SDCardImageLoader(ScreenUtils.getScreenW(), ScreenUtils.getScreenH());
        selectionMap = new SparseBooleanArray();
    }
    
    @Override
    public int getCount() {
        return imagePathList == null ? 0 : imagePathList.size();
    }
    
    @Override
    public Object getItem(int position) {
        return imagePathList.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String filePath = (String) getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_photo_wall_item, null);
            holder = new ViewHolder();
            
            holder.imageView = (ImageView) convertView.findViewById(R.id.photo_wall_item_photo);
            holder.checkBox = (ImageView) convertView.findViewById(R.id.photo_wall_item_cb);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = selectionMap.get(position);
                if (isChecked) {
                    isChecked = false;
                    holder.checkBox.setVisibility(View.INVISIBLE);
                }
                else {
                    isChecked = true;
                    holder.checkBox.setVisibility(View.VISIBLE);
                }
                selectionMap.put(position, isChecked);
                if (isChecked) {
                    holder.imageView.setColorFilter(context.getResources().getColor(R.color.image_checked_bg));
                }
                else {
                    holder.imageView.setColorFilter(null);
                }
                
            }
        });
        if (selectionMap.get(position)) {
            holder.checkBox.setVisibility(View.VISIBLE);
        }
        else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        
        holder.imageView.setTag(filePath);
        
        loader.loadImage(true, 50, filePath, holder.imageView);
        return convertView;
    }
    
    private class ViewHolder {
        ImageView imageView;
        
        ImageView checkBox;
    }
    
    public SparseBooleanArray getSelectionMap() {
        return selectionMap;
    }
    
    public void clearSelectionMap() {
        selectionMap.clear();
    }
}
