package com.sa.service.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sa.base.ConfManager;
import com.sa.base.ServerDataManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.ChannelExtend;
import com.sa.service.client.ClientLogin;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponebRoomUser;
import com.sa.service.server.SUniqueLogon;
import com.sa.service.server.ServerLogin;
import com.sa.util.Constant;
import com.sa.util.HttpClientUtil;
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
		ChannelExtend ce = ServerDataPool.TEMP_CONN_MAP.get(context);
		/** 如果为空 */
		if (null == ce || null == ce.getConnBeginTime()) {
			return;
		}
		
		/** 将 发信人id 和 通道信息 放入 临时通道缓存 */
		ServerDataPool.TEMP_CONN_MAP2.put(loginPact.getFromUserId(), context);

		/** 如果 发信人 是 中心 并 通信地址是中心地址 */
		if ("0".equals(loginPact.getFromUserId()) && strIp.equals(ConfManager.getCenterIp())) {
			/** 将用户信息注册 */
			ServerManager.INSTANCE.addOnlineContext(loginPact.getRoomId(), loginPact.getFromUserId(),
					(String) loginPact.getOption(3), (String) loginPact.getOption(4), new HashSet<String>(),
					ConfManager.getValidateEnable(), context, ce.getChannelType());
			/** 登录信息 下行 处理 */
			clientLogin(loginPact, code, msg, "", context);

			return;
		}
		
		/** 获取 用户 角色 */
		String role = (String) loginPact.getOption(2);

		/** 是否 启用 外部校验 */
		boolean validEnable = ConfManager.getValidateEnable();
		/** 如果 有 外部校验 */
		if (validEnable) {
			//进行外部校验 返回false 则验证失败 直接返回
			String validateResult = doRemoteValidate(context, loginPact, role);
			if (validateResult.equals("false")) {
				return;
			}
			role = validateResult;
		}
		/** 格式化 用户角色 */
		HashSet<String> userRole = toRole(role);
		//进行角色校验  返回非空map 则验证失败 
		Map<String, Object> roleValidate = doRoleValidate(userRole, role);
		if (!roleValidate.isEmpty()) {
			code = (int) roleValidate.get("code");
			msg = (String) roleValidate.get("msg");
			/** 登录信息 下行 处理 */
			clientLogin(loginPact, code, msg, role, context);
			return;
		}

		/** 如果 有 中心 */
		if (ConfManager.getIsCenter()) {
			SUniqueLogon uniqueLogon = new SUniqueLogon(loginPact.getPacketHead());
			uniqueLogon.setOptions(loginPact.getOptions());
			uniqueLogon.setOption(254, "uniqueLogon");
			/** 转发 登录信息 上行 到中心 */
			ServerManager.INSTANCE.sendPacketToCenter(uniqueLogon, Constant.CONSOLE_CODE_TS);

			return;
		} else {
			
			/** 校验 单点登录 判断是否是普通用户 单点登录 或 所有用户 首次登录 */
			//判断用户是否登陆过 code=1未登录 code=0已登录
			Map<String,Object> checkUniqueLogonResult = checkUniqueLogon(loginPact, userRole, context);
			
			/** 返回：code!=1 教師二次登錄 */
			if((!"1".equals(String.valueOf(checkUniqueLogonResult.get("code"))))&&(!"0".equals(String.valueOf(checkUniqueLogonResult.get("code"))))){
				code = 10093;
				msg = Constant.ERR_CODE_10093;
			} else if("0".equals(String.valueOf(checkUniqueLogonResult.get("code")))){
				//已登录则注销上次登录
				doLogonUngister(loginPact, (ChannelHandlerContext)checkUniqueLogonResult.get("result"));
			}

			if (10093 != code) {
			/** 普通用户 单点登录 或 所有用户 首次登录  重新注册登录*/
				doLogin(loginPact,ce,userRole,context,role);
			}
		}

		/** 登录信息 下行 处理 */
		clientLogin(loginPact, code, msg, role, context);

		/** 删除 缓存通道 */
		ServerDataPool.TEMP_CONN_MAP2.remove(loginPact.getFromUserId());
		ServerDataPool.TEMP_CONN_MAP.remove(context);
	}

	private void doLogin(ServerLogin loginPact,ChannelExtend ce,HashSet<String> userRole,ChannelHandlerContext context,String role) {
		/** 注册用户上线信息 */
		ServerManager.INSTANCE.addOnlineContext(loginPact.getRoomId(), loginPact.getFromUserId(),
				(String) loginPact.getOption(3), (String) loginPact.getOption(4),
				(String) loginPact.getOption(5), userRole, ConfManager.getTalkEnable(), context,
				ce.getChannelType());
		//给房间用户发消息 通知用户注册
		noticeUserRegister(loginPact,userRole,role);
	}

	private void doLogonUngister(ServerLogin sl,ChannelHandlerContext temp) {
		/** 实例化 消息回执 */
		ClientMsgReceipt mr = new ClientMsgReceipt(sl.getTransactionId(), sl.getRoomId(), sl.getFromUserId(),
				10098);
		mr.setOption(254, Constant.ERR_CODE_10098);
		/** 发送 消息回执 *///给原通道
		try {
			ServerManager.INSTANCE.sendPacketTo(mr, temp, Constant.CONSOLE_CODE_S);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO
		//给房间用户发消息 通知用户注销
		mr.setFromUserId(sl.getFromUserId());
		noticeUserUngister(mr);

		/** 注销通道 */
		ServerManager.INSTANCE.ungisterUserContext(temp);
	}

	private Map<String, Object> doRoleValidate(HashSet<String> userRole, String role) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if ("6".equals(role)) {
			resultMap.put("code", 10090);
			resultMap.put("msg", Constant.ERR_CODE_10060);
		}

		/** 如果 用户角色 不为空 并 角色集长度 大于 0 */
		if (null == userRole || 0 >= userRole.size()) {
			resultMap.put("code", 10090);
			resultMap.put("msg", Constant.ERR_CODE_10090);
		}
		return resultMap;
	}

	private String doRemoteValidate(ChannelHandlerContext context, ServerLogin loginPact, String role) {
		String strUserId = loginPact.getFromUserId();
		/*if ("1".equals(role) || Constant.ROLE_TEACHER.equals(role)) {
			strUserId = strUserId.replace("APP", "");
		}*/
		String tmpUserId = strUserId.replaceAll("T", "").replaceAll("t", "").replaceAll("J", "").replaceAll("j", "");
		/** 获取 用户 token */
		String token = (String) loginPact.getOption(6);
		/** 用户 远程校验 */
		// 多个房间都通过校验 才算校验通过
		String remote = remoteValidate(loginPact.getRoomId(), tmpUserId, role, token);
		System.out.println("用户远程校验【 " + strUserId + " 】\t" + remote);

		JSONObject jsonObj = JSON.parseObject(remote);
		if (null == jsonObj || null == jsonObj.get("meta")) {
			clientLogin(loginPact, 10101, "用户 远程校验失败", role, context);
			return "false";
		}

		JSONObject jsonObj1 = (JSONObject) jsonObj.get("meta");
		if (jsonObj1.getBoolean("success")) {
			role = jsonObj.getString("data");
		} else {
			clientLogin(loginPact, jsonObj1.getIntValue("code"), jsonObj1.getString("message"), role, context);
			return "false";
		}
		return role;
	}

	private void clientLogin(ServerLogin loginPact, int code, String msg, String role, ChannelHandlerContext context) {
		ClientLogin cl = new ClientLogin(loginPact.getPacketHead());

		cl.setToUserId(loginPact.getFromUserId());
		cl.setStatus(code);
		cl.setOption(1, loginPact.getOption(1));
		cl.setOption(2, role);
		cl.setOption(254, msg);

		try {
			// if (loginPact.getRemoteIp().equals(ConfManager.getCenterIp())) {
			// ServerManager.INSTANCE.sendPacketTo2(cl, context,
			// Constant.CONSOLE_CODE_S);
			// } else {
			ServerManager.INSTANCE.sendPacketTo(cl, context, Constant.CONSOLE_CODE_S);
			// }
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
			
		} else if ("0".equals(role)) {
			userRole.add(Constant.ROLE_PARENT_TEACHER);
		} else {
			userRole.add(role);
		}

		return userRole;
	}

	/** 远程校验用户登录 */
	private String remoteValidate(String roomId, String userId, String role, String token) {

		String url = ConfManager.getRemoteValidateUrl();

		Map<String, String> params = new HashMap<>();
		params.put("courseId", roomId);
		params.put("userId", userId);
		params.put("role", role);
		params.put("token", token);
		String rs = HttpClientUtil.post(url, params);

		return rs;
	}

	/** 校验 单点登录 */
	public Map<String,Object> checkUniqueLogon(ServerLogin sl, HashSet<String> userRole, ChannelHandlerContext context) {
		Map<String,Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("result", null);

		if ("".equals(sl.getFromUserId())) {
			map.put("code", 2000);
		}
		
		/** 根据 用户id 获取 用户通道 */
		ChannelHandlerContext temp = ServerDataPool.USER_CHANNEL_MAP.get(sl.getFromUserId());
		/** 如果 用户通道 不为空 */
		if (null != temp) {
			/** 如果 不是 教师  和主讲老师*/
			//if (!(userRole.contains(Constant.ROLE_TEACHER)||userRole.contains(Constant.ROLE_PARENT_TEACHER))) {
				map.put("code", 0);
				map.put("result", temp);
			/*} else {
				map.put("code", 10093);
			}*/
		}
		return map;
	}

	/** 通知房间内用户 */
	private void noticeUserUngister(ClientMsgReceipt mr) {
		String[] roomIds = null;
		//向用户原来所在房间发减员消息 
		String roomNo = ServerDataPool.serverDataManager.getUserRoomNo(mr.getFromUserId());
		if(null!=roomNo){
			roomIds = roomNo.split(",");
		}
		if (roomIds != null && roomIds.length > 0) {
			// 循环通知每个房间的用户
			for (String rId : roomIds) {
				ClientResponebRoomUser crru = new ClientResponebRoomUser(mr.getPacketHead());
				crru.setFromUserId("0");
				crru.setToUserId("0");
				crru.setStatus(0);
				crru.setOption(12, mr.getToUserId());
				crru.setRoomId(rId);
				crru.execPacket();
			}
		}
	}
	
	/** 通知房间内用户 */
	private void noticeUserRegister(ServerLogin loginPact,HashSet<String> userRole,String role) {
		String[] roomIds = loginPact.getRoomId().split(",");
		if (roomIds != null && roomIds.length > 0) {
			for (String rId : roomIds) {
				/** 实例化 获取房间用户列表 下行 并 赋值 并 执行 */
				int num = ServerDataPool.serverDataManager.getRoomTheSameUserCannotAccessNum(rId,
						loginPact.getFromUserId());
				/** 用户不是教师 */
				if (!((userRole.contains(Constant.ROLE_TEACHER)||userRole.contains(Constant.ROLE_PARENT_TEACHER)) && num > 1)) {
					/** 实例化 房间用户列表 下行 */
					ClientResponebRoomUser crru = new ClientResponebRoomUser(loginPact.getPacketHead());
					crru.setOption(11,
							"{\"id\":\"" + loginPact.getFromUserId() + "\",\"name\":\"" + loginPact.getOption(3)
									+ "\",\"icon\":\"" + loginPact.getOption(4) + "\",\"role\":[\"" + role
									+ "\"],\"agoraId\":\"" + loginPact.getOption(5) + "\"}");
					crru.setRoomId(rId);
					crru.execPacket();
				}
			}
		}
	}
}
