package com.sa.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.ChannelExtend;
import com.sa.net.Packet;
import com.sa.net.PacketManager;
import com.sa.net.PacketType;
import com.sa.service.client.ClientHeartBeat;
import com.sa.service.manager.LoginManager;
import com.sa.service.manager.SystemLoginManager;
import com.sa.service.server.ServerLogin;
import com.sa.service.server.ServerLoginOut;
import com.sa.service.sys.SysLoginReq;
import com.sa.util.StringUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

public class ClientSocketServcerHandler extends ChannelInboundHandlerAdapter {

	// 客户端超时次数
	private Map<ChannelHandlerContext, Integer> clientOvertimeMap = new ConcurrentHashMap<>();

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("channelActive:"+ctx.channel().remoteAddress());
		ServerDataPool.TEMP_CONN_MAP.put(ctx, new ChannelExtend());
	}

	@Override
	public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
		try {
			System.out.println("channelRead:"+context.channel().remoteAddress());
			String strIp = StringUtil.subStringIp(context.channel().remoteAddress().toString());
			Packet packet = (Packet) msg;
			packet.setRemoteIp(strIp);
			if (packet.getPacketType() == PacketType.ServerLogin) {
				ServerManager.INSTANCE.log(packet);
				LoginManager.INSTANCE.login(context, (ServerLogin) packet);
			} else if (packet.getPacketType() == PacketType.SysLoginReq) {
				SystemLoginManager.INSTANCE.login(context, (SysLoginReq) packet);
			} else if (packet.getPacketType() == PacketType.ServerHearBeat) {
			} else {
				// 记录数据库日志
				ServerManager.INSTANCE.log(packet);
				PacketManager.INSTANCE.execPacket(packet);
			}
	
			clientOvertimeMap.remove(context);// 只要接受到数据包，则清空超时次数
		} finally {
			ReferenceCountUtil.release(msg);
		}

	}

	public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
		String strIp = ctx.channel().remoteAddress().toString();
		String log = "TCP closed..."+strIp;
		String logStr = loginOut(ctx,log);
		System.err.println(logStr);
		//若是中心客户端 给出特别日志
		/*if(){
			System.err.println(log);	
		}else{
			
		}*/
		if(null!=ctx){
			ctx.close(promise);			
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String strIp = ctx.channel().remoteAddress().toString();
		String log = "channelInactive客户端"+strIp+"关闭1";
		System.err.println(log);
		//若是中心客户端 给出特别日志
		/*if(){
			System.err.println(log);	
		}else{
			
		}*/
	}

	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
//		ServerManager.INSTANCE.ungisterUserContext(ctx);
		String strIp = ctx.channel().remoteAddress().toString();
		String log = "disconnect客户端"+strIp+"关闭2";
		String logStr = loginOut(ctx,log);
		if(null!=ctx){
			ctx.disconnect(promise);	
		}
		System.err.println(logStr);
		//若是中心客户端 给出特别日志
		/*if(){
			System.err.println(log);	
		}else{
			
		}*/
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {

		String strIp = ctx.channel().remoteAddress().toString();
		System.err.println("exceptionCaught:"+strIp+"业务逻辑出错");
		cause.printStackTrace();

		Channel channel = ctx.channel();
		if (cause instanceof Exception && channel.isActive()) {
			System.err.println("simple client " + channel.remoteAddress() + " 异常");
			//Thread.sleep(5000);
			ctx.close();	
		}
		//若是中心客户端 给出特别日志
		/*if(){
			System.err.println(log);	
		}else{
			
		}*/
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		String strIp = ctx.channel().remoteAddress().toString();

		// 心跳包检测读超时
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				System.err.println("客户端"+strIp+"读超时");
				int overtimeTimes = clientOvertimeMap.get(ctx);
				if (overtimeTimes < ConfManager.getMaxReconnectTimes()) {
					Manager.INSTANCE.sendPacketTo(new ClientHeartBeat(), ctx, null);
					addUserOvertime(ctx);
				} else {
					String log = "客户端"+strIp+"超时踢下线";
					ChannelExtend ce = ServerDataPool.CHANNEL_USER_MAP.get(ctx);
					if (null != ce) {
						log += "("+ce.getUserId()+")";
					}
					
					System.err.println(log);
					//若是中心客户端 给出特别日志
					/*if(){
						System.err.println(log);	
					}else{
						
					}*/
					ServerManager.INSTANCE.ungisterUserContext(ctx);
					
				}
			}
		}
	}

	private void addUserOvertime(ChannelHandlerContext ctx) {
		int oldTimes = 0;
		if (clientOvertimeMap.containsKey(ctx)) {
			oldTimes = clientOvertimeMap.get(ctx);
		}
		clientOvertimeMap.put(ctx, oldTimes + 1);
	}
	
	private String loginOut(ChannelHandlerContext ctx,String log) {
		try {
			ChannelExtend ce = ServerDataPool.CHANNEL_USER_MAP.get(ctx);
			if (null != ce && null != ce.getUserId()) {
				String roomId = ServerDataPool.dataManager.getUserRoomNo(ce.getUserId());

				log += "["+roomId+"]("+ce.getUserId()+")";

				//if(null!=roomId){
					ServerLoginOut serverLoginOut = new ServerLoginOut();
					serverLoginOut.setFromUserId(ce.getUserId());
					serverLoginOut.setRoomId(roomId);
					serverLoginOut.setStatus(0);
					serverLoginOut.setToUserId(ce.getUserId());
					serverLoginOut.setTransactionId(1111199999);
			
					serverLoginOut.execPacket();
				//}
			}
		} catch (Exception e) {
			System.err.print("退出异常");
		}
		return log;
	}
}
