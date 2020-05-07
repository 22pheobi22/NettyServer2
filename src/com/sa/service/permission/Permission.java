package com.sa.service.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.util.Constant;

public enum Permission {

	INSTANCE;

	/*
	 * 设置消息
	 * Integer code 结果类型
	 * String msg 消息内容
	 */
	private Map<String, Object> setResult(Integer code, String msg) {
		Map<String, Object> map = new HashMap<>();

		map.put("code", code);
		map.put("msg", msg);

		return map;
	}

	/*
	 * 判断用户的权限是否开启
	 * String roomId	房间ID
	 * String userId	用户ID
	 * String auth		被判断权限
	 */
	public Map<String, Object> checkUserAuth(String roomId, String userId, String auth) {
		Integer code = 0;
		String msg = "success";

		String[] roomIds = roomId.split(",");
		if(roomIds!=null&&roomIds.length>0){
			//用户同时操作多房间 须在多房间都具权限才能过校验
			for (String rId : roomIds) {
				HashMap<String, Integer> hm = ServerDataPool.dataManager.getRoomUesrAuth(rId, userId);
				if (null == hm) {
					return setResult(10099, Constant.ERR_CODE_10099);
				}

				if (null == hm.get(auth) || hm.get(auth) == 0) {
					code = 10080;
					msg = Constant.ERR_CODE_10080;
					return setResult(code, msg);
				}

			}
		}

		return setResult(code, msg);
	}
	
	/**
	 * 判断目标用户是否存在于指定房间
	 * */
	public Map<String, Object> checkUserExist(String roomId, String userId) {
		Integer code = 10082;
		String msg = Constant.ERR_CODE_10082;

		String[] roomIds = roomId.split(",");
		if(null!=userId&&null!=roomIds&&roomIds.length>0){
			//多个房间仅一个存在即存在
			for (String rId : roomIds) {
				/** 根据房间id 和 目标用户id 获取 人员信息 */
				People people = ServerDataPool.dataManager.getRoomUesr(rId, userId);
				if (null != people) {
					code = 0;
					msg = "success";
					break;
				}
			}
		}
		return setResult(code, msg);
	}
	
	/*
	 * 判断用户角色是否存在
	 * String roomId	房间ID
	 * String userId	用户ID
	 * String role		被判断角色
	 */
	public Map<String, Object> checkUserRole(String roomId, String userId, Set<String> role) {
		Integer code = 0;
		String msg = "success";
		String[] roomIds = roomId.split(",");
		if(roomIds!=null&&roomIds.length>0){
			//要同时操作多个房间 须同时在多房间具有角色才能通过校验
			for (String rId : roomIds) {
				/** 获取用户角色*/
				HashSet<String> hs = ServerDataPool.dataManager.getRoomUesrRole(rId, userId);
				/** 如果角色为空*/
				if (null == hs) {
					/** 设置消息*/
					return setResult(10099, Constant.ERR_CODE_10099);
				}
				/** 如果角色不包含用户角色*/
				role.retainAll(hs);
				if (role.isEmpty()) {
					code = 10081;
					msg = Constant.ERR_CODE_10081;
					
					return setResult(code, msg);
				}
			}
		}
		return setResult(code, msg);
	}
}
