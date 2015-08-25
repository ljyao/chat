package com.chat.ui;

import java.util.ArrayList;

import com.chat.adapter.PreviewPhotoAdapter;
import com.example.chatui.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

public class PreviewActivity extends Activity {
    private TextView titleTV;
    
    private ArrayList<String> mPhotoLists;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_preview_activity);
        titleTV = (TextView) findViewById(R.id.topbar_title_tv);
        titleTV.setText(R.string.latest_image);
        
        mPhotoLists = (ArrayList<String>) getIntent().getSerializableExtra("List");
        
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpage);
        PagerAdapter adapter = new PreviewPhotoAdapter(mPhotoLists, this);
        viewPager.setAdapter(adapter);
    }
    
}
