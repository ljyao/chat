package com.chat.util;

import java.io.Serializable;


@SuppressWarnings("serial")
public class ChatInfo implements Serializable, Comparable<ChatInfo> {
	public int recordId;
	public int iconFromResId;
	public String content;
	public String time;
	public String senderId;
	public String receiverId;
	public int status = ChatUtil.waiting;
	public int fromOrTo;// 0 是收到的消息，1是发送的消息

	@Override
	public String toString() {
		return "ChatInfoEntity [iconFromResId=" + iconFromResId
				+ ", iconFromUrl=" + ", content=" + content + ", time=" + time
				+ ", fromOrTo=" + fromOrTo + "]";
	}

	@Override
	public int compareTo(ChatInfo another) {
		return this.time.compareTo(another.time);
	}
}
