package com.sa.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sa.util.StringUtil;

public class ConfManager {
	/** 运行配置 */
	public static Map<String, String> CONF_MAP = new HashMap<>();

	// 获取服务ip和端口
	/** 普通服务器ip */
	public static String[] getServerAddress() {
		String[] split =null;
		String strAddress = CONF_MAP.get("server.address");
		if(null!=strAddress){
			split = strAddress.split(";");
		}
		return split;
	}

	// 获取另一个中心的ip
	/** 另一个中心的ip */
	public static String getCenterIpAnother() {
		String strIp = CONF_MAP.get("center.ip.another");
		strIp = null == strIp || "".equals(strIp) ? "127.0.0.1" : strIp;

		return strIp;
	}
	/** 另一个中心的ip */
	public static List<String> getCenterId() {
		String centerIds = CONF_MAP.get("center.id");
		centerIds = null == centerIds || "".equals(centerIds) ? "127.0.0.1" : centerIds;

		return Arrays.asList(centerIds.split(";"));
	}

	// 获取另一个中心的端口
	/** 另一个中心的端口 */
	public static int getCenterPortAnother() {
		String strPort = CONF_MAP.get("center.port.another");
		strPort = null == strPort || "".equals(strPort) ? "8081" : strPort;

		return Integer.parseInt(strPort);
	}

	/** 服务器端口 */
	public static int getClientSoketServerPort() {
		String strPort = CONF_MAP.get("clientsoket.server.port");

		strPort = null == strPort || "".equals(strPort) ? "8080" : strPort;

		return Integer.parseInt(strPort);
	}
	
	/** 给中心提供服务的端口 */
	public static int getCenterSoketServerPort() {
		String strPort = CONF_MAP.get("centersoket.server.port");

		strPort = null == strPort || "".equals(strPort) ? "8280" : strPort;

		return Integer.parseInt(strPort);
	}

	/** 服务器端口 */
	public static int getWebSoketServerPort() {
		String strPort = CONF_MAP.get("websoket.server.port");

		strPort = null == strPort || "".equals(strPort) ? "8090" : strPort;

		return Integer.parseInt(strPort);
	}

	/** 客户端断线重连最大尝试次数 */
	public static int getMaxReconnectTimes() {
		String strMaxReconnectTimes = CONF_MAP.get("max.reconnect.times");

		if (StringUtil.isEmpty(strMaxReconnectTimes) || !StringUtil.isInteger(strMaxReconnectTimes)) {
			strMaxReconnectTimes = "10";
		}

		return Integer.parseInt(strMaxReconnectTimes);
	}

	/** 报文是否输出 */
	public static Boolean getConsoleFlag() {
		String strConsoleFlag = CONF_MAP.get("console.flag");

		if (StringUtil.isEmpty(strConsoleFlag)) {
			strConsoleFlag = "true";
		}

		return Boolean.parseBoolean(strConsoleFlag);
	}

	/** 中心是否存在 */
	public static Boolean getIsCenter() {
		String strIsCenter = CONF_MAP.get("is.center");

		if (StringUtil.isEmpty(strIsCenter)) {
			strIsCenter = "false";
		}

		return Boolean.parseBoolean(strIsCenter);
	}
	
	/** 是否使用redis*/
	public static Boolean getIsRedis() {
		String strIsCenter = CONF_MAP.get("is.redis");

		if (StringUtil.isEmpty(strIsCenter)) {
			strIsCenter = "false";
		}

		return Boolean.parseBoolean(strIsCenter);
	}

	/** 间隔同步入库地址 */
	public static String getLogUrl() {
		return CONF_MAP.get("log.out.db.url");
	}

	/** 间隔同步入库时间（单位：分钟） */
	public static int getLogTime() {
		String strLogTime = CONF_MAP.get("log.out.db.time.minute");

		if (StringUtil.isEmpty(strLogTime) || !StringUtil.isInteger(strLogTime)) {
			strLogTime = "1";
		}

		return Integer.parseInt(strLogTime);
	}

	/** 管理员权限 */
	public static int getAdmin() {
		String admin = CONF_MAP.get("permission.admin");

		if (StringUtil.isEmpty(admin) || !StringUtil.isInteger(admin)) {
			admin = "1";
		}

		return Integer.parseInt(admin);
	}

	/** 副管理员权限 */
	public static int getAssistant() {
		String assistant = CONF_MAP.get("permission.assistant");

		if (StringUtil.isEmpty(assistant) || !StringUtil.isInteger(assistant)) {
			assistant = "2";
		}

		return Integer.parseInt(assistant);
	}

	/** 普通用户权限 */
	public static int getNormal() {
		String normal = CONF_MAP.get("permission.normal");

		if (StringUtil.isEmpty(normal) || !StringUtil.isInteger(normal)) {
			normal = "3";
		}

		return Integer.parseInt(normal);
	}

	/** 旁听用户权限 */
	public static int getAudience() {
		String audience = CONF_MAP.get("permission.audience");

		if (StringUtil.isEmpty(audience) || !StringUtil.isInteger(audience)) {
			audience = "4";
		}

		return Integer.parseInt(audience);
	}

	/** 是否启用外部校验 */
	public static Boolean getValidateEnable() {
		String admin = CONF_MAP.get("validate.enable");

		if (StringUtil.isEmpty(admin)) {
			admin = "false";
		}

		return Boolean.parseBoolean(admin);
	}

	/** 三方校验地址 */
	public static String getValidateUrl() {
		return CONF_MAP.get("validate.url");
	}

	/** 开课前是否可以说话 */
	public static Boolean getTalkEnable() {
		String talkEnable = CONF_MAP.get("talk.enable");
		if (StringUtil.isEmpty(talkEnable)) {
			talkEnable = "true";
		}
		return Boolean.parseBoolean(talkEnable);
	}

	/** 保存 文件日志 地址 */
	public static String getFileLogPath() {
		return CONF_MAP.get("file.log.path");
	}

	/** 是否 保存 文件日志 */
	public static Boolean getFileLogFlag() {
		String strFileLogFlag = CONF_MAP.get("file.log.flag");
		if (StringUtil.isEmpty(strFileLogFlag)) {
			strFileLogFlag = "true";
		}

		return Boolean.parseBoolean(strFileLogFlag);
	}

	/** 保存 文件日志 地址 */
	public static String getStatisticUrl() {
		return CONF_MAP.get("statistic.url");
	}

	/** 间隔同步入库时间（单位：分钟） */
	public static int getStatisticTime() {
		String strStatisticTime = CONF_MAP.get("statistic.time");

		if (StringUtil.isEmpty(strStatisticTime) || !StringUtil.isInteger(strStatisticTime)) {
			strStatisticTime = "1";
		}

		return Integer.parseInt(strStatisticTime);
	}

	/** 验签 公钥 */
	public static String getSignKey() {
		String strSignKey = CONF_MAP.get("sign.key");

		if (StringUtil.isEmpty(strSignKey) || !StringUtil.isInteger(strSignKey)) {
			strSignKey = "9527";
		}

		return strSignKey;
	}

	/** 验签 超时时长 */
	public static Long getSignOverTime() {
		String strOverTime = CONF_MAP.get("sign.overtime");

		if (StringUtil.isEmpty(strOverTime) || !StringUtil.isInteger(strOverTime)) {
			strOverTime = "60000";
		}

		return Long.parseLong(strOverTime);
	}

	/** 后台 远程连接 端口 */
	public static int getHttpPort() {
		String strHttpPort = CONF_MAP.get("http.port");

		if (StringUtil.isEmpty(strHttpPort) || !StringUtil.isInteger(strHttpPort)) {
			strHttpPort = "8090";
		}

		return Integer.parseInt(strHttpPort);
	}

	/** 连接 中心 的端口 */
	public static int getCenterPort() {
		String strCenterPort = CONF_MAP.get("center.port");

		if (StringUtil.isEmpty(strCenterPort) || !StringUtil.isInteger(strCenterPort)) {
			strCenterPort = "9090";
		}

		return Integer.parseInt(strCenterPort);
	}

	/** Md5 公钥 */
	public static String getMd5Key() {
		String strSignKey = CONF_MAP.get("md5.key");

		if (StringUtil.isEmpty(strSignKey) || !StringUtil.isInteger(strSignKey)) {
			strSignKey = "6783c950bdbf40aeac52042a9206e0ba";
		}

		return strSignKey;
	}

	/** 日志 key的分割符 */
	public static String getLogKeySplit() {
		String strLogKeySplit = CONF_MAP.get("log.key.split");

		if (StringUtil.isEmpty(strLogKeySplit) || !StringUtil.isInteger(strLogKeySplit)) {
			strLogKeySplit = "LOGSPLIT";
		}

		return strLogKeySplit;
	}

	/**
	 * 自动回收时存储星星数的连接
	 */
	public static String getStatSaveUrl() {
		return CONF_MAP.get("stat.save.url");
	}

	public static String getMongoIp() {
		return CONF_MAP.get("mongo.ip");
	}

	public static Integer getMongoPort() {
		return Integer.parseInt(CONF_MAP.get("mongo.port"));
	}

	public static String getMongoNettyLogDBName() {
		return CONF_MAP.get("mongo.log.netty.dbname");
	}

	public static String getMongoNettyLogTableName() {
		return CONF_MAP.get("mongo.log.netty.table.name");
	}

	public static String getMongoNettyLogUserName() {
		return CONF_MAP.get("mongo.log.netty.user.name");
	}

	public static String getMongoNettyLogPassword() {
		return CONF_MAP.get("mongo.log.netty.user.password");
	}

	public static int getTimelyDealLogMaxThreshold() {
		return Integer.parseInt(CONF_MAP.get("log.out.timely.deal.max.threshold"));
	}

	public static int getLogBatchSaveMaxSize() {
		return Integer.parseInt(CONF_MAP.get("mongo.log.netty.batch.save.max.size"));
	}

	/** 远程校验地址 */
	public static String getRemoteValidateUrl() {
		return CONF_MAP.get("remote.validate.url");
	}

	/** 是否启用mongodb */
	public static Boolean getMongodbEnable() {
		String admin = CONF_MAP.get("mongodb.enable");

		if (StringUtil.isEmpty(admin)) {
			admin = "false";
		}

		return Boolean.parseBoolean(admin);
	}
	
	/** 新增服务器ip */
	public static String[] getAddAddress() {
		String[] split =null;
		String strAddress = CONF_MAP.get("server.add.address");
		if(null!=strAddress){
			split = strAddress.split(";");
		}
		return split;
	}
}
