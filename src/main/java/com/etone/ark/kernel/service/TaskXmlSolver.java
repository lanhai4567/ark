/* TaskXmlSolver.java @date 2012-12-7
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪</p>
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.task.model.TaskStructure;
import com.etone.ark.kernel.task.model.Transition;
import com.etone.ark.kernel.task.model.Variable;

/**
 * 测试业务配置XML文件解析
 * @version 1.2
 * @author 张浩
 * 
 */
public class TaskXmlSolver {
	
	static DocumentBuilder db;
	
	static{
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析XML字符串
	 * @param xml	XML字符串
	 * @return	测试业务结构或NULL
	 */
	public static TaskStructure analyzeXml(String xml) {
		try {
			String temp = new String(xml.getBytes(),"utf-8");
			Document doc = db.parse(new ByteArrayInputStream(temp.getBytes()));
			return analyzeXml(doc);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析XML文件
	 * @param file	XML文件
	 * @return	测试业务结构或NULL
	 */
	public static TaskStructure analyzeXml(File file) {
		try {
			Document doc = db.parse(file);
			return analyzeXml(doc);
		}catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析测试业务XML配置
	 * @param doc	XML文档
	 * @return	测试业务结构
	 */
	public static TaskStructure analyzeXml(Document doc) {
		Element root = doc.getDocumentElement();
		TaskStructure bs = new TaskStructure();
		//解析测试业务编号
		bs.setCode(getAttrValue(root, "id"));
		bs.setVersion(getAttrValue(root, "version"));
		bs.setName(getAttrValue(root, "name"));
		//变量测试业务节点
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			com.etone.ark.kernel.task.model.Node buiNode;
			//如果是普通节点
			if(node.getNodeName().equals("node")){
				buiNode = analyzeNormalNode(node);
				buiNode.setType(Constants.NODE_TYPE_ACTION); //节点类型为动作节点
				//业务节点的两个属性解析到节点参数中
				buiNode.getParameter().put("resourcePrefix", getAttrValue(node, "resourcePrefix"));//资源前缀
				buiNode.getParameter().put("actionType", getAttrValue(node, "actionType"));//动作类型
				bs.getNodeGroup().put(buiNode.getId(), buiNode);
			}
			//是否是分发节点
			else if(node.getNodeName().equals("fork")){
				buiNode = analyzeNormalNode(node);
				buiNode.setType(Constants.NODE_TYPE_FORK); //节点类型为分发节点
				buiNode.getParameter().put("join", getAttrValue(node, "join"));//join节点
				bs.getNodeGroup().put(buiNode.getId(), buiNode);
			}
			//是否是聚合节点
			else if(node.getNodeName().equals("join")){
				buiNode = analyzeNormalNode(node);
				buiNode.setType(Constants.NODE_TYPE_JOIN); //节点类型为分发节点
				bs.getNodeGroup().put(buiNode.getId(), buiNode);
			}
			//是否是开始节点
			else if(node.getNodeName().equals("start")){
				buiNode = analyzeNormalNode(node);
				buiNode.setId(Constants.NODE_TYPE_START); //默认节点ID
				buiNode.setType(Constants.NODE_TYPE_START); //节点类型为开始节点
				bs.setStartNode(buiNode);
			}
			//是否是结束节点
			else if(node.getNodeName().equals("end")){
				buiNode = analyzeNormalNode(node);
				buiNode.setId(Constants.NODE_TYPE_END); //默认节点ID
				buiNode.setType(Constants.NODE_TYPE_END); //节点类型为结束节点
				bs.setEndNode(buiNode);
			}
			//是否是结果节点
			else if(node.getNodeName().equals("result")){
				bs.setResult(new ArrayList<Variable>());
				NodeList childNodeList = node.getChildNodes();
				for(int j=0;j<childNodeList.getLength();j++){
					Node child = childNodeList.item(j);
					if(child.getNodeName().equals("variable")){
						Variable variable = new Variable();
						variable.setCode(getAttrValue(child, "code"));
						variable.setName(getAttrValue(child, "name"));
						variable.setValue(getAttrValue(child, "value"));
						variable.setJavaType(getAttrValue(child, "javaType"));
						variable.setDescription(getAttrValue(child, "description"));
						bs.getResult().add(variable);
					}
				}
			}
		}
		return bs;
	}
	
	/**
	 * 解析普通节点
	 * @param node
	 * @return	
	 */
	private static com.etone.ark.kernel.task.model.Node analyzeNormalNode(Node node){
		com.etone.ark.kernel.task.model.Node buiNode = new com.etone.ark.kernel.task.model.Node();
		buiNode.setId(getAttrValue(node, com.etone.ark.kernel.task.model.Node.NODE_ID));
		buiNode.setName(getAttrValue(node, com.etone.ark.kernel.task.model.Node.NODE_NAME));
		buiNode.setType(getAttrValue(node, com.etone.ark.kernel.task.model.Node.NODE_TYPE));
		//变量节点的转向及变量赋值
		NodeList childNodeList = node.getChildNodes();
		for(int j=0;j<childNodeList.getLength();j++){
			Node child = childNodeList.item(j);
			//解析转向
			if(child.getNodeName().equals("transition")){
				Transition transition = new Transition();
				transition.setToNodeId(getAttrValue(child, Transition.NODE_TO));
				transition.setExpression(getAttrValue(child, Transition.NODE_EXPRESSION));
				buiNode.getTransitions().add(transition);
			}
			//解析节点参数
			else if(child.getNodeName().equals("param")){
				buiNode.getParameter().put(getAttrValue(child, "name"), getAttrValue(child, "value"));
			}
		}
		return buiNode;
	}

	/**
	 * 获取标签中某个属性的值
	 * @param node	标签名
	 * @param name	属性名
	 * @return	属性值
	 */
	private static String getAttrValue(Node node, String name) {
		Node attrNode = node.getAttributes().getNamedItem(name);
		if (attrNode != null) {
			return attrNode.getNodeValue();
		}
		return null;
	}

}
