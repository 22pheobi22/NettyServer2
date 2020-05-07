/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.client]
 * 类名称: [CUniqueLogon]
 * 类描述: [房间用户列表 下行]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月17日 下午3:10:28]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月17日 下午3:10:28]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.ChannelExtend;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.util.Constant;

import io.netty.channel.ChannelHandlerContext;

public class CUniqueLogon extends Packet {
	public CUniqueLogon(){}
	
	@Override
	public void execPacket() {
		System.err.println("服务收到CUniqueLogon："+this.getFromUserId()+"  时间 ："+System.currentTimeMillis());
		//普通用戶登錄後收到的消息有兩種情況，1.首登；2.重登
		ChannelHandlerContext ctx =ServerDataPool.USER_CHANNEL_MAP.get(this.getFromUserId());
		//2.重登-- 原通道發註銷回執 新通道發登陸成功下行
		if("10098".equals(this.getStatus().toString())){
			//code==10098--用戶已登錄 
			//2.1給袁通道回執
			/** 实例化 消息回执 */
			ClientMsgReceipt mr = new ClientMsgReceipt(this.getTransactionId(), this.getRoomId(), this.getFromUserId(),
					10098);
			mr.setOption(254, Constant.ERR_CODE_10098);
			/** 发送 消息回执 *///给原通道服务器
			try {
				Manager.INSTANCE.sendPacketTo(mr, ctx, Constant.CONSOLE_CODE_S);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.err.println("服务发送重登下行："+this.getFromUserId()+"  时间 ："+System.currentTimeMillis());
			//2.2注銷用戶通道信息
			ServerManager.INSTANCE.ungisterUserContext(ctx);
			System.err.println("服务注销上次登录："+this.getFromUserId()+"  时间 ："+System.currentTimeMillis());

		}else{
			//1.首登/踢出旧号后重登-- 發登陸成功的登錄下行消息
			//新server通道
			ChannelHandlerContext context = ServerDataPool.TEMP_CONN_MAP2.get(this.getFromUserId());
			ChannelExtend ce = ServerDataPool.TEMP_CONN_MAP.get(context);
			if(null==context||null == ce || null == ce.getConnBeginTime()){
				return;
			}
			System.err.println("服务重新注册开始："+this.getFromUserId()+"  时间 ："+System.currentTimeMillis());

			doLogin(this.getFromUserId(),context,ce.getChannelType());
			System.err.println("服务重新注册结束："+this.getFromUserId()+"  时间 ："+System.currentTimeMillis());

		}
	}
	
	private void doLogin(String fromUserId, ChannelHandlerContext context, int channelType) {
		ServerManager.INSTANCE.addOnlineContext(fromUserId, context, channelType);
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.CUniqueLogon;
	}
}
