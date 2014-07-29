package com.etone.ark.kernel.http;

import com.google.gson.annotations.Expose;

/**
 * RTD模块网络信息
 * @version 0.1
 * @author 张浩
 *
 */
public class EventRtdModuleNetInfo {

    @Expose
	private String rtdSerialNum; // RTD设备序列号，唯一标识
    @Expose
	private String moduleSerialNum; // 模块序列号，模块唯一标识
    @Expose
	private String netType; // 模块网络制式
    @Expose
    private String moduleType;//模块型号
    @Expose
    private String imsi;
    @Expose
	private String netInfos; // 模块网络信息,JSON字符串
    
	public String getRtdSerialNum() {
		return rtdSerialNum;
	}
	public void setRtdSerialNum(String rtdSerialNum) {
		this.rtdSerialNum = rtdSerialNum;
	}
	public String getModuleSerialNum() {
		return moduleSerialNum;
	}
	public void setModuleSerialNum(String moduleSerialNum) {
		this.moduleSerialNum = moduleSerialNum;
	}
	public String getNetType() {
		return netType;
	}
	public void setNetType(String netType) {
		this.netType = netType;
	}
	public String getImsi() {
        return imsi;
    }
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }
    public String getNetInfos() {
        return netInfos;
    }
    public void setNetInfos(String netInfos) {
        this.netInfos = netInfos;
    }
    public String getModuleType() {
		return moduleType;
	}
	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}
	
	
}
