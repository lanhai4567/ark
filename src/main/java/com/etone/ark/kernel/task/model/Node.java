/* Node.java @date 2012-12-7
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试业务节点数据模型
 * @version 1.1
 * @author 张浩
 *
 */
public class Node{
	
	//XML配置文件中的属性名称
	public static final String NODE_ID = "id";
	public static final String NODE_NAME = "name";
	public static final String NODE_TYPE = "type";
	
	/**
	 * <p>流程节点ID</p>
	 * 在一个标准流程中流程节点ID是唯一的
	 */
	protected String id;
	/**
	 * 节点名称
	 */
	protected String name;
	/**
	 * 节点类型
	 */
	protected String type;
	/**
	 * 连接线列表
	 */
	protected List<Transition> transitions;
	/**
	 * 参数列表
	 */
	protected Map<String,Object> parameter;
	
	//@info 方法
	
	public Node(){
		this.transitions = new ArrayList<Transition>();
		this.parameter = new HashMap<String,Object>();
	}
	
	//@info 属性封装 
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Transition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	public Map<String, Object> getParameter() {
		return parameter;
	}

	public void setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
