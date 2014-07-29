package com.etone.ark.kernel.service.model;

/**
 * SID卡槽对象
 * @author 张浩
 * @version 1.3.1(2013-10-11)
 */
public class SidSlotDevice implements IDevice{

    private Integer id; //SID设备ID
    private Integer systemId;
    private Integer status;
    private Integer workStatus;
    private Integer socketChannelId;
    //设备属性
    private Integer slotNum;
    private String slotImsi;
    
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
	public Integer getSlotNum() {
		return slotNum;
	}
	public void setSlotNum(Integer slotNum) {
		this.slotNum = slotNum;
	}
	public String getSlotImsi() {
		return slotImsi;
	}
	public void setSlotImsi(String slotImsi) {
		this.slotImsi = slotImsi;
	}
	public Integer getWorkStatus() {
		return workStatus;
	}
	public void setWorkStatus(Integer workStatus) {
		this.workStatus = workStatus;
	}
}
