package com.ecaray.bean;

import java.util.List;

/**
 * 列簇信息
 * @author YXD
 *
 */
public class ColFamilyInfo {
	
	private String colFamilyName;
	private List<ColInfo> clList;
	
	public String getColFamilyName() {
		return colFamilyName;
	}
	public void setColFamilyName(String colFamilyName) {
		this.colFamilyName = colFamilyName;
	}
	public List<ColInfo> getClList() {
		return clList;
	}
	public void setClList(List<ColInfo> clList) {
		this.clList = clList;
	}
	
	
}
