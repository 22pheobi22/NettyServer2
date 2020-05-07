/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.thread]
 * 类名称: [LogSync]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月11日 下午5:31:45]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月11日 下午5:31:45]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.net.Packet;
import com.sa.util.DateUtil;
import com.sa.util.mongo.MongoDBConfig;
import com.sa.util.mongo.MongoDBIndex;
import com.sa.util.mongo.MongoDBUtil;

public class MongoLogSync extends BaseSync {

	public MongoLogSync() {}

	private MongoDBUtil mongoUtil ;
	
	private boolean onceRun;
	/**
	 * 批量保存的数量，默认最大500
	 */
	private int batchSaveMaxSize = 500;
	
	private String collectionName = null;
	/**
	 * 上一个达到立即保存日志线程的是否完毕 true为完毕 
	 */
	private AtomicBoolean lastTimelyLogThreadExecuteStatus;

	/*public MongoLogSync(String url, String ip,Integer port,String dbName,String collectionName,String userName,String password, int time , boolean onceRun , int batchSaveMaxSize) {
		super(url, time);
		System.out.println("是否一次运行:"+onceRun);
		mongoUtil = MongoDBUtil.getInstance(new MongoDBConfig(ip, port, dbName, userName,password));
		this.onceRun = onceRun;
		this.collectionName = collectionName;
		this.batchSaveMaxSize = batchSaveMaxSize;
	}
	public MongoLogSync(String url, String ip,Integer port,String dbName,String collectionName,String userName,String password, int time , boolean onceRun) {
		super(url, time);
		System.out.println("是否一次运行:"+onceRun);
		mongoUtil = MongoDBUtil.getInstance(new MongoDBConfig(ip, port, dbName, userName,password));
		this.onceRun = onceRun;
		this.collectionName = collectionName;
	}*/
	public MongoLogSync(String ip,Integer port,String dbName,String collectionName,String userName,String password, int time , boolean onceRun ){
		super(null, time);
		System.out.println("是否一次运行:"+onceRun);
		mongoUtil = MongoDBUtil.getInstance(new MongoDBConfig(ip, port, dbName, userName,password));
		this.onceRun = onceRun;
		this.collectionName = collectionName;
		this.batchSaveMaxSize = ConfManager.getLogBatchSaveMaxSize();
	}
	public MongoLogSync(String ip,Integer port,String dbName,String collectionName,String userName,String password, int time , boolean onceRun , AtomicBoolean lastTimelyLogThreadExecuteStatus) {
		super(null, time);
		System.out.println("是否一次运行:"+onceRun);
		mongoUtil = MongoDBUtil.getInstance(new MongoDBConfig(ip, port, dbName, userName,password));
		this.onceRun = onceRun;
		this.collectionName = collectionName;
		this.lastTimelyLogThreadExecuteStatus = lastTimelyLogThreadExecuteStatus;
		this.batchSaveMaxSize = ConfManager.getLogBatchSaveMaxSize();
	}
	@Override
	public void run() {
		if(onceRun){
			try {
				saveLog();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			while(true) {
				try {
					saveLog();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1000*60*super.getTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args) {
//		String key = System.currentTimeMillis()+ConfManager.getLogKeySplit()+"qqqq";
//		System.out.println(key);
//		System.out.println(key.split(ConfManager.getLogKeySplit())[0]);
		/*int logTotalSize = 2;
		int batchSaveSize = 50;
		int totalPageNum = (logTotalSize  +  batchSaveSize  - 1) / batchSaveSize;
		
		for (int i = 1; i <= logTotalSize; i++) {
			ServerDataPool.testLog.put(System.currentTimeMillis()+""+i, i);
		}
		Map<String, Integer> logs = ServerDataPool.testLog;
		
		for (int i = 1; i <= totalPageNum; i++) {
			int currSaveSize = 0;
			for (Entry<String, Integer> log : logs.entrySet()) {
				System.out.println("[page]"+i+"[log value]"+log.getValue());
				ServerDataPool.testLog.remove(log.getKey());
				currSaveSize++;
				if(currSaveSize==batchSaveSize){
					break;
				}
			}
			//save
		}
		System.out.println(totalPageNum);*/
	}
	/**
	 * 存储日志
	 * @param     参数  
	 * @return void    返回类型  
	 * @throws  
	 * Create at:   2019年2月14日 下午2:50:52
	 * @author bugao.gong
	 */
	private void saveLog(){
		String todayDateStr =  DateUtil.format(new Date(), DateUtil.DATE_FORMAT_03);
		addIndexs();
		synchronized (MongoLogSync.class){
			//本次日志的总数量
			int logTotalSize = ServerDataPool.log.size();
			//计算一共拆分成几页
			int totalPageNum = (logTotalSize  +  batchSaveMaxSize  - 1) / batchSaveMaxSize;
			System.out.println("[ThreadName]>"+Thread.currentThread().getName()+">[logSize]"+logTotalSize);
			if (ServerDataPool.log.size() > 0) {
				Map<String, Packet> logs = ServerDataPool.log;
				List<Map<String,Object>> msgLogList = new ArrayList<>(); 
				for (int i = 1; i <= totalPageNum; i++) {
					long startTime = System.currentTimeMillis();
					int currSaveSize = 0;
					for (Entry<String, Packet> log : logs.entrySet()) {
						
						Map<String, Object> msgMap = new HashMap<String, Object>();
						msgMap.put("keyPutTime", log.getKey().split(ConfManager.getLogKeySplit())[0]);
						msgMap.put("fromUserId", log.getValue().getFromUserId());
						msgMap.put("roomId", log.getValue().getRoomId());
						msgMap.put("toUserIdId", log.getValue().getToUserId());
						msgMap.put("transactionId", log.getValue().getTransactionId());
						msgMap.put("status", log.getValue().getStatus());
						msgMap.put("packetType", log.getValue().getPacketType().getType());
						msgMap.put("packet", JSONObject.toJSONString(log.getValue()));
						msgLogList.add(msgMap);
						ServerDataPool.log.remove(log.getKey());
						
						currSaveSize++;
						if(currSaveSize==batchSaveMaxSize){
							break;
						}
					}
					long endTime = System.currentTimeMillis();
					mongoUtil.saveDocs(collectionName+"_"+todayDateStr, msgLogList);
					msgLogList.clear();
					System.out.println("插入"+currSaveSize+"条，耗时:"+(endTime-startTime)+"ms");
				}
			}
			if(lastTimelyLogThreadExecuteStatus!=null){
				lastTimelyLogThreadExecuteStatus.set(true);;
			}
		}
	}
	private void addIndexs(){
		String todayDateStr =  DateUtil.format(new Date(), DateUtil.DATE_FORMAT_03);
		//
		String[] indexFieldNames = {"fromUserId","keyPutTime","packetType","roomId"};
		for (String indexFieldName : indexFieldNames) {
			mongoUtil.addIndex(collectionName+"_"+todayDateStr, indexFieldName, false);
		}
		//Combination Index
		List<MongoDBIndex> mongoDBIndexs = new ArrayList<>();
		mongoDBIndexs.add(new MongoDBIndex("packetType"));
		mongoDBIndexs.add(new MongoDBIndex("keyPutTime"));
		mongoDBIndexs.add(new MongoDBIndex("roomId"));
		try {
			mongoUtil.addCombinationIndex(collectionName+"_"+todayDateStr, mongoDBIndexs, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Text Index
		mongoUtil.addTextIndex(collectionName+"_"+todayDateStr, "packet", false);
	}
	@Override
	public String toJson() {
		return null;
	}
	
}
