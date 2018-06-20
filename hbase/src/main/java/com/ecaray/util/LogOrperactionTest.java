package com.ecaray.util;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import com.ecaray.bean.LogCondition;
import com.ecaray.bean.LogInfo;
import com.ecaray.constant.Constant;
import com.ecaray.hbase.dao.impl.LogOperationDao;

public class LogOrperactionTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		LogOperationDao loDao = new LogOperationDao();
		//loDao.add(buildLogInfoList());
		//实体查询
		/*LogInfo logInfo = new LogInfo();
		logInfo.setUid("100001011492509");
		logInfo.setSystemId("20180528001948300381461684866578");
		List<LogInfo> logInfoList = loDao.query(logInfo);*/
		//列表查询
		LogCondition logCondition = new LogCondition();
		logCondition.setUid("100001011492509");
		logCondition.setSystemId("20180528001948300381461684866578");
		logCondition.setStartTime("1529483100000");
		logCondition.setEndTime("1529483460000");
		List<LogInfo> logInfoList = loDao.queryList(logCondition);
		for (int i = 0; i < logInfoList.size(); i++) {
			LogInfo logInfo2 = logInfoList.get(i);
			String str = logInfo2.getUid()+"_"+logInfo2.getSystemId()+" ";
			List<JSONObject> jList = logInfo2.getJsonList();
			for (int j = 0; j < jList.size(); j++) {
				JSONObject jobj = jList.get(j);
				str += jobj.get(Constant.COL_KEY)+":"+jobj.get(Constant.COL_VALUE)+":"+jobj.get(Constant.COL_TIME);
			}
			System.out.println(str);
		}
		//删除
		/*List<LogInfo> logList = new ArrayList<LogInfo>();
		logList.add(logInfo);
		loDao.delete(logList);*/
	}
	
	public static List<LogInfo> buildLogInfoList() throws Exception{
		
		List<LogInfo> logList = new ArrayList<LogInfo>();
		for (int i = 0; i < 5; i++) {
			LogInfo logInfo = new LogInfo();
			logInfo.setUid("100001011492509");
			logInfo.setSystemId("20180528001948300381461684866578");
			
			List<JSONObject> jsonList = new ArrayList<JSONObject>();
			JSONObject obj = new JSONObject();
			obj.put(Constant.COL_KEY, "login");
			JSONObject obj1 = new JSONObject();
			obj1.put("oper1", "zhnagxang denglu "+i);
			obj1.put("oper2", "zhnagxang click ad "+i);
			obj1.put("oper3", "zhnagxang view ad "+i);
			obj.put(Constant.COL_VALUE, obj1);
			jsonList.add(obj);
			
			logInfo.setJsonList(jsonList);
			
			logList.add(logInfo);
		}
		return logList;
	}

}
