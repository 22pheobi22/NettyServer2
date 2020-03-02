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

import com.sa.base.SystemServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;

/**
 * 同步共享
 * @author zyh
 *
 */
public class SysShareGetRes extends Packet {

	@Override
	public void execPacket() {
		try {
			SystemServerManager.INSTANCE.sendPacketTo(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysShareGetRes;
	}

}