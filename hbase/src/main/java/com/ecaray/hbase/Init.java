package com.ecaray.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import com.ecaray.connect.HbaseAdminConnect;
import com.ecaray.constant.Constant;

public class Init {
	/**
	 * 初始化命名空间
	 */
	public static void initNameSpace(){
		HBaseAdmin admin = HbaseAdminConnect.getHbaseAdminConnectInstance();
		try {
			//先删除namespace
			admin.deleteNamespace(Constant.NAMESPACE);
			NamespaceDescriptor descriptor = NamespaceDescriptor.create(Constant.NAMESPACE)
					.addConfiguration("creator", "ecaray")
					.addConfiguration("createTime", System.currentTimeMillis()+"").build();
			admin.createNamespace(descriptor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HbaseAdminConnect.colseConnectInstance();
		}
	}
	
	
	public static void main(String[] args) {
		initNameSpace();
	}
}
