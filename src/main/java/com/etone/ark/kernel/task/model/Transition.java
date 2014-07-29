/* Transition.java @date 2013-2-21
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task.model;

/**
 * 测试业务节点数据模型
 * @version 1.1
 * @author 张浩
 *
 */
public class Transition {

	//XML配置文件中的属性名称
	public static final String NODE_TO = "to";
	public static final String NODE_EXPRESSION = "expression";
	
	/**
	 * 指向流程节点
	 */
	private String toNodeId;

	/**
	 * 自定义表达式
	 */
	private String expression;
	
	//@info 方法
	
	public Transition(){}
	
	public Transition(String toNodeId){
		this.toNodeId = toNodeId;
	}
	
	//@info 属性封装 
	
	public String getToNodeId() {
		return toNodeId;
	}

	public void setToNodeId(String toNodeId) {
		this.toNodeId = toNodeId;
	}

	public String getExpression() {
		return expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
}
