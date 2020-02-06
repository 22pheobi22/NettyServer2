package com.sa.base;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sa.base.element.ChannelExtend;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.net.codec.PacketBinEncoder;
import com.sa.thread.MongoLogSync;
import com.sa.util.Constant;
import com.sa.util.JedisUtil;
import com.sa.util.StringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public enum RedisManager {

	INSTANCE;
	private static ExecutorService timelyLogExecutor = Executors.newSingleThreadExecutor();
	
	private static RedisDataManager redisDataManager = new RedisDataManager();;
	/**
	 * 上一个达到立即保存日志线程的是否完毕 true为完毕
	 */
	public final AtomicBoolean lastTimelyLogThreadExecuteStatus = new AtomicBoolean(true);
	private JedisUtil jedisUtil = new JedisUtil();
	private String USER_SERVERIP_MAP_KEY = "USER_SERVERIP_MAP";

	/** 向通道写消息并发送 */
	private void writeAndFlush(ChannelHandlerContext ctx, Packet pact) throws Exception {
		ChannelExtend ce = ServerDataPool.CHANNEL_USER_MAP.get(ctx);
		if (null == ce) {
			ce = ServerDataPool.TEMP_CONN_MAP.get(ctx);
		}

		if (null != ce) {
			if (0 == ce.getChannelType()) {
				// System.out.println("【ctx:"+ctx+"】【pack:"+pact+"】");
				ctx.writeAndFlush(pact);
			} else if (1 == ce.getChannelType()) {
				// 将数据包封成二进制包
				BinaryWebSocketFrame binaryWebSocketFrame = new PacketBinEncoder().encode(pact);

				// 把包放进通道并发送
				ctx.writeAndFlush(binaryWebSocketFrame);
			} else {
				System.out.println("未知类型连接");
			}
		} else {
			System.out.println("通道拓展信息不存在");
		}
	}

	/** 向单一用户发送数据包 */
	//server端发给指定用户 走内存或转中心
	/*public void sendPacketTo(Packet pact, String consoleHead) throws Exception {
		// 如果数据包为空 或 数据接收者是中心 返回
		if (pact == null || "0".equals(pact.getToUserId()))
			return;
		// 因为是中心发消息，不会出现中心-中心循环发送的情况，故去掉此判断
		// if(pact == null) return;
		// 获取目标用户服务ip
		String userServerIp = jedisUtil.getHash(USER_SERVERIP_MAP_KEY, pact.getToUserId());
		// 获取缓存的 用户-通道信息
		Map<String, ChannelHandlerContext> contextMap = ServerDataPool.USER_CHANNEL_MAP;
		// 空则返回
		if (StringUtil.isEmpty(contextMap))
			return;

		// 获取接收用户通道
		ChannelHandlerContext targetContext = contextMap.get(userServerIp);
		// 空则返回
		if (targetContext == null)
			return;

		// 在控制台打印消息头
		pact.printPacket(ConfManager.getConsoleFlag(), consoleHead, ConfManager.getFileLogFlag(),
				ConfManager.getFileLogPath());
		// 缓存消息日志
		// this.log(pact);

		// 给接收用户发送数据包
		writeAndFlush(targetContext, pact);
	}*/

	/**
	 * 向所有在线用户发送数据包
	 * 
	 * @throws Exception
	 */
	// 原逻辑：遍历房间内所有用户，除发送者及中心外均发送消息
	// 中心逻辑:查找房间内用户来源ip 中心IP除外 发消息 对于发送者所在ip 依旧发消息 在服务中判断
	//server逻辑：根据房间id去redis中找用户id 再从内存找通道
	public void sendPacketToRoomAllUsers(Packet pact, String consoleHead) throws Exception {
		// 如果数据包为空 则返回
		if (pact == null)
			return;

		// 获取房间内所有用户信息
		Map<String, People> roomUsers = redisDataManager.getRoomUesrs(pact.getRoomId());
		// 如果房间内没有用户 则返回
		if (null == roomUsers || 0 == roomUsers.size())
			return;

		// 在控制台打印消息头
		pact.printPacket(ConfManager.getConsoleFlag(), consoleHead, ConfManager.getFileLogFlag(),
				ConfManager.getFileLogPath());
		// 缓存消息日志
		// this.log(pact);

		// 存储房间用户所在服务ip
		//Set<String> ipSet = new HashSet<>();
		// 遍历用户map
		for (Map.Entry<String, People> entry : roomUsers.entrySet()) {
			
			// 如果当前遍历出来的用户是发消息的用户，则不发送并继续遍历 if
			/*if(entry.getKey().equals(pact.getFromUserId())) { 
				continue; 
			}*/
			 
			//String serverIp = jedisUtil.getHash(USER_SERVERIP_MAP_KEY, entry.getKey());
			//if (!ipSet.contains(serverIp)) {
				// 获取用户通道
				ChannelHandlerContext ctx = ServerDataPool.USER_CHANNEL_MAP.get(entry.getKey());
				//ipSet.add(serverIp);

				if (null != ctx) {
					// 向通道写数据并发送
					writeAndFlush(ctx, pact);
				}
			//}

		}
	}

	/**
	 * 登录、注册、上线、绑定--非中心
	 */
	public void addOnlineContext(String roomId, String userId, String name, String icon, String agoraId,HashSet<String> userRole,
			boolean notSpeak, ChannelHandlerContext context, int channelType) {
		// 如果通道为空 则抛出空指针错误
		if (context == null) {
			// 抛出通道为空的异常
			throw new NullPointerException("context is null");
		}

		// 緩存用戶-serverIp信息
		String strIp = StringUtil.subStringIp(context.channel().remoteAddress().toString());
		jedisUtil.setHash(USER_SERVERIP_MAP_KEY, userId, strIp);
		// 将用户信息缓存
		String[] roomIds = roomId.split(",");
		if(roomIds!=null&&roomIds.length>0){
			//循环保存房间用户信息
			for (String rId : roomIds) {
				redisDataManager.setRoomUser(rId, userId, name, icon, agoraId, userRole, notSpeak);
			}
		}
	}

	/**注銷用戶信息--非中心*/
	public void ungisterUserInfo(String userId) {
		/** 刪除用戶IP信息*/
		jedisUtil.delHash(USER_SERVERIP_MAP_KEY, userId);
		// 注销用户
		ungisterUserId(userId);
		
	}
	
	
	
	/**
	 * 注销用户通信渠道
	 */
	public void ungisterUserId(String userId) {
		// 如果用户id不为空
		if (userId != null) {
			// 如果不是中心用户id
			if (!ConfManager.getCenterId().equals(userId)) {
				// 删除房间内该用户信息
				redisDataManager.removeRoomUser(userId);
				return;
			}
			// 获取用户通道信息
			ChannelHandlerContext ctx = ServerDataPool.USER_CHANNEL_MAP.get(userId);

			// 如果通道不为空
			if (null == ctx) {
				return;
			}
			System.out.println("用户【 " + userId + " 】注销");
			// 删除通道-用户缓存
			ServerDataPool.CHANNEL_USER_MAP.remove(ctx);
			// 删除用户-通道缓存
			ServerDataPool.USER_CHANNEL_MAP.remove(userId);
			if (null != ctx) {
				// 通道关闭
				ctx.close();
			}
		}
	}

	/**
	 * 注销用户通信渠道
	 */
	public void ungisterUserContext(ChannelHandlerContext context) {
		// 如果通道不为空
		if (context != null) {
			// 根据通道获取用户id
			ChannelExtend ce = ServerDataPool.CHANNEL_USER_MAP.get(context);
			// 如果用户id为空 则返回
			if (null == ce || null == ce.getUserId()) {
				return;
			}

			// 注销用户
			ungisterUserId(ce.getUserId());
		}
	}

	/**
	 * 向房间内所有用户发送数据包
	 * 
	 * @throws Exception
	 */
	// 在中心 向房间所有用户所在的服务的ip对应通道发下行消息
	//在服务 去redis找在本机用户
	public void sendPacketToRoomAllUsers(Packet pact, String consoleHead, String fromUserId) throws Exception {
		// 如果数据包为空 则返回
		if (pact == null)
			return;

		// 获取房间内所有用户信息
		Map<String, People> roomUsers = redisDataManager.getRoomUesrs(pact.getRoomId());
		// 如果房间内没有用户 则返回
		if (null == roomUsers || 0 == roomUsers.size())
			return;

		// 在控制台打印消息头
		pact.printPacket(ConfManager.getConsoleFlag(), consoleHead, ConfManager.getFileLogFlag(),
				ConfManager.getFileLogPath());

		// this.log(pact);

		// 存放房间用户所在ip集合（过滤发送人）
		//Set<String> ipSet = new HashSet<>();

		// 遍历用户map
		for (Map.Entry<String, People> entry : roomUsers.entrySet()) {
			// 如果当前遍历出来的用户是发消息的用户，则不发送并继续遍历
			if (entry.getKey().equals(fromUserId) || "0".equals(entry.getKey())) {
				continue;
			}

			// System.out.println(entry.getKey() + " " + pact.getPacketType());

			// 获取用户对应的ip
			//String userServerIp = jedisUtil.getHash(USER_SERVERIP_MAP_KEY, entry.getKey());

			// 获取用户通道
			// 首先判断ip是否已存在集合 已存在则已发不再发
			//if (ipSet.contains(userServerIp)) {
				//continue;
			//}
			ChannelHandlerContext ctx = ServerDataPool.USER_CHANNEL_MAP.get(entry.getKey());
			//ipSet.add(userServerIp);
			// 如果通道不为空
			if (null == ctx) {
				continue;
			}

			// 向通道写数据并发送
			writeAndFlush(ctx, pact);
		}
	}

	public synchronized void log(Packet packet) {
		Boolean consoleFlag = ConfManager.getConsoleFlag();
		Boolean fileFlag = ConfManager.getFileLogFlag();
		String fileLogPath = ConfManager.getFileLogPath();

		packet.printPacket(consoleFlag, Constant.CONSOLE_CODE_R, fileFlag, fileLogPath);

		// 缓存消息日志
		if (packet.getPacketType() != PacketType.ServerHearBeat && packet.getPacketType() != PacketType.ServerLogin) {
			ServerDataPool.log
					.put(System.currentTimeMillis() + ConfManager.getLogKeySplit() + packet.getTransactionId(), packet);
			int logTotalSize = ServerDataPool.log.size();
			if (ConfManager.getMongodbEnable() && logTotalSize > ConfManager.getTimelyDealLogMaxThreshold()
					&& lastTimelyLogThreadExecuteStatus.get()) {
				lastTimelyLogThreadExecuteStatus.set(false);
				long nowTimestamp = System.currentTimeMillis();
				System.out.println(
						nowTimestamp + "及时清理开始>>" + logTotalSize + "[ThreadName]>" + Thread.currentThread().getName());
				Thread timelyLogThread = new Thread(
						new MongoLogSync(ConfManager.getMongoIp(), ConfManager.getMongoPort(),
								ConfManager.getMongoNettyLogDBName(), ConfManager.getMongoNettyLogTableName(),
								ConfManager.getMongoNettyLogUserName(), ConfManager.getMongoNettyLogPassword(),
								ConfManager.getLogTime(), true, lastTimelyLogThreadExecuteStatus));
				timelyLogExecutor.submit(timelyLogThread);
				System.out.println(
						nowTimestamp + "及时清理结束>>" + logTotalSize + "[ThreadName]>" + Thread.currentThread().getName());
			}
		}
	}
}
