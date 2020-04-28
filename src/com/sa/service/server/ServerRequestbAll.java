/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service]
 * 类名称: [ServerRequestbAll]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 上午11:43:57]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 上午11:43:57]
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
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponebAll;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestbAll extends Packet {
	public ServerRequestbAll(){}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestbAll;
	}

	@Override
	public void execPacket() {
		/** 校验用户角色是否符合*/
		Set<String> checkRoleSet = new HashSet(){{add(Constant.ROLE_SYSTEM);}};
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(), checkRoleSet);
		/** 如果校验成功*/
		if (0 == ((Integer) result.get("code"))) {
			/** 如果有中心 并且 中心不是目标地址*/
			if (ConfManager.getIsCenter()) {
				try {
					/** 转发到中心--中心发下行给本机外其他服务*/
					Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
					/** 发送下行消息给本机*/
					ClientResponebAll clientResponebAll = new ClientResponebAll(this.getPacketHead(), this.getOptions());
					clientResponebAll.execPacket();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else{
				/** 实例化 发送全体消息 下行 并执行*/
				ClientResponebAll clientResponebAll = new ClientResponebAll(this.getPacketHead(), this.getOptions());
				clientResponebAll.execPacket();
			}
		}
		/** 实例化消息回执 赋值 并 执行*/
		new ClientMsgReceipt(this.getPacketHead(), result).execPacket();

	}

}
