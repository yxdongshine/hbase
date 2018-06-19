package com.ecaray.hbase.dao;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.util.Bytes;
import com.ecaray.connect.HbaseAdminConnect;
import com.ecaray.constant.Constant;

/**
 * 创建表类
 * @author YXD
 *
 */
public class DMLDao {

	/**
	 * 创建表
	 * @param tableName
	 * @param familyCol
	 * @author YXD
	 */
	public void createTable(String tableName,List<String> familyCol){
		byte[] tableNameBytes = null;
		List<byte[]> familyColBytes = null;
		if(null == tableName 
				|| "".equals(tableName.trim())) tableNameBytes = Constant.OPERATION_LOG;
		else tableNameBytes = Bytes.toBytes(Constant.NAMESPACE+Constant.SPLIT_COLON+tableName);
	    
		if(null == familyCol
				|| 0 == familyCol.size()){
			byte[] familyCol1 = Bytes.toBytes(Constant.COL_FAMILY);
			familyColBytes.add(familyCol1);
		}else{
			for (int i = 0; i < familyCol.size(); i++) {
				byte[] familyCol1 = Bytes.toBytes(familyCol.get(i));
				familyColBytes.add(familyCol1);
			}
		}
		try {
			/**
			 * 用户操作日志表
			 * 
			 */
			HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableNameBytes));
			for (int i = 0; i < familyColBytes.size(); i++) {
				HColumnDescriptor family = new HColumnDescriptor(familyColBytes.get(i));
				// 开启列簇 -- store的块缓存
				//family.setBlockCacheEnabled(true);
				//family.setBlocksize(1024*1024*2);
				
				family.setMaxVersions(Integer.MAX_VALUE);
				family.setMinVersions(Integer.MAX_VALUE);

				family.setInMemory(true);//优先存储这个列簇
				desc.addFamily(family);
			}
			HbaseAdminConnect.getHbaseAdminConnectInstance().createTable(desc);
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			HbaseAdminConnect.colseConnectInstance();
		}
	}
	
	
	/**
	 * 删除表
	 * @param tableName
	 * @author YXD
	 */
	public void deleteTable(String tableName){
		byte[] tableNameBytes = null;
		if(null == tableName 
				|| "".equals(tableName.trim())) tableNameBytes = Constant.OPERATION_LOG;
		else tableNameBytes = Bytes.toBytes(Constant.NAMESPACE+Constant.SPLIT_COLON+tableName);
		//先禁用表 再删除
		try {
			HbaseAdminConnect.getHbaseAdminConnectInstance().disableTable(tableNameBytes);
			HbaseAdminConnect.getHbaseAdminConnectInstance().deleteTable(tableNameBytes);
			HbaseAdminConnect.colseConnectInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HbaseAdminConnect.colseConnectInstance();
		}
	}

}
