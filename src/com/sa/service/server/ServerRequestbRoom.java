/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service]
 * 类名称: [ServerRequestbRoom]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 上午11:43:47]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 上午11:43:47]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.server;

import java.util.Map;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponebRoom;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestbRoom extends Packet {
	public ServerRequestbRoom() {
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestbRoom;
	}

	@Override
	public void execPacket() {
		/** 校验用户权限 */
		Map<String, Object> result = Permission.INSTANCE.checkUserAuth(this.getRoomId(), this.getFromUserId(),
				Constant.AUTH_SPEAK);
		/** 如果校验合格 */
		if (0 == ((Integer) result.get("code"))) {

			/** 如果有中心 且 目标IP不是中心IP */
			if (ConfManager.getIsCenter()) {
				/** 转发给中心 */
				ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
			} else {
				String[] roomIds = this.getRoomId().split(",");
				if (null != roomIds && roomIds.length > 0) {
					for (String rId : roomIds) {
						ServerDataPool.dataManager.setRoomChats(rId,
								System.currentTimeMillis() + "," + this.getTransactionId(), this.getFromUserId(),
								(String) this.getOption(1));
						
						ClientResponebRoom newCrr = new ClientResponebRoom(this.getPacketHead(), this.getOptions());
						newCrr.setRoomId(rId);
						newCrr.execPacket();
					}
				}
			}
		}

		new ClientMsgReceipt(this.getPacketHead(), result).execPacket();
	}

}
