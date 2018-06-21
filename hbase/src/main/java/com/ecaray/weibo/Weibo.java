package com.ecaray.weibo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 微博类
 * @author YXD
 *
 */
public class Weibo {
	
	static  final Configuration conf = HBaseConfiguration.create();
	
	private static final byte[] weibo_content = Bytes.toBytes("weibo:weibo-content");
	
	private static final byte[] relations =  Bytes.toBytes("weibo:relations");
	
	private static final byte[] receive_content_email = Bytes.toBytes("weibo:receive-content-email");
	
	
	/**
	 * 初始化命名空间
	 */
	public void initNameSpace(){
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(conf);
			
			NamespaceDescriptor descriptor = NamespaceDescriptor.create("weibo")
					.addConfiguration("creator", "ibeifeng")
					.addConfiguration("createTime", System.currentTimeMillis()+"").build();
			admin.createNamespace(descriptor);
			
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(admin!=null)
				try {
					admin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	
	/**
	 * 初始化表
	 */
	public void initTable(){
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(conf);
			
			/*
			 * 1、微博内容表
				TableName:   weibo:weibo-content
				RowKey：用户ID_timestamp
				列簇：cf
				列标签：	
						cf:content
						cf:title
						cf:photo
						
					版本设计：只需要保留一个版本
			 */
			HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(weibo_content));
			HColumnDescriptor family = new HColumnDescriptor(Bytes.toBytes("cf"));
			// 开启列簇 -- store的块缓存
			family.setBlockCacheEnabled(true);
			family.setBlocksize(1024*1024*2);
			
			family.setCompressionType(Algorithm.SNAPPY);
			
			family.setMaxVersions(1);
			family.setMinVersions(1);
			
			desc.addFamily(family);
			
			//admin.createTable(desc);
			byte[][] splitKeys = {
                    Bytes.toBytes("100"),
                    Bytes.toBytes("200"),
                    Bytes.toBytes("300")
            };
			admin.createTable(desc,splitKeys);
			
			
			/*
			 * 2、用户关系表
				TableName: weibo:relations
				RowKey: 用户ID
				列簇：attend 关注用户
						fan  粉丝用户
				列标签：使用用户ID作为列标签，值为用户ID
				
				版本：只需要一个版本
			 */
			HTableDescriptor relationTbl = new HTableDescriptor(TableName.valueOf(relations));
			HColumnDescriptor attend = new HColumnDescriptor(Bytes.toBytes("attend"));
			// 开启列簇 -- store的块缓存
			attend.setBlockCacheEnabled(true);
			attend.setBlocksize(1024*1024*2);
			
			attend.setCompressionType(Algorithm.SNAPPY);
			
			attend.setMaxVersions(1);
			attend.setMinVersions(1);
			
			relationTbl.addFamily(attend);
			
			HColumnDescriptor fans = new HColumnDescriptor(Bytes.toBytes("fans"));
			// 开启列簇 -- store的块缓存
			fans.setBlockCacheEnabled(true);
			fans.setBlocksize(1024*1024*2);
			
			fans.setCompressionType(Algorithm.SNAPPY);
			
			fans.setMaxVersions(1);
			fans.setMinVersions(1);
			
			relationTbl.addFamily(fans);
			
			admin.createTable(relationTbl);
			
			/*
			 * 3、用户微博内容接收邮件箱表
			TableName：   weibo:receive-content-email
			RowKey：用户ID
			列簇：cf
			列标签：
				直接使用用户ID，value值取微博内容的RowKey
				
				版本：设置最大版本为1000
			 */
			HTableDescriptor receiveContentEmail = 
					new HTableDescriptor(TableName.valueOf(receive_content_email));
			HColumnDescriptor rce_cf = new HColumnDescriptor(Bytes.toBytes("cf"));
			// 开启列簇 -- store的块缓存
			rce_cf.setBlockCacheEnabled(true);
			rce_cf.setBlocksize(1024*1024*2);
			
			rce_cf.setCompressionType(Algorithm.SNAPPY);
			
			rce_cf.setMaxVersions(1000);
			rce_cf.setMinVersions(1000);
			
			receiveContentEmail.addFamily(rce_cf);
			
			admin.createTable(receiveContentEmail);
			
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(admin!=null)
				try {
					admin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	/*
	 *  发布微博内容：
	 *  	1）在微博内容表中插入一行数据
	 *  	2）在用户微博内容接收邮件箱表对用户的所有粉丝用户添加数据
	 *  
	 *  Put
	 *  put 'tablename','rowkey','cf:cq','value'
	 */
	public void pubishWeiboContent(String uid,String content){
		HConnection hconn = null;
		try {
			hconn = HConnectionManager.createConnection(conf);
			// 1）在微博内容表中插入一行数据
			HTableInterface weiboContentTbl = hconn.getTable(TableName.valueOf(weibo_content));
			// rowkey : uid_timestamp
			long timestamp = System.currentTimeMillis();
			String rowkey = uid+"_"+timestamp;
			Put put = new Put(Bytes.toBytes(rowkey));
			put.add(Bytes.toBytes("cf"), Bytes.toBytes("content"), Bytes.toBytes(content));
			weiboContentTbl.put(put);
					
			// 查询该用户的粉丝用户
			HTableInterface relationsTbl = hconn.getTable(TableName.valueOf(relations));
			// get 'tablename','rowkey','cf','cq'
			Get get = new Get(Bytes.toBytes(uid));
			// 查询粉丝列簇下的所有粉丝
			get.addFamily(Bytes.toBytes("fans"));
			Result r = relationsTbl.get(get);
			
			List<byte[]> fans = new ArrayList<byte[]>();
			Cell[] cells = r.rawCells();
			for(Cell c : cells){
				fans.add(CellUtil.cloneQualifier(c));
			}
			
			if(fans.size() > 0){
				//2）在用户微博内容接收邮件箱表对用户的所有粉丝用户添加数据
				HTableInterface rceTbl = hconn.getTable(TableName.valueOf(receive_content_email));
				List<Put> ps = new  ArrayList<Put>();
				for(byte[] fanId : fans){
					Put p = new Put(fanId);
//					p.add(Bytes.toBytes("cf"), 
//							Bytes.toBytes(uid), 
//							Bytes.toBytes(uid+"_"+System.currentTimeMillis()));
					
					p.add(Bytes.toBytes("cf"), 
							Bytes.toBytes(uid), timestamp,
							Bytes.toBytes(rowkey));
					ps.add(p);
				}
				rceTbl.put(ps);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally{
			if(hconn!=null)
				try {
					hconn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	
	//添加关注用户
	/**
	 * 添加关注用户
	 * 	1）在微博用户关系表中，新增数据（关注用户列簇下添加标签）
	 * 	2）从被添加的关注用户角度，新增粉丝用户
	 * 	3）在微博邮件箱中添加关注用户发布的微博内容通知
	 * 
	 * @param uid
	 * @param attends
	 */
	public void addAttends(String uid,String... attends){
		
		if(attends == null || attends.length <= 0) return ;
		
		HConnection hconn = null;
		try {
			hconn = HConnectionManager.createConnection(conf);
			//1）在微博用户关系表中，新增数据（关注用户列簇下添加标签）
			HTableInterface relationsTbl = hconn.getTable(TableName.valueOf(relations));
			List<Put> ps = new ArrayList<Put>();
			Put put = new Put(Bytes.toBytes(uid));
			for(String attend:attends){
				put.add(Bytes.toBytes("attend"), Bytes.toBytes(attend), Bytes.toBytes(attend));
				// 2）从被添加的关注用户角度，新增粉丝用户
				Put attendPut = new Put(Bytes.toBytes(attend));
				attendPut.add(Bytes.toBytes("fans"), Bytes.toBytes(uid), Bytes.toBytes(uid));
				ps.add(attendPut);
			}
			ps.add(put);
			relationsTbl.put(ps);
			
			
			//3）在微博邮件箱中添加关注用户发布的微博内容通知
			// 先查询关注用户发布微博内容
			HTableInterface weiboContentTbl = hconn.getTable(TableName.valueOf(weibo_content));
			List<byte[]> rks = new ArrayList<byte[]>();
			Scan scan = new Scan();
			for(String attend:attends){
				// Filter
				// 扫描表的rowkey，只有rowkey含有字符串（"关注用户ID_"），取出
				RowFilter rowFilter = 
						new RowFilter(CompareOp.EQUAL, new SubstringComparator(attend+"_"));
				scan.setFilter(rowFilter);
				ResultScanner resultScanner = weiboContentTbl.getScanner(scan);
				Iterator<Result> it = resultScanner.iterator();
				while(it.hasNext()){
					Result r = it.next();
					Cell[] cells = r.rawCells();
					for(Cell c : cells){
						rks.add(CellUtil.cloneRow(c));
					}
				}
			}
			if(rks.size() > 0){
				//List<byte[]> rks = new ArrayList<byte[]>();
				HTableInterface rceTbl = hconn.getTable(TableName.valueOf(receive_content_email));
				List<Put> puts = new ArrayList<Put>();
				for(byte[] rk : rks){
					Put p = new Put(Bytes.toBytes(uid));
					String rowkey =  Bytes.toString(rk);
					Long timestamp = Long.valueOf(rowkey.substring(rowkey.indexOf("_")+1));
					String attendId = rowkey.substring(0, rowkey.indexOf("_"));
					p.add(Bytes.toBytes("cf"), 
							Bytes.toBytes(attendId), timestamp,rk);
					
					puts.add(p);
				}
				rceTbl.put(puts);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(hconn!=null)
				try {
					hconn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
	}
	
	/**
	 * 取消关注用户
	 * 1）在微博用户关系表，针对该用户，删除被取消的关注用户所对应的单元格
	 * 2）在微博用户关系表，针对被取消用户，删除它们的粉丝用户
	 * 3）在微博内容接收邮件箱表中，移除该用户的这些被取消关注用户微博内容通知记录
	 * @param uid
	 * @param attends  可变长度的参数列表
	 */
	public void removeAttends(String uid,String... attends){
		
		if(attends == null || attends.length <= 0) return ;
		
		HConnection hconn = null;
		try {
			hconn = HConnectionManager.createConnection(conf);
			
			// 1）在微博用户关系表，针对该用户，删除被取消的关注用户所对应的单元格
			HTableInterface relationsTbl = hconn.getTable(TableName.valueOf(relations));
			List<Delete> deletes = new ArrayList<Delete>();
			Delete delete = new Delete(Bytes.toBytes(uid));
			for(String attend:attends){
				delete.deleteColumn(Bytes.toBytes("attend"), Bytes.toBytes(attend));
				// 2）在微博用户关系表，针对被取消用户，删除它们的粉丝用户
				Delete deleteFan = new Delete(Bytes.toBytes(attend));
				deleteFan.deleteColumn(Bytes.toBytes("fans"), Bytes.toBytes(uid));
				deletes.add(deleteFan);
			}
			deletes.add(delete);
			relationsTbl.delete(deletes);
			
			
			//3）在微博内容接收邮件箱表中，移除该用户的这些被取消关注用户微博内容通知记录
			HTableInterface rceTbl = hconn.getTable(TableName.valueOf(receive_content_email));
			Delete deleteRCE = new Delete(Bytes.toBytes(uid));
			for(String attend:attends){
				// deleteColumn删除最近版本
				//deleteRCE.deleteColumn(Bytes.toBytes("cf"), Bytes.toBytes(attend));
				// 删除单元格的所有版本
				Long timestamp = System.currentTimeMillis();
				deleteRCE.deleteColumns(Bytes.toBytes("cf"), Bytes.toBytes(attend),timestamp+1000000);
			}
			rceTbl.delete(deleteRCE);
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(hconn!=null)
				try {
					hconn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * 用户获取所关注用户的微博内容
	 * 1） 从微博内容接收邮件箱表中获取用户其关注用户的微博内容 rowkey
	 * 2）从微博内容表中取出微博内容
	 * 
	 * 
	 * @param uid
	 * @return
	 */
	public List<Message> getAttendContents(String uid){
		
		List<Message>  msgs = new ArrayList<Message>();
		HConnection hconn = null;
		try {
			hconn = HConnectionManager.createConnection(conf);
			// 1） 从微博内容接收邮件箱表中获取用户其关注用户的微博内容 rowkey
			HTableInterface rceTbl = hconn.getTable(TableName.valueOf(receive_content_email));
			Get get = new Get(Bytes.toBytes(uid));
			get.setMaxVersions(5);
			Result r = rceTbl.get(get);
			List<byte[]> rks = new ArrayList<byte[]>();
			Cell[] cells = r.rawCells();
			if(cells != null && cells.length > 0){
				for(Cell c : cells){
					
					byte[] rk = CellUtil.cloneValue(c);
					rks.add(rk);
				}
			}
			
			//2）从微博内容表中取出微博内容
			if(rks.size() > 0){
				HTableInterface weiboContentTbl = hconn.getTable(TableName.valueOf(weibo_content));
				List<Get> gets = new  ArrayList<Get>();
				for(byte[] rk : rks){
					Get g = new Get(rk);
					gets.add(g);
				}
				
				Result[] results = weiboContentTbl.get(gets);
				for(Result result : results){
					Cell[] cls = result.rawCells();
					for(Cell cell : cls){
						Message msg = new Message();
						String rowkey = Bytes.toString(CellUtil.cloneRow(cell));
						String attendUid = rowkey.substring(0, rowkey.indexOf("_"));
						msg.setUid(attendUid);
						String timestamp = rowkey.substring(rowkey.indexOf("_")+1);
						msg.setTimestamp(timestamp);
						
						String content = Bytes.toString(CellUtil.cloneValue(cell));
						msg.setContent(content);
						
						msgs.add(msg);
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(hconn!=null)
				try {
					hconn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return msgs;
	} 
	
	public static void main(String[] args) {
		/*Weibo wb = new  Weibo();
		wb.initNameSpace();
		wb.initTable();
		//wb.pubishWeiboContent("0001", "今天天气真不错！");
		//wb.pubishWeiboContent("0003", "今天天气真不错！");
		//wb.pubishWeiboContent("0003", "今天天气真不错！");
		//wb.pubishWeiboContent("0004", "今天天气真不错！");
		//wb.pubishWeiboContent("0004", "今天天气真不错！");
		//wb.pubishWeiboContent("0005", "今天天气真不错！");
		
		//wb.addAttends("0001", "0003","0004","0005");
		//wb.removeAttends("0001", "0003");
		
		//List<Message> msgs = wb.getAttendContents("0001");
		
		//System.out.println(msgs);
		
		for(int i=0;i < 1000 ;i++){
			wb.pubishWeiboContent(String.format("%04d", i), "今天天气真不错！" + i);
		}*/
		System.out.println(Integer.MAX_VALUE);
		
	}

}
