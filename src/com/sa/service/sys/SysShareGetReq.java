/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.server]
 * 类名称: [ServerRequestcGetShare]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月11日 下午5:52:27]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月11日 下午5:52:27]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.sys;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.sa.base.ServerDataPool;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponebShareGet;

/**
 * 同步共享
 * @author zyh
 *
 */
public class SysShareGetReq extends Packet {
	public SysShareGetReq(){}

	@Override
	public void execPacket() {
			/** 获取选项 1 的内容 */
			String op1 = (String) this.getOption(1);
			String op2 = (String) this.getOption(2);
			String userId = (String) this.getOption(3);
			String[] roomIds = this.getRoomId().split(",");
			if (null != roomIds && roomIds.length > 0) {
				for (String rId : roomIds) {
					if ("1".equals(op2)) {
						/** 根据房间id和选项1 获取 共享信息 */
						Object share = ServerDataPool.dataManager.getShare(rId, op1);
						/** 设置共享 */
						/** 实例化获取共享类型 下行 并赋值 */
						ClientResponebShareGet clientResponebShareGet = new ClientResponebShareGet(this.getPacketHead());
						clientResponebShareGet.setOptions(this.getOptions());
						clientResponebShareGet.setOption(3, share);
						clientResponebShareGet.setRoomId(rId);
						clientResponebShareGet.setFromUserId(userId);
						/** 执行 */
						clientResponebShareGet.execPacket();
					} else if ("n".equals(op2)) {
						List<Object> list = ServerDataPool.dataManager.getShareList(rId, op1);
						/** 设置共享 */
						/** 实例化获取共享类型 下行 并赋值 */
						if (null != list) {
							List<Object> temp = new ArrayList<>();
							for (int i = 0; i < list.size(); i++) {
								temp.add(list.get(i));

								if ((i + 1) % 51 == 0 || i + 1 >= list.size()) {
									ClientResponebShareGet clientResponebShareGet = new ClientResponebShareGet(
											this.getPacketHead());
									clientResponebShareGet.setOptions(this.getOptions());
									clientResponebShareGet.setOption(3, JSON.toJSONString(temp));
									clientResponebShareGet.setRoomId(rId);
									clientResponebShareGet.setFromUserId(userId);

									/** 执行 */
									clientResponebShareGet.execPacket();

									temp = new ArrayList<>();
								}
							}
						} else {
							ClientResponebShareGet clientResponebShareGet = new ClientResponebShareGet(this.getPacketHead());
							clientResponebShareGet.setOptions(this.getOptions());
							clientResponebShareGet.setOption(3, "");
							clientResponebShareGet.setRoomId(rId);
							clientResponebShareGet.setFromUserId(userId);

							/** 执行 */
							clientResponebShareGet.execPacket();
						}
					} else {
						ClientResponebShareGet clientResponebShareGet = new ClientResponebShareGet(this.getPacketHead());

						clientResponebShareGet.setStatus(5001);
						clientResponebShareGet.setOption(254, "共享类型错误[option.2]");
						clientResponebShareGet.setRoomId(rId);

						clientResponebShareGet.execPacket();
					}
				}
			}
			
			SysShareGetRes res=new SysShareGetRes();
			res.setPacketHead(this.getPacketHead());
			res.setOption(254, "同步共享消息成功！");
			res.execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysShareGetReq;
	}

}
