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
import com.sa.net.Packet;
import com.sa.net.PacketType;

public class SysCloseRoomReq extends Packet {

	public SysCloseRoomReq(){
	}

	@Override
	public void execPacket() {
		String roomId = (String) this.getOption(1);
		/** 删除 房间 消息 缓存 */
		ServerDataPool.dataManager.cleanLogs(roomId);
		/** 删除 房间 缓存 */
		ServerDataPool.dataManager.removeRoom(roomId);
		/** 删除 房间 空闲 计数 缓存 */
		ServerDataPool.dataManager.cancelFreeRoom(roomId);
		
		SysCloseRoomRes sysCloseRoomRes = new SysCloseRoomRes();
		sysCloseRoomRes.setPacketHead(this.getPacketHead());
		sysCloseRoomRes.setOptions(this.getOptions());
		sysCloseRoomRes.setOption(254, "关闭房间成功");
		sysCloseRoomRes.execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysCloseRoomReq;
	}

}