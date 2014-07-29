package com.etone.ark.kernel.service.model;

import java.util.Date;

/**
 * RTD设备对象
 * @author 张浩
 * @version 1.3.1(2013-10-11)
 */
public class RtdDevice implements IDevice{

    private Integer id;
    private Integer systemId;
    private Integer status;
    private Integer workStatus;
    private Integer socketChannelId;
    //设备属性
    private Integer serialNum;// 设备序列号
    private String deviceIp;// IP地址
    private String model;// 型号
    private String softVersion;// 软件版本
    private String vendor; //厂商
    private Integer moduleCount;// 设备模块数
    
    private Date linkTime;
    private Date heartBeatTime;
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getSocketChannelId() {
        return socketChannelId;
    }
    public void setSocketChannelId(Integer socketChannelId) {
        this.socketChannelId = socketChannelId;
    }
    public Integer getSystemId() {
        return systemId;
    }
    public void setSystemId(Integer systemId) {
        this.systemId = systemId;
    }
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getSerialNum() {
		return serialNum;
	}
	public void setSerialNum(Integer serialNum) {
		this.serialNum = serialNum;
	}
	public String getDeviceIp() {
		return deviceIp;
	}
	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getSoftVersion() {
		return softVersion;
	}
	public void setSoftVersion(String softVersion) {
		this.softVersion = softVersion;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public Integer getModuleCount() {
		return moduleCount;
	}
	public void setModuleCount(Integer moduleCount) {
		this.moduleCount = moduleCount;
	}
	public Integer getWorkStatus() {
		return workStatus;
	}
	public void setWorkStatus(Integer workStatus) {
		this.workStatus = workStatus;
	}
	public Date getLinkTime() {
		return linkTime;
	}
	public void setLinkTime(Date linkTime) {
		this.linkTime = linkTime;
	}
	public Date getHeartBeatTime() {
		return heartBeatTime;
	}
	public void setHeartBeatTime(Date heartBeatTime) {
		this.heartBeatTime = heartBeatTime;
	}
}
