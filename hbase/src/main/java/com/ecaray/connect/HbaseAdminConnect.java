package com.ecaray.connect;

import java.io.IOException;

import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import com.ecaray.constant.Constant;

public class HbaseAdminConnect {

	private HbaseAdminConnect(){}
	
	private static HBaseAdmin hBaseAdmin = null;
	
	/**
	 * 获取HBaseAdmin连接
	 * @return
	 * @author YXD
	 */
	public static HBaseAdmin getHbaseAdminConnectInstance(){
		if(null == hBaseAdmin){
			try {
				hBaseAdmin = new HBaseAdmin(Constant.conf);
			} catch (MasterNotRunningException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ZooKeeperConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hBaseAdmin;
	}
	
	/**
	 * 关闭HBaseAdmin
	 * 
	 * @author YXD
	 */
	public static void colseConnectInstance(){
		if(null != hBaseAdmin){
			try {
				hBaseAdmin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
