package com.ecaray.weibo;

import java.io.Serializable;

/**
 * 微博内容实体类
 * @author YXD
 *
 */
public class Message implements Serializable{
	
	private static final long serialVersionUID = 2789732708160004861L;

	private String uid;
	
	private  String content;
	
	private String timestamp;

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}


	@Override
	public String toString() {
		return "uid=" + uid +",timestamp=" 
				+ timestamp + ",content=\"" + content+"\"";
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
}
