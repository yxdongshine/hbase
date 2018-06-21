package com.ecaray.bean;

import java.util.List;

public class LogListPage {
	
	private int pageIndex;
	private int pageSize;
	private Integer totalNum;
	private List<LogInfo> logList;
	
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}
	public List<LogInfo> getLogList() {
		return logList;
	}
	public void setLogList(List<LogInfo> logList) {
		this.logList = logList;
	}
}
