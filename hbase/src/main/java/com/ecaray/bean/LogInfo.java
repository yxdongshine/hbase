package com.ecaray.bean;

import java.util.List;
import org.codehaus.jettison.json.JSONObject;

public class LogInfo {

	private String uid;
	private String systemId;
	private List<JSONObject> jsonList;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	public List<JSONObject> getJsonList() {
		return jsonList;
	}
	public void setJsonList(List<JSONObject> jsonList) {
		this.jsonList = jsonList;
	}
	
	
	
}
