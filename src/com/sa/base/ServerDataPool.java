package com.sa.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sa.base.element.ChannelExtend;
import com.sa.net.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public class ServerDataPool {
	/** 缓存所有登录用户对应的通信上下文环境（主要用于业务数据处理） */
	public static Map<String, ChannelHandlerContext> USER_CHANNEL_MAP  = new ConcurrentHashMap<>();
	/** 缓存通信上下文环境对应的登录用户（主要用于服务） */
	public static Map<ChannelHandlerContext, ChannelExtend> CHANNEL_USER_MAP  = new ConcurrentHashMap<>();

	/** 房间信息及相关处理*/
	public static DataManager dataManager = new DataManager();

	/** 临时连接 */
	public static Map<ChannelHandlerContext, ChannelExtend> TEMP_CONN_MAP = new ConcurrentHashMap<>();
	public static Map<String, ChannelHandlerContext> TEMP_CONN_MAP2 = new ConcurrentHashMap<>();

	/** 中心不存在的时候启用 */
	/** 操作日志 */
	public static Map<String, Packet> log = new ConcurrentHashMap<>();
	/** 缓存 后台用户 通道 */
	public static Map<String, ChannelHandlerContext> USER_CHANNEL_MAP_BACK  = new ConcurrentHashMap<>();
	/** 缓存 后台用户 请求信息 */
	public static Map<String, HttpRequest> USER_REQUEST_MAP  = new ConcurrentHashMap<>();
	
	/** 系统用户通信上下文环境（主要用于业务数据处理） */
	public static Map<String, ChannelHandlerContext> SYSTEM_CHANNEL_MAP  = new ConcurrentHashMap<>();
	public static Map<ChannelHandlerContext, String> CHANNEL_SYSTEM_MAP  = new ConcurrentHashMap<>();
}
