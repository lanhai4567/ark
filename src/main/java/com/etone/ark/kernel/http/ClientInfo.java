package com.etone.ark.kernel.http;

import java.util.List;

public class ClientInfo {

	private String token;
	private String name;
	
	private List<ClientEventInfo> events;
	
	public ClientInfo() {
		super();
	}

	public ClientInfo(String token, String name) {
		super();
		this.token = token;
		this.name = name;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public List<ClientEventInfo> getEvents() {
		return events;
	}

	public void setEvents(List<ClientEventInfo> events) {
		this.events = events;
	}
}
