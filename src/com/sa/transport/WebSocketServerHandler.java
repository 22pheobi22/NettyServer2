/**
 *
 * 项目名称:[WebSocketServer]
 * 包:	 [com.sa.transport]
 * 类名称: [WebSocketServerHandler]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月12日 下午6:12:04]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月12日 下午6:12:04]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
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
import com.sa.net.codec.PacketBinDecoder;
import com.sa.service.client.ClientHeartBeat;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.manager.LoginManager;
import com.sa.service.server.ServerLogin;
import com.sa.service.server.ServerLoginOut;
import com.sa.util.ByteBufUtil;
import com.sa.util.Constant;
import com.sa.util.LogOutPrint;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private WebSocketServerHandshaker handshaker;
	// 客户端超时次数
	private Map<ChannelHandlerContext, Integer> clientOvertimeMap = new ConcurrentHashMap<>();

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ServerDataPool.TEMP_CONN_MAP.put(ctx, new ChannelExtend(1));
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		 // 传统的HTTP接入
		 if (msg instanceof FullHttpRequest) {
		     handleHttpRequest(ctx, (FullHttpRequest) msg);
		 }
		 // WebSocket接入
		 else if (msg instanceof WebSocketFrame) {
		     handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		 }

		 clientOvertimeMap.remove(ctx);// 只要接受到数据包，则清空超时次数
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

		// 如果HTTP解码失败，返回HHTP异常
		if (!req.decoderResult().isSuccess()
				|| (!"websocket".equals(req.headers().get("Upgrade")))) {
//			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// 构造握手响应返回，本机测试
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + req.headers().get(HttpHeaders.Names.HOST), null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

		// 判断是否是关闭链路的指令
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}

		// 判断是否是Ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}

		// 文本数据接收
		if (frame instanceof TextWebSocketFrame) {
			String request = ((TextWebSocketFrame) frame).text();

			System.out.println("TextWebSocketFrame 文本类型 : " + request);
			if (ConfManager.getFileLogFlag()) {
				LogOutPrint.log(ConfManager.getFileLogPath(), "TextWebSocketFrame 文本类型 : " + request);
			}
		}

		// 二进制数据接收
        if(frame instanceof BinaryWebSocketFrame) {
        	byte[] bytes=new byte[frame.content().readableBytes()];
        	frame.content().readBytes(bytes);
        	ByteBufUtil byteBufUtil = new ByteBufUtil(bytes);

        	binHander(ctx, byteBufUtil);
        }
	}

	private void binHander(ChannelHandlerContext context, ByteBufUtil byteBufUtil) throws Exception {
		Packet packet = (Packet) (new PacketBinDecoder().decode(byteBufUtil));

		String strIp = context.channel().remoteAddress().toString();
		System.out.println(strIp);
//		strIp = StringUtil.subStringIp(strIp);
//		packet.setRemoteIp(strIp);

		if (packet.getPacketType() == PacketType.ServerLogin) { // 如果是登录类型
			ServerLogin loginPact = (ServerLogin) packet; // 消息转为登录类型

//			packet.printPacket(ConfManager.getConsoleFlag(), Constant.CONSOLE_CODE_USERLOGIN, ConfManager.getFileLogFlag(), ConfManager.getFileLogPath());
//
//			ServerManager.INSTANCE.log(packet);
			// 登录
			LoginManager.INSTANCE.login(context, loginPact);

		} else if(packet.getPacketType() == PacketType.ServerHearBeat){ // 消息类型是心跳
			PacketManager.INSTANCE.execPacket(packet);
		} else { // 其他
			if (validateSession(packet, context)) { // 验证session
//				packet.printPacket(ConfManager.getConsoleFlag(), Constant.CONSOLE_CODE_R, ConfManager.getFileLogFlag(), ConfManager.getFileLogPath());

				ServerManager.INSTANCE.log(packet);

				// 执行定义动作
				PacketManager.INSTANCE.execPacket(packet);
			} else {
				ClientMsgReceipt cmr = new ClientMsgReceipt(packet.getTransactionId(), packet.getRoomId(), packet.getFromUserId(), 10099);
				cmr.setOption(254, Constant.ERR_CODE_10099);
				Manager.INSTANCE.sendPacketTo(packet, context, Constant.CONSOLE_CODE_S);
			}
		}
	}

	private boolean validateSession(Packet pact, ChannelHandlerContext context) {
		if (!ConfManager.getIsCenter() && null == ServerDataPool.CHANNEL_USER_MAP.get(context)) {
			return false;
		}
		return true;
	}

	public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
		System.err.println("TCP closed...");
		loginOut(ctx);
		ctx.close(promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.err.println("客户端关闭1");

		loginOut(ctx);

//		ServerManager.INSTANCE.ungisterUserContext(ctx);
	}

	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		loginOut(ctx);

//		ServerManager.INSTANCE.ungisterUserContext(ctx);
		ctx.disconnect(promise);
		System.err.println("客户端关闭2");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		loginOut(ctx);

		Manager.INSTANCE.ungisterUserContext(ctx);
		System.err.println("业务逻辑出错");
		cause.printStackTrace();

		Channel channel = ctx.channel();
		if (cause instanceof Exception && channel.isActive()) {
			System.err.println("simple client " + channel.remoteAddress() + " 异常");
			ctx.close();
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// 心跳包检测读超时
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				System.err.println("客户端读超时");
				int overtimeTimes = clientOvertimeMap.get(ctx);
				if (overtimeTimes < ConfManager.getMaxReconnectTimes()) {
					Manager.INSTANCE.sendPacketTo(new ClientHeartBeat(), ctx, null);
					addUserOvertime(ctx);
				} else {
					loginOut(ctx);

					Manager.INSTANCE.ungisterUserContext(ctx);
				}
			}
		}
	}

	private void loginOut(ChannelHandlerContext ctx) {
		try {
			ChannelExtend ce = ServerDataPool.CHANNEL_USER_MAP.get(ctx);
			if (null != ce) {
				String fromUserId = ce.getUserId();
				String roomId = ServerDataPool.dataManager.getUserRoomNo(fromUserId);

				ServerLoginOut serverLoginOut = new ServerLoginOut();
				serverLoginOut.setFromUserId(fromUserId);
				serverLoginOut.setRoomId(roomId);
				serverLoginOut.setStatus(0);
				serverLoginOut.setToUserId(fromUserId);
				serverLoginOut.setTransactionId(1122334455);
		
				serverLoginOut.execPacket();
			}
		} catch (Exception e) {
			System.err.print("退出异常");
		}
	}

	private void addUserOvertime(ChannelHandlerContext ctx) {
		int oldTimes = 0;
		if (clientOvertimeMap.containsKey(ctx)) {
			oldTimes = clientOvertimeMap.get(ctx);
		}
		clientOvertimeMap.put(ctx, oldTimes + 1);
	}

}
