/* NodeExecuteException.java @date 2013-1-8
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.exception;

import com.etone.ark.kernel.task.model.Node;

/**
 * 节点执行错误
 * @version 0.1
 * @author 张浩
 *
 */
public class NodeExecuteException extends Exception{

	private static final long serialVersionUID = -5167120622576206154L;
	
	private Node node;
	
	public NodeExecuteException(Node node){
		this.node = node;
	}

	@Override
	public String getMessage() {
		String msg = "节点执行错误";
		if(node != null){
			return msg + " 信息:节点id="+node.getId()+";节点name="+node.getName()+";";
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
