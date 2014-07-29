/* NoNodeException.java @date 2012-11-16
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.exception;

import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Transition;

/**
 * 工作流程无法找到节点错误
 * @since 0.1
 * @version 0.2
 * @author 张浩
 *
 */
@SuppressWarnings("serial")
public class NoNodeException extends Exception {
	
	private Node node;
	private Transition line;
	
	public NoNodeException(Node node,Transition line){
		this.node = node;
		this.line = line;
	}

	@Override
	public String getMessage() {
		String msg = "连接线指定的节点不存在";
		if(node != null){
			return msg + " 信息:节点id="+node.getId()+";节点name="+node.getName()+";连接线to="+line.getToNodeId();
		}
		return msg;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Transition getLine() {
		return line;
	}

	public void setLine(Transition line) {
		this.line = line;
	}

	
	
}
