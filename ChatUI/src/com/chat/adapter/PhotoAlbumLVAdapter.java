package com.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import com.chat.util.PhotoAlbumLVItem;
import com.chat.util.SDCardImageLoader;
import com.chat.util.ScreenUtils;
import com.chat.util.SDCardImageLoader.CompressParam;
import com.example.chatui.R;

public class PhotoAlbumLVAdapter extends BaseAdapter {
    private Context context;
    
    private ArrayList<PhotoAlbumLVItem> list;
    
    private SDCardImageLoader loader;
    
    public PhotoAlbumLVAdapter(Context context, ArrayList<PhotoAlbumLVItem> list) {
        this.context = context;
        this.list = list;
        
        loader = new SDCardImageLoader(context);
    }
    
    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }
    
    @Override
    public Object getItem(int position) {
        return null;
    }
    
    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_photo_album_lv_item, null);
            holder = new ViewHolder();
            
            holder.firstImageIV = (ImageView) convertView.findViewById(R.id.select_img_gridView_img);
            holder.pathNameTV = (TextView) convertView.findViewById(R.id.select_img_gridView_path);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        String filePath = list.get(position).getFirstImagePath();
        holder.firstImageIV.setTag(filePath);
        CompressParam param = new CompressParam();
        param.type = CompressParam.TYPE_DP;
        param.smallSize = 50;
        param.filePath = filePath;
        loader.loadImage(param, holder.firstImageIV);
        holder.pathNameTV.setText(getPathNameToShow(list.get(position)));
        
        return convertView;
    }
    
    private class ViewHolder {
        ImageView firstImageIV;
        
        TextView pathNameTV;
    }
    
    private String getPathNameToShow(PhotoAlbumLVItem item) {
        String absolutePath = item.getPathName();
        int lastSeparator = absolutePath.lastIndexOf(File.separator);
        return absolutePath.substring(lastSeparator + 1) + "(" + item.getFileCount() + ")";
    }
    
}
