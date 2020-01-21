/**
 *
 * 项目名称：CommunicationManage
 * 类名称：SysCloseRoomsReq
 * 类描述：
 * 创建人：Y.P
 * 创建时间：2019年2月12日 下午3:36:20
 * 修改人：Y.P
 * 修改时间：2019年2月12日 下午3:36:20
 * @version  1.0
 *
 */
package com.sa.service.sys;

import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientLoginOut;

public class SysCloseStudentReq extends Packet {

	public SysCloseStudentReq() {
	}

	@Override
	public void execPacket() {
		String roomId = (String) this.getOption(1);
		String stuIds = (String) this.getOption(2);

		if (null != stuIds) {
			String[] arr = stuIds.split(",");
			for (String tmp : arr) {
				/** 根据房间id 和 发信人id 查询人员信息*/
				People people = ServerDataPool.dataManager.getRoomUesr(roomId, tmp);
				ClientLoginOut clientLoginOut = new ClientLoginOut();
				clientLoginOut.setPacketHead(this.getPacketHead());
				clientLoginOut.setRoomId(roomId);
				clientLoginOut.setFromUserId(tmp);

				/** 如果人员信息为空*/
				if (null != people)
					/** 设置记录集选项 为 delete*/
					clientLoginOut.setOption(255, "deleted");

//				/** 如果有中心*/
//				if (ConfManager.getIsCenter()) {
//					/** 将消息转发到中心*/
//					ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
//				}

				/** 实例化登出 下行 并执行*/
				clientLoginOut.execPacket();
			}
		}
		
		SysCloseStudentRes sysCloseStudentRes = new SysCloseStudentRes();
		sysCloseStudentRes.setPacketHead(this.getPacketHead());
		sysCloseStudentRes.setOption(1, roomId);
		sysCloseStudentRes.setOption(254, "关闭学员通讯通道成功");
		
		sysCloseStudentRes.execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysCloseStudentReq;
	}

}