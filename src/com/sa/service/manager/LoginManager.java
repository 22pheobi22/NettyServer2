package com.sa.service.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.service.client.ClientLogin;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponebRoomUser;
import com.sa.service.server.SUniqueLogon;
import com.sa.service.server.ServerLogin;
import com.sa.util.Constant;
import com.sa.util.HttpClientUtil;
import com.sa.util.MD5Util;
import com.sa.util.StringUtil;

import io.netty.channel.ChannelHandlerContext;

public enum LoginManager {

	INSTANCE;

	public void login(ChannelHandlerContext context, ServerLogin loginPact) {
		String strIp = context.channel().remoteAddress().toString();
		strIp = StringUtil.subStringIp(strIp);

		int code = 0;
		String msg = "成功";
		/** 获取 临时通道 状态*/
		Integer status = ServerDataPool.TEMP_CONN_MAP.get(context);
		/** 如果为空*/
		if (null == status) {
			return;
		}
		/** 将 发信人id 和 通道信息 放入 临时通道缓存*/
		ServerDataPool.TEMP_CONN_MAP2.put(loginPact.getFromUserId(), context);
		/** 如果 发信人 是 中心 并 通信地址是中心地址*/
		if ("0".equals(loginPact.getFromUserId()) && strIp.equals(ConfManager.getCenterIp())) {
			/** 将用户信息注册*/
			ServerManager.INSTANCE.addOnlineContext(loginPact.getRoomId(),
					loginPact.getFromUserId(), (String) loginPact.getOption(3),
					(String) loginPact.getOption(4), new HashSet<String>(),
					ConfManager.getValidateEnable(), context);
			/** 登录信息 下行 处理*/
			clientLogin(loginPact, code, msg, "", context);

			return;
		}
		/** 获取 用户 角色*/
		String role = (String) loginPact.getOption(2);
		/** 获取 用户 token*/
		String token = (String) loginPact.getOption(6);
		/** 是否 启用 外部校验*/
		boolean validEnable = ConfManager.getValidateEnable();
		/** 如果 有 外部校验*/
		if (validEnable) {
			String strUserId = loginPact.getFromUserId();
			if ("1".equals(role) || Constant.ROLE_TEACHER.equals(role)) {
				strUserId = strUserId.replace("APP", "");
			}
			/** 用户 远程校验*/
			//role = remoteValidate(context, loginPact.getRoomId(), strUserId, (String) loginPact.getOption(1),role,token);
			role = remoteValidate(loginPact.getRoomId(), strUserId,role,token);
		}
		/** 格式化 用户角色*/
		HashSet<String> userRole = toRole(role);
		/** 如果 用户角色 不为空 并 角色集长度 大于 0*/
		if (null != userRole && 0 < userRole.size() && !"6".equals(role)) {
			/** 如果 有 中心*/
			if (ConfManager.getIsCenter()) {
				SUniqueLogon uniqueLogon = new SUniqueLogon(loginPact.getPacketHead());
				uniqueLogon.setOptions(loginPact.getOptions());
				uniqueLogon.setOption(254, "uniqueLogon");
				/** 转发 登录信息 上行 到中心*/
				ServerManager.INSTANCE.sendPacketToCenter(uniqueLogon, Constant.CONSOLE_CODE_TS);

				return;
			} else {
				/** 校验 单点登录 返回：1.普通用户 单点登录 或 所有用户 首次登录*/
				int rs = uniqueLogon(loginPact, userRole, context);
				/** 删除 缓存通道*/
				ServerDataPool.TEMP_CONN_MAP2.remove(context);
				ServerDataPool.TEMP_CONN_MAP.remove(context);
				/** 普通用户 单点登录 或 所有用户 首次登录*/
				if (rs == 1) {
					/** 注册用户上线信息*/
					ServerManager.INSTANCE.addOnlineContext(loginPact.getRoomId(),
							loginPact.getFromUserId(), (String) loginPact.getOption(3),
							(String) loginPact.getOption(4),  (String) loginPact.getOption(5), userRole,
							ConfManager.getTalkEnable(), context);

					/** 实例化 获取房间用户列表 下行 并 赋值 并 执行*/
					int num = ServerDataPool.serverDataManager.getRoomTheSameUserCannotAccessNum(loginPact.getRoomId(), loginPact.getFromUserId());
					/** 用户不是教师 */
					if (!(userRole.contains(Constant.ROLE_TEACHER) && num>1)) {
						/** 实例化 房间用户列表 下行*/
						ClientResponebRoomUser crru = new ClientResponebRoomUser(loginPact.getPacketHead());
						crru.setOption(11, "{\"id\":\""+loginPact.getFromUserId()+"\",\"name\":\""+loginPact.getOption(3)+"\",\"icon\":\""+loginPact.getOption(4)+"\",\"role\":[\""+role+"\"],\"agoraId\":\""+loginPact.getOption(5)+"\"}");

						crru.execPacket();
					}
				} else {
					code = 10093;
					msg = Constant.ERR_CODE_10093;
				}
			}
		} else {
			code = 10090;
			msg = Constant.ERR_CODE_10090;

			if ("6".equals(role)) {
				code = 10090;
				msg = Constant.ERR_CODE_10060;
			}
		}
		/** 登录信息 下行 处理*/
		clientLogin(loginPact, code, msg, role, context);

	}

	private void clientLogin(ServerLogin loginPact, int code, String msg, String role, ChannelHandlerContext context) {
		ClientLogin cl = new ClientLogin(loginPact.getPacketHead());

		cl.setToUserId(loginPact.getFromUserId());
		cl.setStatus(code);
		cl.setOption(1, loginPact.getOption(1));
		cl.setOption(2, role);
		cl.setOption(254, msg);

		try {
//			if (loginPact.getRemoteIp().equals(ConfManager.getCenterIp())) {
//				ServerManager.INSTANCE.sendPacketTo2(cl, context, Constant.CONSOLE_CODE_S);
//			} else {
			ServerManager.INSTANCE.sendPacketTo(cl, context, Constant.CONSOLE_CODE_S);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 格式化 用户角色
	 */
	private HashSet<String> toRole(String role) {
		HashSet<String> userRole = new HashSet<String>();

		if ("1".equals(role)) {
			userRole.add(Constant.ROLE_TEACHER);
		} else if ("2".equals(role)) {
			userRole.add(Constant.ROLE_ASSISTANT);
		} else if ("3".equals(role)) {
			userRole.add(Constant.ROLE_STUDENT);
		} else if ("4".equals(role)) {
			userRole.add(Constant.ROLE_AUDIENCE);
		} else if ("5".equals(role)) {
		} else {
			userRole.add(role);
		}

		return userRole;
	}

	/** 远程校验用户登录*/
	//private String remoteValidate(ChannelHandlerContext context, String roomId, String userId, String userKey,String role,String token) {
	private String remoteValidate(String roomId, String userId, String role,String token) {

		String url=ConfManager.getRemoteValidateUrl();

		//String key = ConfManager.getMd5Key();
		//String sign = MD5Util.MD5(roomId+"authorizeLiveShare"+userId+userKey+key);
		
		Map<String, String> params = new HashMap<>();
		params.put("courseId", roomId);
		params.put("userId", userId);
		//params.put("userKey", userKey);
		//params.put("sign", sign);
		params.put("role", role);
		params.put("token", token);
		String rs = HttpClientUtil.post(url, params);

		return rs;
	}

	/** 校验 单点登录*/
	public int uniqueLogon (ServerLogin sl, HashSet<String> userRole,ChannelHandlerContext context) {
		if ("".equals(sl.getFromUserId())) {
			return 20000;
		}
		/** 根据 用户id 获取 用户通道*/
		ChannelHandlerContext temp = ServerDataPool.USER_CHANNEL_MAP.get(sl.getFromUserId());
		/** 如果 用户通道 不为空*/
		if (null != temp) {
			/** 如果 不是 教师*/
			if (!userRole.contains(Constant.ROLE_TEACHER) || !sl.getFromUserId().endsWith("APP")) {
				/** 实例化 消息回执*/
				ClientMsgReceipt mr = new ClientMsgReceipt(sl.getTransactionId(), sl.getRoomId(), sl.getFromUserId(), 10098);
				mr.setOption(254, Constant.ERR_CODE_10098);
				/** 发送 消息回执*/
				try {
					ServerManager.INSTANCE.sendPacketTo(mr, temp, Constant.CONSOLE_CODE_S);
				} catch (Exception e) {
					e.printStackTrace();
				}

				noticeUser(mr);

				/** 注销通道*/
				ServerManager.INSTANCE.ungisterUserContext(temp);

				return 1;
			} else {
				return 10093;
			}
		}

		return 1;
	}

	/** 通知房间内用户*/
	private void noticeUser(ClientMsgReceipt mr) {
		ClientResponebRoomUser crru = new ClientResponebRoomUser(mr.getPacketHead());
		crru.setFromUserId("0");
		crru.setToUserId("0");
		crru.setStatus(0);
		crru.setOption(12, mr.getToUserId());

		crru.execPacket();
	}
}