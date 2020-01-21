package com.sa.thread;

import java.net.URLEncoder;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.util.MD5Util;

public class StatisticRoomInfoSync extends BaseSync {

	public StatisticRoomInfoSync() {}

	public StatisticRoomInfoSync(String url, int time) {
		super(url, time);
	}

	@Override
	public void run() {
		while(true) {
			if (null != super.getUrl() && !"".equals(super.getUrl())) {
				try {
					String json = toJson();

					String key = ConfManager.getMd5Key();
					String sign = MD5Util.MD5("getRoomInfoChat"+key);

					json = URLEncoder.encode(json, "utf-8");

					post(json, sign);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(1000*60*super.getTime());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toJson() {
		String strJson = "[";

		Map<String, Integer> roomInfo = ServerDataPool.dataManager.getRoomInfo();

		strJson += "{\"" + "roomInfo" + "\"" + ":" + JSONObject.toJSONString(roomInfo)+ "}]";

		return strJson;
	}

}
