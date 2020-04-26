package com.sa;

import com.sa.base.ConfManager;
import com.sa.thread.AutoCancelTempConnect;
import com.sa.thread.MongoLogSync;
import com.sa.transport.CenterSocketServcer;
import com.sa.transport.ClientSocketServcer;
import com.sa.transport.WebSocketServer;
import com.sa.util.ReadConf;

public class ServerStart {

	public static void main(String[] args) {
		initConf();
		
		new Thread(new AutoCancelTempConnect()).start();

		/** 是否 启用 mongodb*/
		boolean mongodbEnable = ConfManager.getMongodbEnable();
		if(mongodbEnable){
			new Thread(new MongoLogSync(ConfManager.getMongoIp(), ConfManager.getMongoPort(), ConfManager.getMongoNettyLogDBName(),ConfManager.getMongoNettyLogTableName(),ConfManager.getMongoNettyLogUserName(),ConfManager.getMongoNettyLogPassword(), ConfManager.getLogTime(),false)).start();
		}

		//new Thread(new RoomCancelSync()).start();

/*		new Thread(new HttpServer(ConfManager.getHttpPort())).start();
		new Thread(new LogSync(ConfManager.getLogUrl(), ConfManager.getLogTime())).start();
		if (!ConfManager.getIsCenter()) {
			new Thread(new StatisticRoomInfoSync(ConfManager.getStatisticUrl(), ConfManager.getStatisticTime())).start();
		} else {
			new Thread(new ClientSocketServcer(ConfManager.getCenterPort())).start();
		}*/

		startNetty();
	}

	private static void initConf() {
		new ReadConf().readFileByLines();
	}

	private static void startNetty() {
		try {
			new Thread(new CenterSocketServcer(ConfManager.getCenterSoketServerPort())).start();
			new Thread(new ClientSocketServcer(ConfManager.getClientSoketServerPort())).start();
			new WebSocketServer().bind(ConfManager.getWebSoketServerPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
