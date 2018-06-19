package com.ecaray.bean;

import java.util.List;

/**
 * 整行记录信息
 * @author YXD
 */
public class RowInfo {
	//采用uid_systemid
	private String rowKey;
	private List<ColFamilyInfo> CfList ;
	
	public String getRowKey() {
		return rowKey;
	}
	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}
	public List<ColFamilyInfo> getCfList() {
		return CfList;
	}
	public void setCfList(List<ColFamilyInfo> cfList) {
		CfList = cfList;
	}
	
	
}
