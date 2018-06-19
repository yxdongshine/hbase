package com.ecaray.constant;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;

public class Constant {
	//hbase连接工厂初始化
	public static  final Configuration conf = HBaseConfiguration.create();
	public static final byte[] OPERATION_LOG = Bytes.toBytes("stif:operation-log");
	public static final String COL_FAMILY = "content";
	public static final String NAMESPACE = "stif";
	public static final String SPLIT_COLON = ":";
	public static final String SPLIT_UNDERLINE = "_";
	public static final String[] SYSTEM_IDS = {"20180528001948300381461684866578","20180527225455450290024949800869","20180526220919620500441770684717"};
	public static final String[] UIDS = {"100001011492509","100001236853379","100001642163054","100001845969440","100002357125081","100002362870154"};
	public static final String COL_KEY = "col_key";
	public static final String COL_VALUE = "col_value";

}
