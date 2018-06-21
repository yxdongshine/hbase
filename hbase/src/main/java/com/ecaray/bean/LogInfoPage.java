package com.ecaray.bean;

import java.util.List;

/**
 * 日志查询条件
 * @author YXD
 *
 */
public class LogInfoPage extends LogInfo{

	private String startTime;
	private String endTime;
	private List<String> columnList;//获取指定的列 为了提高查询效率 可不传
	private int pageIndex = 1;//默认第一页
	private int pageSize = 50;//默认每页10条
	private Boolean isPage = false ;// 默认不分页

	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
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
	public List<String> getColumnList() {
		return columnList;
	}
	public void setColumnList(List<String> columnList) {
		this.columnList = columnList;
	}
	public Boolean getIsPage() {
		return isPage;
	}
	public void setIsPage(Boolean isPage) {
		this.isPage = isPage;
	}
	
}
