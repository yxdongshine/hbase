package com.ecaray.util;

import com.ecaray.bean.LogInfo;
import com.ecaray.constant.Constant;

public class HbaseUtil {
	
	public static String buildRowkey(LogInfo logInfo){
		return logInfo.getUid().trim()+Constant.SPLIT_UNDERLINE+logInfo.getSystemId().trim();
	}
	
}
