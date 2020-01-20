/**
 * 登陆 上行
 */
package com.sa.service.server;

import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;

public class ServerLogin extends Packet {
	public ServerLogin(){}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerLogin;
	}

	@Override
	public void execPacket() {
		// 用户登录成功
		ClientMsgReceipt clientMsgReceipt = new ClientMsgReceipt(this.getPacketHead());
		clientMsgReceipt.setOptions(this.getOptions());

		clientMsgReceipt.execPacket();
	}
}
