package com.chat.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import com.chat.adapter.PhotoAlbumLVAdapter;
import com.chat.util.PhotoAlbumLVItem;
import com.chat.util.Utility;
import com.example.chatui.R;

/**
 * 分相册查看SD卡所有图片。
 */
public class PhotoAlbumActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat_photo_album);

        if (!Utility.isSDcardOK()) {
            Utility.showToast(this, "SD卡不可用。");
            return;
        }

        Intent t = getIntent();
        if (!t.hasExtra("latest_count")) {
            return;
        }

        TextView titleTV = (TextView) findViewById(R.id.topbar_title_tv);
        titleTV.setText(R.string.select_album);

        Button cancelBtn = (Button) findViewById(R.id.topbar_right_btn);
        cancelBtn.setText(R.string.main_cancel);
        cancelBtn.setVisibility(View.VISIBLE);

        ListView listView = (ListView) findViewById(R.id.select_img_listView);

        // //第一种方式：使用file
        // File rootFile = new File(Utility.getSDcardRoot());
        // //屏蔽/mnt/sdcard/DCIM/.thumbnails目录
        // String ignorePath = rootFile + File.separator + "DCIM" +
        // File.separator + ".thumbnails";
        // getImagePathsByFile(rootFile, ignorePath);

        // 第二种方式：使用ContentProvider。（效率更高）
        final ArrayList<PhotoAlbumLVItem> list = new ArrayList<PhotoAlbumLVItem>();
        // “最近照片”
        list.add(new PhotoAlbumLVItem(getResources().getString(
                R.string.latest_image), t.getIntExtra("latest_count", -1), t
                .getStringExtra("latest_first_img")));
        // 相册
        list.addAll(getImagePathsByContentProvider());

        PhotoAlbumLVAdapter adapter = new PhotoAlbumLVAdapter(this, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent intent = new Intent(PhotoAlbumActivity.this,
                        PhotoWallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                // 第一行为“最近照片”
                if (position == 0) {
                    intent.putExtra("code", 200);
                } else {
                    intent.putExtra("code", 100);
                    intent.putExtra("folderPath", list.get(position)
                            .getPathName());
                }
                startActivity(intent);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消，回到主页面
                backAction();
            }
        });
    }

    /**
     * 点击返回时，回到聊天界面
     */
    private void backAction() {
        Intent intent = new Intent(PhotoAlbumActivity.this,
                ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // 重写返回键
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 获取目录中图片的个数。
     */
    private int getImageCount(File folder) {
        int count = 0;
        File[] files = folder.listFiles();
        for (File file : files) {
            if (Utility.isImage(file.getName())) {
                count++;
            }
        }

        return count;
    }

    /**
     * 获取目录中最新的一张图片的绝对路径。
     */
    private String getFirstImagePath(File folder) {
        File[] files = folder.listFiles();
        for (int i = files.length - 1; i >= 0; i--) {
            File file = files[i];
            if (Utility.isImage(file.getName())) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    /**
     * 使用ContentProvider读取SD卡所有图片。
     */
    private ArrayList<PhotoAlbumLVItem> getImagePathsByContentProvider() {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
        String key_DATA = MediaStore.Images.Media.DATA;

        ContentResolver mContentResolver = getContentResolver();

        // 只查询jpg和png的图片
        Cursor cursor = mContentResolver.query(mImageUri,
                new String[] { key_DATA }, key_MIME_TYPE + "=? or "
                        + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
                new String[] { "image/jpg", "image/jpeg", "image/png" },
                MediaStore.Images.Media.DATE_MODIFIED);

        ArrayList<PhotoAlbumLVItem> list = null;
        if (cursor != null) {
            if (cursor.moveToLast()) {
                // 路径缓存，防止多次扫描同一目录
                HashSet<String> cachePath = new HashSet<String>();
                list = new ArrayList<PhotoAlbumLVItem>();

                while (true) {
                    // 获取图片的路径
                    String imagePath = cursor.getString(0);

                    File parentFile = new File(imagePath).getParentFile();
                    String parentPath = parentFile.getAbsolutePath();

                    // 不扫描重复路径
                    if (!cachePath.contains(parentPath)) {
                        list.add(new PhotoAlbumLVItem(parentPath,
                                getImageCount(parentFile),
                                getFirstImagePath(parentFile)));
                        cachePath.add(parentPath);
                    }

                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }

            cursor.close();
        }

        return list;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    // /**
    // * 使用File读取SD卡所有图片。
    // */
    // private void getImagePathsByFile(File file, String ignorePath) {
    // if (file.isFile()) {
    // File parentFile = file.getParentFile();
    // String parentFilePath = parentFile.getAbsolutePath();
    //
    // if (cachePath.contains(parentFilePath)) {
    // return;
    // }
    //
    // if (isImage(file.getName())) {
    // list.add(new SelectImgGVItem(parentFilePath, getImageCount(parentFile),
    // getFirstImagePath(parentFile)));
    // cachePath.add(parentFilePath);
    // }
    // } else {
    // String absolutePath = file.getAbsolutePath();
    // //屏蔽文件夹
    // if (absolutePath.equals(ignorePath)) {
    // return;
    // }
    //
    // //不读取缩略图
    // if (absolutePath.contains("thumb")) {
    // return;
    // }
    //
    // //不读取层级超过5的
    // if (Utility.countMatches(absolutePath, File.separator) > 5) {
    // return;
    // }
    //
    // //不读取路径包含.的和隐藏文件
    // if (file.getName().contains(".")) {
    // return;
    // }
    //
    // File[] childFiles = file.listFiles();
    // if (childFiles != null) {
    // for (File childFile : childFiles) {
    // getImagePathsByFile(childFile, ignorePath);
    // }
    // }
    // }
    // }
}
