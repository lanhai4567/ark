package com.etone.ark.kernel.service.model;

/**
 * 设备对象接口
 * @author 张浩
 * @version 1.3.1(2013-10-11)
 */
public interface IDevice {
    
    public Integer getId();
    public Integer getSystemId();
    public Integer getStatus();
    public Integer getWorkStatus();
    public Integer getSocketChannelId();
}
