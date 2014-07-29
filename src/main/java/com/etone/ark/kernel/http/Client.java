/* Client.java	@date 2012-12-19
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.etone.ark.communication.http.HttpChannel;

/**
 * 客户端对象
 * @version 1.2
 * @author 张浩
 *
 */
public class Client {

	/**
	 * 客户端标识
	 */
	private String token;
	
	/**
	 * 客户端名称
	 */
	private String name;
	
	/**
	 * 客户端连接时间
	 */
	private Date connectTime;
	
	/**
	 * 最后一次的心跳时间
	 */
	private Date heartBeatTime;
	
	/**
	 * 回调服务路径
	 */
	private Map<String,HttpChannel> events;
	
	/**
	 * <p>客户端状态</p>
	 */
	volatile Integer statua;

	public Client(){
		events = new HashMap<String,HttpChannel>();
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getConnectTime() {
		return connectTime;
	}

	public void setConnectTime(Date connectTime) {
		this.connectTime = connectTime;
	}

	public Date getHeartBeatTime() {
		return heartBeatTime;
	}

	public void setHeartBeatTime(Date heartBeatTime) {
		this.heartBeatTime = heartBeatTime;
	}
	
	public Map<String, HttpChannel> getEvents() {
		return events;
	}

	public void setEvents(Map<String, HttpChannel> events) {
		this.events = events;
	}

	public Integer getStatua() {
		return statua;
	}

	public void setStatua(Integer statua) {
		this.statua = statua;
	}
}
