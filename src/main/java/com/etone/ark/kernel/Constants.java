/* Constants.java	@date 2012-12-19
 * @JDK 1.6
 * @encoding UTF-8
 * <p>版权所有：宜通世纪</p>
 * <p>未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel;


/**
 * 微内核全局的常量 </p>
 * 2013-4-2修正设备状态常量，参考文档：统一平台-拨测管理系统与拨测终端设备接口技术要求V1.7 </p>
 * @version 1.2
 * @author 张浩
 *
 */
public class Constants {
	
	/**
	 * 省平台系统ID
	 */
	public final static Integer SYSTEM_ID = Integer.parseInt(PropertiesUtil.getValue("systemId").trim());
	
	/**
	 * 通讯中心系统ID
	 */
	public final static Integer REMOTE_SYSTEM_ID = Integer.parseInt(PropertiesUtil.getValue("remoteSystemId").trim());
	
	public static final String H2_TCP_PORT = PropertiesUtil.getValue("h2TcpPort");
	
	public static final String H2_WEB_PORT = PropertiesUtil.getValue("h2WebPort");
	
	/**
     * 设备心跳周期
     */
    public static final Integer DEVICE_HEART_BEAT_PERIOD = Integer.valueOf(PropertiesUtil.getValue("deviceHeartBeatPeriod").trim());
	/**
	 * 设备超时时间
	 */
	public static final Integer DEVICE_HEART_BEAT_TIMEOUT = Integer.parseInt(PropertiesUtil.getValue("deviceHeartBeatTimeout").trim());
	
	/**
     * 客户端超时时间
     */
    public static final Integer CLIENT_HEART_BEAT_TIMEOUT = Integer.parseInt(PropertiesUtil.getValue("clientHeartBeatTimeout").trim());
    
    public static final String DIGEST_KEY = PropertiesUtil.getValue("digestKey");
    
	/**
	 * 微内核线程池个数
	 */
	public static final int KERNEL_MAX_THREAD_SIZE = Integer.parseInt(PropertiesUtil.getValue("kernelMaxTreadSize").trim());

	public static final int FTP_SERVER_PORT = Integer.valueOf(PropertiesUtil.getValue("ftpserverport").trim());

	/**
	 * FTP路径
	 */
	public static final String FTP_PATH = PropertiesUtil.getValue("ftpPath");
	
	public static final String BYTE_PROTOCOL_VERSION = "0.1";
	
	public static final String JSON_PROTOCOL_VERSION = "1.0";

	//资源参数前缀
	public static final String RESOURCES [] = new String [] {"resourceA_","resourceB_","resourceC_","resourceD_","resourceE_"};
	
	//==========================================设备相关============================================//
	
	//卡类型
	/**
	 * 本地卡，使用RTD自带的IMSI
	 */
	public final static Integer CARD_LOCAL = 0;
	
	/**
	 * 远程卡，使用SID自带的IMSI
	 */
	public final static Integer CARD_REMOTE = 1;

	/**
	 * 设备心跳超时，微内核在指定时间内未收到设备心跳信息
	 */
	public final static int DEVICE_STATUS_DISCONNECT = 10;
	/**
	 * 设备升级，微内核接收到设备升级请求时修改设备状态
	 */
	public final static int DEVICE_STATUS_UPDATE = 11;
	
	//RTD状态
	/**
	 * 0：RTD设备正常（自动上报）
	 */
	public static final int RTD_STATUS_NORMAL = 0;
	/**
	 * 1：RTD设备故障（自动上报）
	 */
	public static final int RTD_STATUS_FAULT = 1;
	
	//RTD模块状态
	/**
	 * 0：模块正常(自动上报)
	 */
	public static final int RTD_MODULE_STATUS_NORMAL = 0;
	/**
	 * 1：模块故障(自动上报)
	 */
	public static final int RTD_MODULE_STATUS_FAULT = 1;
	/**
	 * 2：模块无法连接，微内核无法PING通RTD设备的IP
	 */
	public static final int RTD_MODULE_STATUS_LINK_ERROR = 2;
	/**
	 * 3: 模块自检(自动上报)
	 */
	public static final int RTD_MODULE_STATUS_SELF_CHECK = 3;

	
	//SID状态
	/**
	 * 0：SID设备正常（自动上报）
	 */
	public static final int SID_STATUS_NORMAL = 0;
	/**
	 * 1：SID设备故障（自动上报）
	 */
	public static final int SID_STATUS_FAULT = 1;
	
	//SID卡槽状态
	/**
	 * 0：槽位正常（自动上报）
	 */
	public static final int SID_SLOT_STATUS_NORMAL = 0;
	/**
	 * 1：没插卡（自动上报）
	 */
	public static final int SID_SLOT_STATUS_NO_CARD = 1;
	/**
	 * 2：卡槽故障（自动上报）
	 */
	public static final int SID_SLOT_STATUS_FAULT = 2;
	/**
	 * 3：槽位链路故障，微内核无法PING通SID设备的IP
	 */
	public static final int SID_SLOT_STATUS_LINK_ERROR = 3;
	/**
	 * 4：槽位自检（自动上报）
	 */
	public static final int SID_SLOT_STATUS_SELF_CHECK = 4;
	
	
	//=============================================动作和测试业务相关=======================================// 
	
	/**
	 * 动作执行成功
	 */
	public static final String ACTION_SUCCESS = "SUCCESS";
	/**
	 * 动作执行错误
	 */
	public static final String ACTION_ERROR = "ER_KERNEL_INTERNAL_ERROR";
	/**
	 * 动作执行超时
	 */
	public final static String ACTION_TIMEOUT = "ER_KERNEL_ACTION_TIMEOUT";
	/**
	 * 测试项中断标志
	 */
	public final static String TASK_INTERRUPT = "ER_MC_BUSINESS_INTERRUPT";
	
	//流程节点类型
	
	/**
	 * 开始节点类型
	 */
	public final static String NODE_TYPE_START = "start";
	
	/**
	 * 业务节点类型
	 */
	public final static String NODE_TYPE_ACTION = "action";
	
	/**
	 * 分支节点类型
	 */
	public final static String NODE_TYPE_FORK = "fork";
	
	/**
	 * 聚合节点类型
	 */
	public final static String NODE_TYPE_JOIN = "join";
	
	/**
	 * 结束节点类型
	 */
	public final static String NODE_TYPE_END = "end";
	
	
	//======================= before of version 1.1 ========================//

	
	//微内核响应设备通信的状态，在设备登记状态，设备释放结果等地方使用
	/**
	 * 成功状态 
	 */
	public static final String RESOURCE_RESPONSE_SUCCESS = "1";
	/**
	 * 失败状态
	 */
	public static final String RESOURCE_RESPONSE_FAIL = "0";   
	
	
	
	//=========================================回调事件相关===============================================//
	
	//异常回调类型
	
	/**
	 * 设备登记异常
	 */
	public final static String EXCEPTION_DEVICE_LOGIN_FAIL = "DEVICE_LOGIN_FAIL";
	/**
	 * 微内核初始化异常
	 */
	public final static String EXCEPTION_KERNEL_INITIAL = "KERNEL_INITIAL";
	/**
	 * 微内核回调功能异常
	 */
	public final static String EXCEPTION_EVENT_CALLBACK_FAIL = "EVENT_CALLBACK_FAIL";
	/**
	 * 微内核内部错误
	 */
	public final static String EXCEPTION_INTERNAL_ERROR = "EVENT_INTERNAL_ERROR";
	
	//========================================资源调度返回码===============================================//
	
	/**
	 * 统一的成功返回码
	 */
	public final static Integer SUCCESS = 0;
	
	/**
	 * 统一的参数错误返回码
	 */
	public final static Integer PARAMETER_ERROR = 1;
	
	/**
	 * SOCKET通信通道错误
	 */
	public final static Integer SOCKET_CHANNEL_ERROR = 2;
	
	//返回码组成规则，从左到右，每位代表的意义：
	//百位	1：代表资源
	//		2：代表测试项
	//百位十位	11：代表RTD
	//			12: 代表SID
	
	/**
	 * RTD资源未找到
	 */
	public final static Integer RTD_NOT_FIND = 110;
	
	/**
	 * RTD模块资源未找到
	 */
	public final static Integer RTD_MODULE_NOT_FIND = 111;
	
	/**
	 * RTD模块状态不正常
	 */
	public final static Integer RTD_MODULE_ERROR = 112;
	
	/**
	 * RTD模块资源忙碌
	 */
	public final static Integer RTD_MODULE_BUSY = 113;
	
	/**
	 * SID资源未找到
	 */
	public final static Integer SID_NOT_FIND = 120;
	
	/**
	 * SID卡槽资源未找到
	 */
	public final static Integer SID_SLOT_NOT_FIND = 121;
	
	/**
	 * SID卡槽状态不正常
	 */
	public final static Integer SID_SLOT_ERROR = 122;
	
	/**
	 * SID卡槽资源忙碌
	 */
	public final static Integer SID_SLOT_BUSY = 123;
	
	/**
	 * IMSI无效
	 */
	public final static Integer IMSI_ERROR = 130;
	
	
	/*****************ANDROID相关******************/
	
	public static final String RTD_TYPE_ANDROID="Android";
	public static final String RTD_TYPE_NORMAL="Normal";
}
