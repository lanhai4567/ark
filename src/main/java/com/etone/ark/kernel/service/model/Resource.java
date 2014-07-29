package com.etone.ark.kernel.service.model;

import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.service.DeviceManager;

/**
 * 资源对象
 * @author 张浩
 * @version 1.3.1(2013-10-11)
 */
public class Resource {
	
	public static final int UNBIND = 0; // 未绑定
	public static final int BINDING = 1; // 已绑定
	public static final int INTERRUPT = 2; // 中断绑定

	/**
	 * RTD设备序列号
	 */
	private Integer rtdSerialNum;
	/**
	 * RTD模块号
	 */
	private Integer rtdModuleNum;
	/**
	 * SID设备序列号
	 */
	private Integer sidSerialNum;
	/**
	 * SID卡槽号
	 */
	private Integer sidSlotNum;
	/**
	 * 卡的唯一标识
	 */
	private String imsi;
	/**
	 * <p>测试号</p>
	 * 该号码可以使MSISDN号码也可以使短号
	 */
	private String testNum;
	
	/**
	 * 卡类型
	 * <ul>
	 * 	<li>0：本地卡</li>
	 *  <li>1：远程卡</li>
	 * </ul>
	 */
	private Integer cardType;
	
	private String netType;
	
	private Integer systemId;
	
	private Integer workStatus; //工作状态
	
	private Integer bindStatus; //绑定状态

	public Integer getRtdSerialNum() {
		return rtdSerialNum;
	}

	public void setRtdSerialNum(Integer rtdSerialNum) {
		this.rtdSerialNum = rtdSerialNum;
	}

	public Integer getRtdModuleNum() {
		return rtdModuleNum;
	}

	public void setRtdModuleNum(Integer rtdModuleNum) {
		this.rtdModuleNum = rtdModuleNum;
	}

	public Integer getSidSerialNum() {
		return sidSerialNum;
	}

	public void setSidSerialNum(Integer sidSerialNum) {
		this.sidSerialNum = sidSerialNum;
	}

	public Integer getSidSlotNum() {
		return sidSlotNum;
	}

	public void setSidSlotNum(Integer sidSlotNum) {
		this.sidSlotNum = sidSlotNum;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getTestNum() {
		return testNum;
	}

	public void setTestNum(String testNum) {
		this.testNum = testNum;
	}

	public String getNetType() {
		return netType;
	}

	public void setNetType(String netType) {
		this.netType = netType;
	}
	public Integer getWorkStatus() {
		return workStatus;
	}

	public void setWorkStatus(Integer workStatus) {
		this.workStatus = workStatus;
	}

	public Integer getBindStatus() {
		return bindStatus;
	}

	public void setBindStatus(Integer bindStatus) {
		this.bindStatus = bindStatus;
	}

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}
	
	public Integer getCardType() {
		return cardType;
	}

	public void setCardType(Integer cardType) {
		this.cardType = cardType;
	}

	public RtdModuleDevice getRtdModule(){
		return DeviceManager.findRtdModuleDevice(rtdSerialNum, systemId, rtdModuleNum);
	}
	
	public SidSlotDevice getSidSlot(){
		return DeviceManager.findSidSlotDevice(imsi);
	}
	
	public SocketChannel getChannel(){
		RtdModuleDevice rtdModule = getRtdModule();
		if(rtdModule != null){
			return DeviceManager.getSocketChannel(rtdModule.getSocketChannelId());
		}
		return null;
	}
}
