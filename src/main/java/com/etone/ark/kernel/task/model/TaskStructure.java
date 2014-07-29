/* BusinessStructure.java @date 2012-12-7
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>测试业务结构数据模型</p>
 * @version 1.1
 * @author 张浩
 *
 */
public class TaskStructure{
	
	/**
	 * 测试业务编号
	 */
	private String code;
	
	/**
	 * 测试业务版本号
	 */
	private String version;
	
	/**
	 * 测试业务名称
	 */
	private String name;
	
	/**
	 * 测试业务开始节点
	 */
	private Node startNode;
	
	/**
	 * 测试业务节点组
	 */
	private Map<String,Node> nodeGroup;
	
	/**
	 * 测试业务结束节点
	 */
	private Node endNode;
	
	/**
	 * 测试业务结果描述
	 */
	private List<Variable> result;
	
	/**
	 * 配置内容信息
	 */
	private String xml;
	
	//方法
	
	public TaskStructure(){
		//创建对象时就初始化，虽然增加了创建对象的成本，当在运行时不用进行非空判断
		this.nodeGroup = new LinkedHashMap<String,Node>();
	}
	
	//属性封装

	public Node getStartNode() {
		return startNode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Map<String, Node> getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(Map<String, Node> nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	
	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Variable> getResult() {
		return result;
	}

	public void setResult(List<Variable> result) {
		this.result = result;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
}
