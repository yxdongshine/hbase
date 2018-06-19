package com.ecaray.connect;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;

import com.ecaray.constant.Constant;

/**
 * Created by YXD on 2017/10/24.
 */
public class ConnectPool {

    private Logger log = Logger.getLogger(this.getClass().getName());

    //连接池个数
    private int connectNum = 10;

    private static ConnectPool connectPool = null;
  
    //线程池队列
    ConcurrentLinkedQueue<HConnection> connectionQueue = new ConcurrentLinkedQueue<HConnection>();

    private ConnectPool(){}

    public static ConnectPool getInstance(){
        synchronized (ConnectPool.class){
            if (connectPool == null ){
                connectPool = new ConnectPool();
            }
        }
        return connectPool;
    }

    /**
     * 根据配置初始化connection
     * @return
     */
    private HConnection initConnection(){
    	HConnection hconn = null;
		try {
			hconn = HConnectionManager.createConnection(Constant.conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return hconn;
    }

    /**
     *从连接池中获取一个connetion
     * @return
     */
    public  HConnection getConnection() {
    	HConnection connection = null;
        synchronized(connectionQueue){
            if (connectionQueue.size() < connectNum){
                connection = initConnection();
                connectionQueue.offer(connection);
            }else{
                connection = connectionQueue.poll();
            }
        }
        return connection;
    }

    /**
     * 归还连接
     * @return
     */
    public  void putConnection(HConnection connection) {
        if (connectionQueue.size() < connectNum){
            connectionQueue.offer(connection);
        }
    }

}
