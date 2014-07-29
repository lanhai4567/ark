package com.etone.ark.kernel.http;

/**
 * 客户端事件信息
 * @version 1.2
 * @author 张浩
 *
 */
public class ClientEventInfo {

	private String token;
	private String name; // 回调的事件名，参考回调事件接口
	private String callback;// 应用层提供的回调地址，例如：http://xxx/eventxx
	private String param;
	
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
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
}
