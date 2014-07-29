package com.etone.ark.kernel.task.model;

public class ActionNode extends Node{
	
	//XML配置文件中的属性名称
	public static final String NODE_ACTION_TYPE = "actionType";
	public static final String NODE_RESORCE_PREFIX = "resourcePrefix";

	private String actionType;
	private String resourcePrefix;
	
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getResourcePrefix() {
		return resourcePrefix;
	}
	public void setResourcePrefix(String resourcePrefix) {
		this.resourcePrefix = resourcePrefix;
	}
}
