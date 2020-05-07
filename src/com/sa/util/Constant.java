/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.util]
 * 类名称: [Constant]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 下午3:08:16]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 下午3:08:16]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.util;

public class Constant {
	// 系统常量
	// 每次返回用户数量
	public final static int PEOPLE_NUM = 50;
	// 每次返回用户数量
	public final static int HISTORY_NUM = 100;
	// 角色
	// -------------------------------------------------------------
	public final static String ROLE_SYSTEM = "SYSTEM";		// 系统
	public final static String ROLE_TEACHER = "TEACHER";	// 教师1
	public final static String ROLE_ASSISTANT = "ASSISTANT";// 助教2
	public final static String ROLE_STUDENT = "STUDENT";	// 学生3
	public final static String ROLE_AUDIENCE = "AUDIENCE";	// 听众4
	public final static String ROLE_PARENT_TEACHER="PARENT_TEACHER";//主讲老师0
	// -------------------------------------------------------------

	// 权限
	// -------------------------------------------------------------
	public final static String AUTH_SPEAK = "SPEAK";		// 说
	// -------------------------------------------------------------

	// 输出类型
	// -------------------------------------------------------------
	public final static String CONSOLE_CODE_UNIQUELOGON = "接受单点登录";
	public final static String CONSOLE_CODE_CENTERLONGIN = "接受中心登录";
	public final static String CONSOLE_CODE_USERLOGIN = "接受用户登录";
	public final static String CONSOLE_CODE_TR = "中转接收";
	public final static String CONSOLE_CODE_TS = "中转发送";
	public final static String CONSOLE_CODE_R = "接收";
	public final static String CONSOLE_CODE_S = "发送";
	// -------------------------------------------------------------

	// 错误消息
	// -------------------------------------------------------------
	public final static String ERR_CODE_10080 = "用户权限不足";
	public final static String ERR_CODE_10081 = "用户角色不足";
	public final static String ERR_CODE_10082 = "用户不存在";
	
	public final static String ERR_CODE_10060 = "您还未购买此课程";
	
	public final static String ERR_CODE_10090 = "帐号或密码错误";
	public final static String ERR_CODE_10091 = "登录失败，您的账号在其他设备登录";
	public final static String ERR_CODE_10092 = "远程认证失败";
	public final static String ERR_CODE_10093 = "教师已经登录";
	public final static String ERR_CODE_10095 = "禁言";
	public final static String ERR_CODE_10096 = "解除禁言";
	public final static String ERR_CODE_10097 = "被迫下线";
	public final static String ERR_CODE_10098 = "被迫下线，您的账号在其他设备登录";
	public final static String ERR_CODE_10099 = "未登录";

	public final static String CONTROL_CODE_10061 = "获取用户列表失败！";
	public final static String CONTROL_CODE_GAG_10062 = "禁言失败！";
	public final static String CONTROL_CODE_GAG_RELEASE_10063 = "解除禁言失败！";
	public final static String CONTROL_CODE_KICK_10064 = "踢人！失败";
	// -------------------------------------------------------------

	// 提示消息
	// -------------------------------------------------------------
	public final static Object PROMPT_CODE_20046 = "聊天室已关闭";
	// -------------------------------------------------------------
}
