package com.sa.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.service.client.COffline;
import com.sa.service.client.CUniqueLogon;
import com.sa.service.client.ClientHeartBeat;
import com.sa.service.client.ClientLogin;
import com.sa.service.client.ClientLoginOut;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponebAll;
import com.sa.service.client.ClientResponebOne;
import com.sa.service.client.ClientResponebRoom;
import com.sa.service.client.ClientResponebRoomChat;
import com.sa.service.client.ClientResponebRoomTeacher;
import com.sa.service.client.ClientResponebRoomUser;
import com.sa.service.client.ClientResponebShareGet;
import com.sa.service.client.ClientResponebShareHistory;
import com.sa.service.client.ClientResponebShareUpd;
import com.sa.service.client.ClientResponebShareUpdSelf;
import com.sa.service.client.ClientResponecAgreeApplyAuth;
import com.sa.service.client.ClientResponecApplyAuth;
import com.sa.service.client.ClientResponecBegin;
import com.sa.service.client.ClientResponecGag;
import com.sa.service.client.ClientResponecNotGag;
import com.sa.service.client.ClientResponecRemove;
import com.sa.service.client.ClientResponecRoomRemove;
import com.sa.service.client.ClientResponecShareRemove;
import com.sa.service.client.ClientResponehRoomUser;
import com.sa.service.server.SUniqueLogon;
import com.sa.service.server.ServerHeartBeat;
import com.sa.service.server.ServerLogin;
import com.sa.service.server.ServerLoginOut;
import com.sa.service.server.ServerRequestbAll;
import com.sa.service.server.ServerRequestbOne;
import com.sa.service.server.ServerRequestbRoom;
import com.sa.service.server.ServerRequestbRoomChat;
import com.sa.service.server.ServerRequestbRoomTeacher;
import com.sa.service.server.ServerRequestbRoomUser;
import com.sa.service.server.ServerRequestbShareGet;
import com.sa.service.server.ServerRequestbShareHistory;
import com.sa.service.server.ServerRequestbShareUpd;
import com.sa.service.server.ServerRequestbShareUpdSelf;
import com.sa.service.server.ServerRequestcAgreeApplyAuth;
import com.sa.service.server.ServerRequestcApplyAuth;
import com.sa.service.server.ServerRequestcBegin;
import com.sa.service.server.ServerRequestcGag;
import com.sa.service.server.ServerRequestcNotGag;
import com.sa.service.server.ServerRequestcRemove;
import com.sa.service.server.ServerRequestcRoomRemove;
import com.sa.service.server.ServerRequestcShareRemove;
import com.sa.service.server.ServerRequesthRoomUser;
import com.sa.service.sys.SysChangeSpeakStatusReq;
import com.sa.service.sys.SysChangeSpeakStatusRes;
import com.sa.service.sys.SysCloseRoomReq;
import com.sa.service.sys.SysCloseRoomRes;
import com.sa.service.sys.SysCloseStudentReq;
import com.sa.service.sys.SysCloseStudentRes;
import com.sa.service.sys.SysGetRoomStudentsReq;
import com.sa.service.sys.SysGetRoomStudentsRes;
import com.sa.service.sys.SysGetRoomsReq;
import com.sa.service.sys.SysGetRoomsRes;
import com.sa.service.sys.SysLoginOutReq;
import com.sa.service.sys.SysLoginOutRes;
import com.sa.service.sys.SysLoginReq;
import com.sa.service.sys.SysLoginRes;
import com.sa.service.sys.SysReloadConfigReq;
import com.sa.service.sys.SysReloadConfigRes;
import com.sa.service.sys.SysSendMsgAllReq;
import com.sa.service.sys.SysSendMsgAllRes;
import com.sa.service.sys.SysSendMsgOneReq;
import com.sa.service.sys.SysSendMsgOneRes;
import com.sa.service.sys.SysSendMsgRoomReq;
import com.sa.service.sys.SysSendMsgRoomRes;
import com.sa.service.sys.SysShareGetReq;
import com.sa.service.sys.SysShareGetRes;
import com.sa.service.sys.SysUpdRoomReq;
import com.sa.service.sys.SysUpdRoomRes;

public enum PacketType {
	// 业务上行数据包
	ServerLogin((short) 101, ServerLogin.class),						// 上行 注册
	ServerHearBeat((short) 102, ServerHeartBeat.class),				// 上行 心跳
	SUniqueLogon((short) 103, SUniqueLogon.class),					// 上行 校验
	ServerLoginOut((short) 105, ServerLoginOut.class),				// 上行 退出

	// 业务下行数据包
	ClientLogin((short) 201, ClientLogin.class),						// 下行 注册
	CUniqueLogon((short) 203, CUniqueLogon.class),					// 下行 校验
	ClientLoginOut((short) 205, ClientLoginOut.class),				// 下行 退出
	COffline((short) 204, COffline.class),							// 下行 被迫下线
	ClientHeartBeat((short) 202, ClientHeartBeat.class),				// 下行 心跳

	// 业务上行数据包
	ServerRequestbOne((short) 301, ServerRequestbOne.class),          // 上行 发消息-单人
	ServerRequestbRoom((short) 302, ServerRequestbRoom.class),		// 上行 发消息-房间内
	ServerRequestbAll((short) 303, ServerRequestbAll.class),			// 上行 发消息-所有人
	ServerRequestbRoomUser((short) 304, ServerRequestbRoomUser.class),// 上行 房间内用户列表
	ServerRequestbRoomChat((short) 313, ServerRequestbRoomChat.class),// 上行 房间聊天记录
	ServerRequestbRoomTeacher((short) 322, ServerRequestbRoomTeacher.class),// 上行 房间内普通教师列表

	// 共享上行数据包
	ServerRequestbShareUpdSelf((short) 323, ServerRequestbShareUpdSelf.class),// 上行 修改共享 仅回执
	ServerRequestbShareGet((short) 305, ServerRequestbShareGet.class),	// 上行 获取共享
	ServerRequestbShareHistory((short) 315, ServerRequestbShareHistory.class),// 上行 获取共享历史
	ServerRequestbShareUpd((short) 306, ServerRequestbShareUpd.class),	// 上行 修改共享
	ServerRequestcShareRemove((short) 307,ServerRequestcShareRemove.class),	// 上行 共享删除
	ServerRequestcRoomRemove((short) 308, ServerRequestcRoomRemove.class),	// 上行 移出房间
	ServerRequestcBegin((short) 309, ServerRequestcBegin.class),				// 上行 开课
	// 控制上行数据包
	ServerRequestcGag((short) 310, ServerRequestcGag.class),			// 上行 禁言
	ServerRequestcNotGag((short) 311, ServerRequestcNotGag.class),	// 上行 解除禁言
	ServerRequestcRemove((short) 312, ServerRequestcRemove.class),	// 上行 踢人
	// HTTP 获取用户列表
	ServerRequesthRoomUser((short) 314, ServerRequesthRoomUser.class),// 上行 获取用户列表 HTTP
	ServerRequestcApplyAuth((short) 320, ServerRequestcApplyAuth.class),	// 上行 申请开通权限
	ServerRequestcAgreeApplyAuth((short) 321, ServerRequestcAgreeApplyAuth.class),	// 上行 申请开通权限同意

	// 业务下行数据包
	ClientResponebOne((short)401, ClientResponebOne.class),			// 下行 发消息-单人
	ClientResponebRoom((short)402, ClientResponebRoom.class),		// 下行 发消息-房间内
	ClientResponebAll((short)403, ClientResponebAll.class),			// 下行 发消息-所有人
	ClientResponebRoomUser((short)404,ClientResponebRoomUser.class),	// 下行 房间内用户列表
	ClientResponebRoomChat((short)413,ClientResponebRoomChat.class), // 下行 房间聊天记录
	ClientResponebRoomTeacher((short)422,ClientResponebRoomTeacher.class),	// 下行 房间内用户列表

	// 共享下行行数据包
	ClientResponebShareUpdSelf((short)423,ClientResponebShareUpdSelf.class),	// 下行 共享修改 仅回执
	ClientResponebShareGet((short)405,ClientResponebShareGet.class), // 下行 获取共享内容
	ClientResponebShareHistory((short)415,ClientResponebShareHistory.class),// 下行 获取共享历史
	ClientResponebShareUpd((short)406,ClientResponebShareUpd.class), // 下行 共享修改
	ClientResponecShareRemove((short)407,ClientResponecShareRemove.class),	// 下行 共享删除
	ClientResponecRoomRemove((short)408, ClientResponecRoomRemove.class),	// 下行 移出房间
	ClientResponecBegin((short)409, ClientResponecBegin.class),	    // 下行 开课
	ClientResponecGag((short)410, ClientResponecGag.class),	    // 下行 禁言
	ClientResponecNotGag((short)411, ClientResponecNotGag.class),	    // 下行 解除禁言
	ClientResponecRemove((short)412, ClientResponecRemove.class),	// 下行 踢人
	ClientResponecApplyAuth((short)420, ClientResponecApplyAuth.class),	// 下行 申请开通权限
	ClientResponecAgreeApplyAuth((short)421, ClientResponecAgreeApplyAuth.class),	// 下行 申请开通权限同意
	// HTTP 获取用户列表
	ClientResponehRoomUser((short)414, ClientResponehRoomUser.class),// 下行 获取用户列表 HTTP

	// 下行 消息回执数据包
	MsgReceipt((short)501, ClientMsgReceipt.class),

	// 管理系统消息
	SysLoginReq((short) 10001, SysLoginReq.class),
	SysLoginRes((short) 10002, SysLoginRes.class),
	SysGetRoomsReq((short) 10003, SysGetRoomsReq.class),
	SysGetRoomsRes((short) 10004, SysGetRoomsRes.class),
	SysGetRoomStudentsReq((short) 10005, SysGetRoomStudentsReq.class),
	SysGetRoomStudentsRes((short) 10006, SysGetRoomStudentsRes.class),
	SysLoginOutReq((short) 10007, SysLoginOutReq.class),
	SysLoginOutRes((short) 10008, SysLoginOutRes.class),
	SysCloseRoomReq((short) 10009, SysCloseRoomReq.class),
	SysCloseRoomRes((short) 10010, SysCloseRoomRes.class),
	SysUpdRoomReq((short) 10011, SysUpdRoomReq.class),
	SysUpdRoomRes((short) 10012, SysUpdRoomRes.class),
	SysChangeSpeakStatusReq((short) 10013, SysChangeSpeakStatusReq.class),
	SysChangeSpeakStatusRes((short) 10014, SysChangeSpeakStatusRes.class),
	SysCloseStudentReq((short) 10015, SysCloseStudentReq.class),
	SysCloseStudentRes((short) 10016, SysCloseStudentRes.class),
	SysReloadConfigReq((short) 10017, SysReloadConfigReq.class),
	SysReloadConfigRes((short) 10018, SysReloadConfigRes.class),
	SysSendMsgOneReq((short) 10019, SysSendMsgOneReq.class),
	SysSendMsgOneRes((short) 10020, SysSendMsgOneRes.class),
	SysSendMsgRoomReq((short) 10021, SysSendMsgRoomReq.class),
	SysSendMsgRoomRes((short) 10022, SysSendMsgRoomRes.class),
	SysSendMsgAllReq((short) 10023, SysSendMsgAllReq.class),
	SysSendMsgAllRes((short) 10024, SysSendMsgAllRes.class),
	SysShareGetReq((short) 10025, SysShareGetReq.class),
	SysShareGetRes((short) 10026, SysShareGetRes.class)
	;

	private short type;	// 消息类型
	private Class<? extends Packet> packetClass;
	private static Map<Short,Class<? extends Packet>> PACKET_CLASS_MAP = new HashMap<Short,Class<? extends Packet>>();

	static{
		// 实例化消息类型集合
		Set<Short> typeSet = new HashSet<Short>();
		// 遍历消息类型
		for(PacketType p:PacketType.values()){
			Short type = p.getType();
			// 如果消息类型包含已有类型
			if(typeSet.contains(type)){
				// 抛出非法状态异常
				throw new IllegalStateException("packet type 协议类型重复"+type);
			}
			// 将类型存放到包类Map
			PACKET_CLASS_MAP.put(type,p.getPacketClass());
			// 消息类型集合添加类型
			typeSet.add(type);
		}
	}

	PacketType(short type,Class<? extends Packet> packetClass){
		this.setType(type);
		this.packetClass = packetClass;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public Class<? extends Packet> getPacketClass() {
		return packetClass;
	}

	public void setPacketClass(Class<? extends Packet> packetClass) {
		this.packetClass = packetClass;
	}


	public static  Class<? extends Packet> getPacketClassBy(short packetType){
		return PACKET_CLASS_MAP.get(packetType);
	}

	public static void main(String[] args) {
		for(PacketType p:PacketType.values()){
			System.err.println(p.getPacketClass().getSimpleName());
		}
	}

}
