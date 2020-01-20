package com.sa.service.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.ChannelExtend;
import com.sa.service.client.ClientLogin;
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
		if (ConfManager.getIsCenter()&&!ConfManager.getCenterId().equals(loginPact.getFromUserId())) {
			SUniqueLogon uniqueLogon = new SUniqueLogon(loginPact.getPacketHead());
			uniqueLogon.setOptions(loginPact.getOptions());
			
			uniqueLogon.setOption(254, "uniqueLogon");
			uniqueLogon.setOption(100, strIp);
			
			//uniqueLogon.setRemoteIp(strIp);//設置本服務器IP
			/** 转发 登录信息 上行 到中心 */
			ServerManager.INSTANCE.sendPacketToCenter(uniqueLogon, Constant.CONSOLE_CODE_TS);
		}else{
			doLogin(loginPact.getFromUserId(),context,ce.getChannelType());
			
			/** 登录信息 下行 处理 */
			clientLogin(loginPact, code, msg, role, context);
		}

	}

	private void doLogin(String userId,ChannelHandlerContext context, int channelType) {
		ServerManager.INSTANCE.addOnlineContext(userId, context, channelType);
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
}
