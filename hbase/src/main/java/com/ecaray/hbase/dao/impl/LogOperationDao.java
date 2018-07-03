package com.ecaray.hbase.dao.impl;

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
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.TimestampsFilter;

import com.ecaray.bean.ColFamilyInfo;
import com.ecaray.bean.ColInfo;
import com.ecaray.bean.LogInfoPage;
import com.ecaray.bean.LogInfo;
import com.ecaray.bean.LogListPage;
import com.ecaray.bean.RowInfo;
import com.ecaray.connect.ConnectPool;
import com.ecaray.constant.Constant;
import com.ecaray.hbase.dao.DDLDao;
import com.ecaray.log.Logging;
import com.ecaray.util.HbaseUtil;
import com.ecaray.util.StringUtil;

/**
 * 组合条件查询参考：
 * https://www.cnblogs.com/linjiqin/archive/2013/06/05/3118921.html
 * https://blog.csdn.net/vaq37942/article/details/54949428
 * http://ygydaiaq-gmail-com.iteye.com/blog/1716844
 * @author YXD
 *
 */
public class LogOperationDao extends DDLDao{
	
	private static Logging log = Logging.getLogging(LogOperationDao.class.getName());
	private static final byte[] TABLE_NAME = Constant.OPERATION_LOG;
	private static final String COL_FAMILY_NAME = "content";
	private static final String COL_NAME = "log";
	
	/**
	 * 添加
	 * @param logList
	 * @return
	 * @author YXD
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public Boolean add(List<LogInfo> logList) throws Exception{
		Boolean isSuss = false ;
		List<RowInfo> rowList = buildRowInfo(logList);
		HConnection connection = ConnectPool.getInstance().getConnection();
		HTableInterface rceTbl = connection.getTable(TableName.valueOf(TABLE_NAME));
		//构建puts
		List<Put> putList = buildPuts(rowList);
		//rceTbl.setAutoFlush(true);
		//rceTbl.flushCommits();
		long beforeTime = HbaseUtil.getSystemTime();
		rceTbl.put(putList);
		System.out.println("写入时间差(毫秒)："+ (HbaseUtil.getSystemTime() - beforeTime));
		ConnectPool.getInstance().putConnection(connection);
		isSuss = true;
		return isSuss;
	}
	
	/**
	 * 删除
	 * @param logList
	 * @return
	 * @author YXD
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public Boolean delete(List<LogInfo> logList) throws Exception{
		Boolean isSuss = false ;
		List<RowInfo> rowList = buildRowInfo(logList);
		HConnection connection = ConnectPool.getInstance().getConnection();
		HTableInterface rceTbl = connection.getTable(TableName.valueOf(TABLE_NAME));
		//rceTbl.setAutoFlush(true);
		//rceTbl.flushCommits();
		//构建Deletes
		List<Delete> deleteList = buildDelete(rowList);
		rceTbl.delete(deleteList);
		ConnectPool.getInstance().putConnection(connection);
		isSuss = true;
		return isSuss;
	}
	
	/**
	 * 查询
	 * @param log
	 * @return
	 * @author YXD
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public LogListPage query(LogInfoPage logInfoPage) throws Exception{
		LogListPage llPage = new LogListPage();
		List<LogInfo> queryLogList = new ArrayList<LogInfo>();
		List<LogInfo> logList = new ArrayList<LogInfo>();
		logList.add(logInfoPage);
		List<RowInfo> rowList = buildRowInfo(logList);
		HConnection connection = ConnectPool.getInstance().getConnection();
		HTableInterface rceTbl = connection.getTable(TableName.valueOf(TABLE_NAME));
		//构建Deletes
		List<Get> getList = buildGets(rowList);
		Result[] results = rceTbl.get(getList);
		for(Result result : results){
			Cell[] cls = result.rawCells();
			int index = 0;
			llPage.setTotalNum(cls.length);
			for(Cell cell : cls){
				if(logInfoPage.getIsPage()){//如果分页
					index++;
					int pageIndex = logInfoPage.getPageIndex();
					int pageSize = logInfoPage.getPageSize();
					if(index <= (pageIndex-1)*pageSize){
						continue;
					}else if(index > pageIndex*pageSize){
						break;
					}
				}
				LogInfo querylogInfo = new LogInfo();
				String rowkey = Bytes.toString(CellUtil.cloneRow(cell));
				String[] splitArr = rowkey.split(Constant.SPLIT_UNDERLINE);
				if(null != splitArr
						&&2 == splitArr.length){
					querylogInfo.setUid(splitArr[0]);
					querylogInfo.setSystemId(splitArr[1]);
				}
				JSONObject jObj = new JSONObject();
				String colName = Bytes.toString(CellUtil.cloneQualifier(cell));
				jObj.put(Constant.COL_KEY, colName);
				String value = Bytes.toString(CellUtil.cloneValue(cell));
				jObj.put(Constant.COL_VALUE, new JSONObject(value));
				jObj.put(Constant.COL_TIME, cell.getTimestamp());
				List<JSONObject> jsonList = new ArrayList<JSONObject>();
				jsonList.add(jObj);
				querylogInfo.setJsonList(jsonList);
				queryLogList.add(querylogInfo);
			}
		}
		ConnectPool.getInstance().putConnection(connection);
		llPage.setLogList(queryLogList);
		return llPage;
	}
	
	/**
	 * 根据条件查询日志列表
	 * @param logCondition
	 * @return
	 * @throws Exception
	 * @author YXD
	 */
	public LogListPage queryList(LogInfoPage logInfoPage) throws Exception{
		LogListPage llPage = new LogListPage();
		List<LogInfo> logList = new ArrayList<LogInfo>();
		HConnection connection = ConnectPool.getInstance().getConnection();
		HTableInterface rceTbl = connection.getTable(TableName.valueOf(TABLE_NAME));
		Scan scan = new Scan();
        scan.setMaxVersions();
        scan.setBatch(100000);//单用户使用十年最大数据10w
        //时间过滤
        if(!StringUtil.strIsEmpty(logInfoPage.getStartTime())
        		&& !StringUtil.strIsEmpty(logInfoPage.getEndTime())){
            scan.setTimeRange(Long.parseLong(logInfoPage.getStartTime()), Long.parseLong(logInfoPage.getEndTime()));
        }
        //列过滤
        List<String> columnList = logInfoPage.getColumnList();
        if(null != columnList){
        	for (Iterator iterator = columnList.iterator(); iterator.hasNext();) {
				String columnName = (String) iterator.next();
		        scan.addColumn(TABLE_NAME, Bytes.toBytes(columnName));
			}
        }
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        /* //分页
        PageFilter pageFilter = new PageFilter(1);
        filterList.addFilter(pageFilter);
        scan.setStartRow(Bytes.toBytes("100001011492509_20180528001948300381461684866578"));*/
        //组合条件
        if(!StringUtil.strIsEmpty(logInfoPage.getUid())
        		&& StringUtil.strIsEmpty(logInfoPage.getSystemId())){
        	//如果只有uid
        	PrefixFilter prefixFilter = new PrefixFilter(Bytes.toBytes(logInfoPage.getUid()));
        	filterList.addFilter(prefixFilter);
        }else if(StringUtil.strIsEmpty(logInfoPage.getUid())
        		&& !StringUtil.strIsEmpty(logInfoPage.getSystemId())){
        	//如果只有systemId
        	RowFilter rowFilter = new RowFilter(CompareOp.EQUAL, new SubstringComparator(logInfoPage.getSystemId()));
        	filterList.addFilter(rowFilter);
        }else if(!StringUtil.strIsEmpty(logInfoPage.getUid())
        		&& !StringUtil.strIsEmpty(logInfoPage.getSystemId())){
        	//如果systemId和uid都存在
        	RowFilter rowFilter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(HbaseUtil.buildRowkey(logInfoPage)));
        	filterList.addFilter(rowFilter);
        }
        scan.setFilter(filterList);

        long beforeTime = HbaseUtil.getSystemTime();
        ResultScanner resultScanner = rceTbl.getScanner(scan);
		Iterator<Result> iter = resultScanner.iterator();
		System.out.println("查询时间差(毫秒)："+ (HbaseUtil.getSystemTime() - beforeTime - 100));
		while(iter.hasNext()){
			Result r = iter.next();
			Cell[] cells = r.rawCells();
			int index = 0;
			llPage.setTotalNum(cells.length);
			for(Cell cell : cells){
				if(logInfoPage.getIsPage()){//如果分页
					index++;
					int pageIndex = logInfoPage.getPageIndex();
					int pageSize = logInfoPage.getPageSize();
					if(index <= (pageIndex-1)*pageSize){
						continue;
					}else if(index > pageIndex*pageSize){
						break;
					}
				}
				LogInfo querylogInfo = new LogInfo();
				String rowkey = Bytes.toString(CellUtil.cloneRow(cell));
				String[] splitArr = rowkey.split(Constant.SPLIT_UNDERLINE);
				if(null != splitArr
						&&2 == splitArr.length){
					querylogInfo.setUid(splitArr[0]);
					querylogInfo.setSystemId(splitArr[1]);
				}
				JSONObject jObj = new JSONObject();
				String colName = Bytes.toString(CellUtil.cloneQualifier(cell));
				jObj.put(Constant.COL_KEY, colName);
				String value = Bytes.toString(CellUtil.cloneValue(cell));
				jObj.put(Constant.COL_VALUE, new JSONObject(value));
				jObj.put(Constant.COL_TIME, cell.getTimestamp());
				List<JSONObject> jsonList = new ArrayList<JSONObject>();
				jsonList.add(jObj);
				querylogInfo.setJsonList(jsonList);
				logList.add(querylogInfo);
			}
		}
		ConnectPool.getInstance().putConnection(connection);
		llPage.setLogList(logList);
		return llPage;
	}

		
	/**
	 * 构建row列表信息
	 * @param jObj
	 * @return
	 * @author YXD
	 */
	public List<RowInfo> buildRowInfo(List<LogInfo> logList){
		List<RowInfo> rList = new ArrayList<RowInfo>();
		for (int i = 0; i < logList.size(); i++) {
			LogInfo logInfo = logList.get(i);
			//构建rowkey
			RowInfo rowInfo = new RowInfo();
			rowInfo.setRowKey(HbaseUtil.buildRowkey(logInfo));
			// 构建列
			List<ColInfo> cList = new ArrayList<ColInfo>();
			List<JSONObject> jsonList = logInfo.getJsonList();
			for (int j = 0; null != jsonList && j < jsonList.size(); j++) {
				JSONObject jobj = jsonList.get(j);
				ColInfo cInfo = new ColInfo();
				try {
					cInfo.setColKey(jobj.getString(Constant.COL_KEY));
					cInfo.setColVaue(jobj.getJSONObject(Constant.COL_VALUE).toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cList.add(cInfo);
			}
			
			//构建列簇
			List<ColFamilyInfo> cfList = new ArrayList<ColFamilyInfo>();
			ColFamilyInfo cfInfo = new ColFamilyInfo();
			cfInfo.setColFamilyName(COL_FAMILY_NAME);
			cfInfo.setClList(cList);
			cfList.add(cfInfo);
			
			rowInfo.setCfList(cfList);
			
			rList.add(rowInfo);
		}
		return rList;
	}
}
