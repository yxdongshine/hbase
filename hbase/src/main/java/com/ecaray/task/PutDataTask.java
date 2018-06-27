package com.ecaray.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ecaray.bean.LogInfo;
import com.ecaray.constant.Constant;
import com.ecaray.hbase.dao.impl.LogOperationDao;
import com.ecaray.util.IDGenerator;
import com.ecaray.util.LogOrperactionTest;

public class PutDataTask implements Runnable {

	private int indexStart;//开始索引
	private int indexEnd;//开始索引
	private int num;//同一用户记录数
	
	public void run() {
		// TODO Auto-generated method stub
		LogOperationDao loDao = new LogOperationDao();
		Random random = new Random();
		for (int K = indexStart; K < indexEnd ; K++) {
			List<LogInfo> logList = new ArrayList<LogInfo>();
			LogInfo logInfo = new LogInfo();
			String uid = new StringBuilder(IDGenerator.newGUID()).reverse().toString();
			logInfo.setUid(uid);
			String reSystemId = new StringBuilder("100019296424" + random.nextInt(100)).reverse().toString();
			logInfo.setSystemId(reSystemId);
			LogOrperactionTest.log.info("第"+ K +"次 uid: "+uid+" systemid:"+reSystemId);
			List<JSONObject> jsonList = new ArrayList<JSONObject>();
			for (int i = 0; i < num; i++) {
				JSONObject obj = new JSONObject();
				try {
					obj.put(Constant.COL_KEY, "login");
					JSONObject obj1 = new JSONObject();
					obj1.put("oper1", "zhnagxang denglu "+i);
					obj1.put("oper2", "zhnagxang click ad "+i);
					obj1.put("oper3", "zhnagxang view ad "+i);
					obj.put(Constant.COL_VALUE, obj1);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jsonList.add(obj);
			}
			logInfo.setJsonList(jsonList);
			logList.add(logInfo);
			try {
				loDao.add(logList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getIndexStart() {
		return indexStart;
	}

	public void setIndexStart(int indexStart) {
		this.indexStart = indexStart;
	}

	public int getIndexEnd() {
		return indexEnd;
	}

	public void setIndexEnd(int indexEnd) {
		this.indexEnd = indexEnd;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	

}
