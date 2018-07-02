package com.ecaray.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.jettison.json.JSONObject;

import com.ecaray.bean.LogInfoPage;
import com.ecaray.bean.LogInfo;
import com.ecaray.bean.LogListPage;
import com.ecaray.connect.ConnectPool;
import com.ecaray.constant.Constant;
import com.ecaray.executors.ThreadPool;
import com.ecaray.hbase.dao.impl.LogOperationDao;
import com.ecaray.log.Logging;
import com.ecaray.task.PutDataTask;

public class LogOrperactionTest {
	
	public static Logging log = Logging.getLogging(LogOrperactionTest.class.getName());
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//bigDataTest();
		multThreadBigDataTest(10*10000);
		//queryList();
		//关闭所有资源
		//ConnectPool.getInstance().closeAllConnection();
	}
	
	
	/**
	 * 测试添加
	 * @throws Exception
	 * @author YXD
	 */
	public static void addTest() throws Exception{
		LogOperationDao loDao = new LogOperationDao();
		loDao.add(buildLogInfoList());
	}
	
	/**
	 * 查询实体
	 * @throws Exception
	 * @author YXD
	 */
	public static void query() throws Exception{
		LogOperationDao loDao = new LogOperationDao();
		//实体查询
		LogInfoPage logInfoPage = new LogInfoPage();
		logInfoPage.setUid("100001011492509");
		logInfoPage.setSystemId("20180528001948300381461684866578");
		LogListPage logListPage = loDao.query(logInfoPage);
		List<LogInfo> logInfoList = logListPage.getLogList();
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
	}
	
	
	/**
	 * 查询列表
	 * @throws Exception
	 * @author YXD
	 */
	public static void queryList() throws Exception{
		LogOperationDao loDao = new LogOperationDao();
		//列表查询
		LogInfoPage logCondition = new LogInfoPage();
		//logCondition.setUid("27309697350484412024953292608102");
		logCondition.setSystemId("2424692910001");
		//logCondition.setStartTime("1529856000000");
		//logCondition.setEndTime("1529938800000");
		logCondition.setIsPage(true);//分页
		logCondition.setPageIndex(1);
		logCondition.setPageSize(10000); 
		LogListPage llPage = loDao.queryList(logCondition);
		List<LogInfo> logInfoList = llPage.getLogList();
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
	}
	
	
	public static void deleteList() throws Exception{
		LogOperationDao loDao = new LogOperationDao();
		//删除
		LogInfo logInfo = new LogInfoPage();
		logInfo.setUid("100001011492509");
		logInfo.setSystemId("20180528001948300381461684866578");
		List<LogInfo> logList = new ArrayList<LogInfo>();
		logList.add(logInfo);
		loDao.delete(logList);
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

	public static void bigDataTest() throws Exception{
		LogOperationDao loDao = new LogOperationDao();
		Random random = new Random();
		for (int K = 0; K < 1000 ; K++) {
			List<LogInfo> logList = new ArrayList<LogInfo>();
			LogInfo logInfo = new LogInfo();
			String uid = new StringBuilder(IDGenerator.newGUID()).reverse().toString();
			logInfo.setUid(uid);
			String reSystemId = new StringBuilder("100019296424" + random.nextInt(100)).reverse().toString();
			logInfo.setSystemId(reSystemId);
			log.info("第"+ K +"次 uid: "+uid+" systemid:"+reSystemId);
			List<JSONObject> jsonList = new ArrayList<JSONObject>();
			for (int i = 0; i < 1000; i++) {
				JSONObject obj = new JSONObject();
				obj.put(Constant.COL_KEY, "login");
				JSONObject obj1 = new JSONObject();
				obj1.put("oper1", "zhnagxang denglu "+i);
				obj1.put("oper2", "zhnagxang click ad "+i);
				obj1.put("oper3", "zhnagxang view ad "+i);
				obj.put(Constant.COL_VALUE, obj1);
				jsonList.add(obj);
			}
			logInfo.setJsonList(jsonList);
			logList.add(logInfo);
			loDao.add(logList);
		}
		
	}
	
	
	public static void multThreadBigDataTest(int start) throws Exception{
		for (int i = 1; i <= 4; i++) {
			PutDataTask pdTask = new PutDataTask();
			pdTask.setNum(10 * 10000);
			int indexEnd = 10 * i + start;
			if(1 == i){
				pdTask.setIndexStart(start);
			}else{
				pdTask.setIndexStart(indexEnd - 10 );
			}
			pdTask.setIndexEnd(indexEnd);
			ThreadPool.getInstance().addTask(pdTask);
		}
	}
}
