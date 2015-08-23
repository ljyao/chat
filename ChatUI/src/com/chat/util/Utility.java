package com.chat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

	/**
	 * 判断SD卡是否可用
	 */
	public static boolean isSDcardOK() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD卡跟路径。SD卡不可用时，返回null
	 */
	public static String getSDcardRoot() {
		if (isSDcardOK()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		return null;
	}

	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int msgId) {
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}

	/** 获取字符串中某个字符串出现的次数。 */
	public static int countMatches(String res, String findString) {
		if (res == null) {
			return 0;
		}

		if (findString == null || findString.length() == 0) {
			throw new IllegalArgumentException(
					"The param findString cannot be null or 0 length.");
		}

		return (res.length() - res.replace(findString, "").length())
				/ findString.length();
	}

	/** 判断该文件是否是一个图片。 */
	public static boolean isImage(String fileName) {
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".png");
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static String CreatImageName() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		str += (int) (Math.random() * 1000000) + "";
		return str;
	}
}
