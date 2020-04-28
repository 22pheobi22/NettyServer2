/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.server]
 * 类名称: [ServerRequestcRemove]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 下午5:00:33]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 下午5:00:33]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponecRemove;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestcRemove extends Packet {
	public ServerRequestcRemove(){}

	/**
	 * @param transactionId
	 * @param roomId
	 * @param fromUserId
	 * @param toUserId
	 * @param status
	 */
	public ServerRequestcRemove(Integer transactionId, String roomId, String fromUserId, String toUserId, Integer status) {
		this.setTransactionId(transactionId);
		this.setRoomId(roomId);
		this.setFromUserId(fromUserId);
		this.setToUserId(toUserId);
		this.setStatus(status);
	}



	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestcRemove;
	}

	@Override
	public void execPacket() {
		/** 校验用户角色*/
		Set<String> checkRoleSet = new HashSet(){{add(Constant.ROLE_TEACHER);add(Constant.ROLE_PARENT_TEACHER);}};
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(), checkRoleSet);
		/** 校验成功*/
		if (0 == ((Integer) result.get("code"))) {
			/** 如果有中心*/
			if (ConfManager.getIsCenter()) {
				/** 转发到中心*/
				Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
				//本服務器處理
				ClientResponecRemove clientResponecRemove = new ClientResponecRemove(this.getPacketHead(), this.getOptions());
				clientResponecRemove.execPacket();
			}else{
				String[] roomIds = this.getRoomId().split(",");
				if (null != roomIds && roomIds.length > 0) {
					for (String rId : roomIds) {
						/** 获取目标用户信息*/
						People people = ServerDataPool.dataManager.getRoomUesr(rId, this.getToUserId());
						/** 如果用户信息不为空*/
						if (null != people)
							/** 设置删除成功*/
							this.setOption(255, "deleted");
						/** 实例化删除信息 下行 并赋值 并 执行*/
						ClientResponecRemove clientResponecRemove = new ClientResponecRemove(this.getPacketHead(), this.getOptions());
						clientResponecRemove.setRoomId(rId);
						clientResponecRemove.execPacket();
					}
				}
			}

		}
	}

}
