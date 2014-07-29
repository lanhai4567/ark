/* NoLineException.java @date 2012-11-16
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.exception;

import com.etone.ark.kernel.task.model.Node;

/**
 * 工作流程无法找到可用的连接线错误
 * @since 0.1
 * @version 0.2
 * @author 张浩
 *
 */
@SuppressWarnings("serial")
public class NoLineException extends Exception {
	
	private Node node;
	
	public NoLineException(Node node){
		this.node = node;
	}

	@Override
	public String getMessage() {
		String msg = "节点没有可用的连接线";
		if(node != null){
			return msg + " 信息:节点id="+node.getId()+";节点name="+node.getName()+";lineSize:"+node.getTransitions().size();
		}
		return msg;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}
