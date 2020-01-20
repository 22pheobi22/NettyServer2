package com.sa.client;

import java.util.TreeMap;

import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketManager;
import com.sa.service.server.ServerLogin;
import com.sa.util.Constant;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ClientTransportHandler extends ChannelInboundHandlerAdapter {
	
	public ClientTransportHandler(){ }

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		Integer transactionId = (int) (1 + Math.random()*100000000);
		//String fromUserId = roomId + "-" + index + "-" + System.currentTimeMillis() + "-" + transactionId;
		ServerLogin serverLogin = new ServerLogin(transactionId,"","ip","0",0);
		//TreeMap<Integer, Object> options = new TreeMap<>(); // 消息记录集
		
		ServerDataPool.USER_CHANNEL_MAP.put("0", ctx);
		ServerManager.INSTANCE.sendPacketToCenter(serverLogin, Constant.CONSOLE_CODE_S);


	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception{
		Packet  packet = (Packet) msg;
		System.out.println(packet.toString());
//		packet.printPacket(ClientConfigs.CONSOLE_FLAG, "", packet.toString());

		PacketManager.INSTANCE.execPacket(packet);
	}

	public void close(ChannelHandlerContext ctx,ChannelPromise promise){
		System.err.println("TCP closed...");

		ctx.close(promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.err.println("客户端关闭1");
		ctx.close();
	}

	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		ctx.disconnect(promise);
		System.err.println("客户端关闭2");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.err.println("客户端关闭3");
//		ctx.fireExceptionCaught(cause);
		Channel channel = ctx.channel();
		cause.printStackTrace();
		if(channel.isActive()){
			System.err.println("simpleclient"+channel.remoteAddress()+"异常");
		}
	}
}
