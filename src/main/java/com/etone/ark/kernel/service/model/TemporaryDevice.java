package com.etone.ark.kernel.service.model;

/**
 * 临时设备登陆，临时设备用于转发报文
 * @author 张浩
 * @version 1.3.1(2013-10-11)
 */
public class TemporaryDevice implements IDevice{

    private Integer id;
    private Integer systemId;
    private Integer index;
    private Integer workStatus;
    private Integer status;
    private Integer socketChannelId;
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
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
	public Integer getWorkStatus() {
		return workStatus;
	}
	public void setWorkStatus(Integer workStatus) {
		this.workStatus = workStatus;
	}
}
