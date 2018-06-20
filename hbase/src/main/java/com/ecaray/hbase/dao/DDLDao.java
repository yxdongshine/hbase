package com.ecaray.hbase.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.ecaray.bean.ColFamilyInfo;
import com.ecaray.bean.ColInfo;
import com.ecaray.bean.RowInfo;
import com.ecaray.util.HbaseUtil;
import com.ecaray.util.MD5;

public class DDLDao {

	/**
	 * 批量构建puts
	 * @param row
	 * @author YXD
	 */
	public List<Put> buildPuts(List<RowInfo> rowList){
		List<Put> puts = new ArrayList<Put>();
		for (int k = 0; k < rowList.size(); k++) {
			RowInfo row = rowList.get(k);
			//rowkey 采用md5 散裂化 分散regionserver压力
			//String rowKey = MD5.MD5Encode(row.getRowKey());
			String rowKey = row.getRowKey();
			List<ColFamilyInfo> cfList = row.getCfList();
			long time = HbaseUtil.getSystemTime();
			for (int i = 0; i < cfList.size(); i++) {
				ColFamilyInfo cfInfo = cfList.get(i);
				String cfName = cfInfo.getColFamilyName();
				List<ColInfo> cList = cfInfo.getClList();
				for (int j = 0; j < cList.size(); j++) {
					ColInfo cInfo = cList.get(j);
					String key = cInfo.getColKey();
					String value = cInfo.getColVaue();
					Put put = new Put(Bytes.toBytes(rowKey));
					put.add(Bytes.toBytes(cfName),Bytes.toBytes(key),time,Bytes.toBytes(value));
					puts.add(put);
				}
			}
		}
		return puts;
	}
	
	/**
	 * 批量构建Delete
	 * @param rowList
	 * @return
	 * @author YXD
	 */
	public List<Delete> buildDelete(List<RowInfo> rowList){
		List<Delete> deletes = new ArrayList<Delete>();
		for (int k = 0; k < rowList.size(); k++) {
			RowInfo row = rowList.get(k);
			//rowkey 采用md5 散裂化 分散regionserver压力
			//String rowKey = MD5.MD5Encode(row.getRowKey());
			String rowKey = row.getRowKey();
			Delete delete = new Delete(Bytes.toBytes(rowKey));
			List<ColFamilyInfo> cfList = row.getCfList();
			for (int i = 0; i < cfList.size(); i++) {
				ColFamilyInfo cfInfo = cfList.get(i);
				String cfName = cfInfo.getColFamilyName();
				List<ColInfo> cList = cfInfo.getClList();
				for (int j = 0; j < cList.size(); j++) {
					ColInfo cInfo = cList.get(j);
					String key = cInfo.getColKey();
					String value = cInfo.getColVaue();
					delete.deleteColumn(Bytes.toBytes(key), Bytes.toBytes(value));
				}
			}
			deletes.add(delete);
		}	
		return deletes;
	}

	/**
	 * 根据rowkey获取
	 * @param List<RowInfo>
	 * @author YXD
	 */
	public List<Get> buildGets(List<RowInfo> rowList){
		List<Get> getList = new ArrayList<Get>();
		for (Iterator iterator = rowList.iterator(); iterator.hasNext();) {
			RowInfo rowInfo = (RowInfo) iterator.next();
			//rowkey 采用md5 散裂化 分散regionserver压力
			//String rowKey = MD5.MD5Encode(row.getRowKey());
			String rowKey = rowInfo.getRowKey();
			Get get = new Get(Bytes.toBytes(rowKey));
			try {
				get.setMaxVersions(Integer.MAX_VALUE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getList.add(get);
		}
		return getList;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String rowKey = MD5.MD5Encode("123456");
		System.out.println(rowKey.length());
		System.out.println(Bytes.toBytes(rowKey).length);
	} 
}
