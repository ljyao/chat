package com.chat.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chat.animate.AnimatedGifDrawable;
import com.chat.animate.AnimatedImageSpan;
import com.chat.ui.ChatActivity;
import com.chat.util.ChatInfo;
import com.chat.util.ChatUtil;
import com.chat.util.ScreenUtils;
import com.example.chatui.R;

import com.nineoldandroids.view.ViewHelper;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class ChatLVAdapter extends BaseAdapter {
	private Context mContext;
	private List<ChatInfo> list;
	/** 弹出的更多选择框 */
	private PopupWindow popupWindow;

	/** 复制，删除 */
	private TextView copy, delete;

	private LayoutInflater inflater;
	/**
	 * 执行动画的时间
	 */
	protected long mAnimationTime = 150;

	public ChatLVAdapter(Context mContext, List<ChatInfo> list) {
		super();
		this.mContext = mContext;
		this.list = list;
		inflater = LayoutInflater.from(mContext);
		initPopWindow();
	}

	public void setList(List<ChatInfo> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHodler hodler;
		if (convertView == null) {
			hodler = new ViewHodler();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_lv_item, null);
			hodler.fromContainer = (ViewGroup) convertView
					.findViewById(R.id.chart_from_container);
			hodler.toContainer = (ViewGroup) convertView
					.findViewById(R.id.chart_to_container);
			hodler.fromContent = (TextView) convertView
					.findViewById(R.id.chatfrom_content);
			hodler.toContent = (TextView) convertView
					.findViewById(R.id.chatto_content);
			hodler.time = (TextView) convertView.findViewById(R.id.chat_time);
			convertView.setTag(hodler);
			hodler.failIcon = (ImageView) convertView
					.findViewById(R.id.chat_failIcon);
			hodler.sendStatue = (ProgressBar) convertView
					.findViewById(R.id.chat_sendloading);
			hodler.senderId = (TextView) convertView
					.findViewById(R.id.chat_sendid);
			hodler.receiverId = (TextView) convertView
					.findViewById(R.id.chat_receiverid);
			hodler.fromIcon = (ImageView) convertView
					.findViewById(R.id.chatfrom_icon);
		} else {
			hodler = (ViewHodler) convertView.getTag();
		}

		if (list.get(position).fromOrTo == 0) {
			// 收到消息 from显示
			hodler.toContainer.setVisibility(View.GONE);
			hodler.fromContainer.setVisibility(View.VISIBLE);
			hodler.receiverId.setText(list.get(position).senderId);
			// 对内容做处理
			SpannableStringBuilder sb = handler(hodler.fromContent,
					list.get(position).content);
			hodler.fromContent.setText(sb);
			hodler.time.setText(list.get(position).time);
			// 头像
			hodler.fromIcon.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				}
			});
		} else {
			// 发送消息 to显示
			hodler.toContainer.setVisibility(View.VISIBLE);
			hodler.fromContainer.setVisibility(View.GONE);
			hodler.senderId.setText(list.get(position).senderId);
			if (list.get(position).status == ChatUtil.SENDSUCCESS) {
				hodler.sendStatue.setVisibility(View.GONE);
				hodler.failIcon.setVisibility(View.GONE);
			} else if (list.get(position).status == ChatUtil.SENDFAIL) {
				hodler.sendStatue.setVisibility(View.GONE);
				hodler.failIcon.setVisibility(View.VISIBLE);
			}
			// 对内容做处理
			SpannableStringBuilder sb = handler(hodler.toContent,
					list.get(position).content);
			hodler.toContent.setText(sb);
			hodler.time.setText(list.get(position).time);
			// ////
			hodler.toContent.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				}
			});
		}

		// 设置+按钮点击效果
		hodler.fromContent.setOnLongClickListener(new popAction(convertView,
				position, list.get(position).fromOrTo));
		hodler.toContent.setOnLongClickListener(new popAction(convertView,
				position, list.get(position).fromOrTo));
		return convertView;
	}

	private SpannableStringBuilder handler(final TextView gifTextView,
			String content) {
		SpannableStringBuilder sb = new SpannableStringBuilder(content);
		String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		boolean flag = true;
		while (m.find()) {
			flag = false;
			String tempText = m.group();

			try {

				String num = tempText.substring(
						"#[face/png/f_static_".length(), tempText.length()
								- ".png]#".length());
				String gif = "face/gif/f" + num + ".gif";
				/**
				 * 如果open这里不抛异常说明存在gif，则显示对应的gif 否则说明gif找不到，则显示png
				 * */
				InputStream is = mContext.getAssets().open(gif);
				AnimatedImageSpan drawableSpan = new AnimatedImageSpan(
						new AnimatedGifDrawable(mContext,
								ChatUtil.getImageCache(), num, is,
								new AnimatedGifDrawable.UpdateListener() {
									@Override
									public void update() {
										gifTextView.postInvalidate();
									}
								}));

				sb.setSpan(drawableSpan, m.start(), m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				is.close();
			} catch (Exception e) {
				String png = tempText.substring("#[".length(),
						tempText.length() - "]#".length());
				try {
					Bitmap bitmap = null;
					if (ChatUtil.getImageCache().get(png) != null) {
						bitmap = ChatUtil.getImageCache().get(png);
					} else {
						bitmap = BitmapFactory.decodeStream(mContext
								.getAssets().open(png));
						ChatUtil.getImageCache().put(png, bitmap);
					}
					BitmapDrawable bd = new BitmapDrawable(bitmap);
					Drawable drawable = (Drawable) bd;
					int val = ScreenUtils.dp2px(30);
					drawable.setBounds(0, 0, val, val);
					sb.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM),
							m.start(), m.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (flag) {
			return imagehandler(mContext, content);
		}
		return sb;
	}

	private SpannableStringBuilder imagehandler(Context context, String content) {
		SpannableStringBuilder sb = new SpannableStringBuilder(content);
		String regex = "(\\#\\[)\\d{20}(\\]\\#)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		while (m.find()) {
			String tempText = m.group();
			String path;
			String num = tempText.substring(2, tempText.length() - 2);
			ChatActivity cthis = (ChatActivity) context;
			File file;
			FileInputStream inStream = null;
			Bitmap bitmap = null;
			boolean flag = true;
			if (ChatUtil.getImageCache().get(num) != null) {
				bitmap = ChatUtil.getImageCache().get(num);
			} else {
				try {
					path = cthis.PHOTO_FILE_PATH + "/" + num + "copy.jpg";
					file = new File(path);
					inStream = new FileInputStream(file);
					bitmap = BitmapFactory.decodeStream(inStream);
					inStream.close();
					ChatUtil.getImageCache().put(num, bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				if (!flag)
					return sb;
				BitmapDrawable bd = new BitmapDrawable(bitmap);
				Drawable drawable = (Drawable) bd;
				int w, h;
				w = ScreenUtils.dp2px(200);
				h = Math.round((float) bitmap.getHeight()
						/ ((float) bitmap.getWidth() / (float) w));
				drawable.setBounds(0, 0, w, h);// 这里设置图片的大小
				ImageSpan imageSpan = new ImageSpan(drawable,
						ImageSpan.ALIGN_BOTTOM);
				sb.setSpan(imageSpan, m.start(), m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return sb;
	}

	/**
	 * 屏蔽listitem的所有事件
	 * */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	/**
	 * 初始化弹出的pop
	 * */
	@SuppressLint("InflateParams")
	private void initPopWindow() {
		View popView = inflater.inflate(R.layout.chat_item_copy_delete_menu,
				null);
		copy = (TextView) popView.findViewById(R.id.chat_copy_menu);
		delete = (TextView) popView.findViewById(R.id.chat_delete_menu);
		popupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new ColorDrawable(0));
		// 设置popwindow出现和消失动画
		// popupWindow.setAnimationStyle(R.style.PopMenuAnimation);
	}

	/**
	 * 显示popWindow
	 * */
	public void showPop(View parent, int x, int y, final View view,
			final int position, final int fromOrTo) {
		// 设置popwindow显示位置
		popupWindow.showAtLocation(parent, 0, x, y);
		// 获取popwindow焦点
		popupWindow.setFocusable(true);
		// 设置popwindow如果点击外面区域，便关闭。
		popupWindow.setOutsideTouchable(true);
		// 为按钮绑定事件
		// 复制
		copy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				// 获取剪贴板管理服务
				ClipboardManager cm = (ClipboardManager) mContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				// 将文本数据复制到剪贴板
				cm.setText(list.get(position).content);
			}
		});
		// 删除
		delete.setOnClickListener(new View.OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				if (fromOrTo == 0) {
					// from
					leftRemoveAnimation(view, position);
				} else if (fromOrTo == 1) {
					// to
					rightRemoveAnimation(view, position);
				}

				

				// list.remove(position);
				// notifyDataSetChanged();
			}
		});
		popupWindow.update();
		if (popupWindow.isShowing()) {

		}
	}

	/**
	 * 每个ITEM中more按钮对应的点击动作
	 * */
	public class popAction implements OnLongClickListener {
		int position;
		View view;
		int fromOrTo;

		public popAction(View view, int position, int fromOrTo) {
			this.position = position;
			this.view = view;
			this.fromOrTo = fromOrTo;
		}

		@Override
		public boolean onLongClick(View v) {
			int[] arrayOfInt = new int[2];
			// 获取点击按钮的坐标
			v.getLocationOnScreen(arrayOfInt);
			int heigh = v.getHeight();
			int x = arrayOfInt[0];
			int y = arrayOfInt[1];

			showPop(v, x, y, view, position, fromOrTo);
			return true;
		}
	}

	/**
	 * item删除动画
	 * */
	private void rightRemoveAnimation(final View view, final int position) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				mContext, R.anim.chat_to_remove_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setAlpha(0);
				performDismiss(view, position);
				animation.cancel();
			}
		});

		view.startAnimation(animation);
	}

	/**
	 * item删除动画
	 * */
	private void leftRemoveAnimation(final View view, final int position) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				mContext, R.anim.chat_from_remove_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			@SuppressLint("NewApi")
			public void onAnimationEnd(Animation animation) {
				view.setAlpha(0);
				performDismiss(view, position);
				animation.cancel();
			}
		});

		view.startAnimation(animation);
	}

	/**
	 * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
	 * 
	 * @param dismissView
	 * @param dismissPosition
	 */

	private void performDismiss(final View dismissView,
			final int dismissPosition) {
		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();// 获取item的布局参数
		final int originalHeight = dismissView.getHeight();// item的高度

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
				.setDuration(mAnimationTime);
		animator.start();

		animator.addListener(new AnimatorListenerAdapter() {
			@SuppressLint("NewApi")
			@Override
			public void onAnimationEnd(Animator animation) {
				list.remove(dismissPosition);
				notifyDataSetChanged();
				// 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
				// 所以我们在动画执行完毕之后将item设置回来
				ViewHelper.setAlpha(dismissView, 1f);
				ViewHelper.setTranslationX(dismissView, 0);
				ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
				lp.height = originalHeight;
				dismissView.setLayoutParams(lp);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});
	}

	static class ViewHodler {
		ImageView fromIcon, toIcon, failIcon;
		TextView fromContent, toContent, time, senderId, receiverId;
		ViewGroup fromContainer, toContainer;
		ProgressBar sendStatue;
	}

}
