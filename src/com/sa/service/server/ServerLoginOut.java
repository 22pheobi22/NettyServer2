/**
 * 登出 上行
 */
package com.sa.service.server;

import java.util.Objects;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientLoginOut;
import com.sa.util.Constant;

public class ServerLoginOut extends Packet{
	public ServerLoginOut(){}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerLoginOut;
	}

	@Override
	public void execPacket() {
		People people = null;
		String[] roomIds = this.getRoomId().split(",");
		if (null != roomIds && roomIds.length > 0) {
			for (String rId : roomIds) {
				/** 根据房间id 和 发信人id 查询人员信息 */
				people = ServerDataPool.dataManager.getRoomUesr(rId, this.getFromUserId());
				if(Objects.nonNull(people)){
					break;
				}
			}
		}

		/** 如果人员信息为空 */
		if (null != people)
			/** 设置记录集选项 为 delete */
			this.setOption(255, "deleted");

		/** 如果有中心 */
		if (ConfManager.getIsCenter()) {
			/** 将消息转发到中心 */
			ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
		}
		/** 实例化消息回执并执行 */
		// new ClientMsgReceipt(this.getPacketHead()).execPacket();
		/** 实例化登出 下行 并执行 */
		ClientLoginOut clientLoginOut = new ClientLoginOut(this.getPacketHead(), this.getOptions());
		clientLoginOut.execPacket();
	}

}
