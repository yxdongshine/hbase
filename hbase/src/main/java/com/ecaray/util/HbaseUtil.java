package com.ecaray.util;

import org.apache.hadoop.hbase.util.Bytes;

import com.ecaray.bean.LogCondition;
import com.ecaray.bean.LogInfo;
import com.ecaray.constant.Constant;

public class HbaseUtil {
	
	public static String buildRowkey(LogInfo logInfo){
		return logInfo.getUid().trim()+Constant.SPLIT_UNDERLINE+logInfo.getSystemId().trim();
	}
	
	public static byte[] buildRowkey(LogCondition logCondition){
		return Bytes.toBytes(logCondition.getUid().trim()+Constant.SPLIT_UNDERLINE+logCondition.getSystemId().trim());
	}
	
	public static synchronized Long getSystemTime(){
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return System.currentTimeMillis();
	}
}
