package com.etone.ark.kernel.service.model;

/**
 * 设备模块对象
 * @author 张浩
 * @version 1.3.1(2013-10-11)
 */
public class RtdModuleDevice implements IDevice{

    private Integer id; //RTD设备ID
    private Integer systemId;
    private Integer status;
    private Integer workStatus;
    private Integer socketChannelId;
    //设备属性
    private Integer moduleNum;
    private String moduleType;
    //NORMAL:表示普通RTD,ANDROID:表示Anroid设备
    private String businessType ;
    private String netType; //模块网络制式
    private String moduleImsi;
    
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
	public Integer getModuleNum() {
		return moduleNum;
	}
	public void setModuleNum(Integer moduleNum) {
		this.moduleNum = moduleNum;
	}
	public String getModuleType() {
		return moduleType;
	}
	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}
	public String getNetType() {
		return netType;
	}
	public void setNetType(String netType) {
		this.netType = netType;
	}
	public String getModuleImsi() {
		return moduleImsi;
	}
	public void setModuleImsi(String moduleImsi) {
		this.moduleImsi = moduleImsi;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getWorkStatus() {
		return workStatus;
	}
	public void setWorkStatus(Integer workStatus) {
		this.workStatus = workStatus;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
}
