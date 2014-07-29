/* NodeStartRunnable.java @date 2012-12-9
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.etone.ark.communication.solver.EventController;
import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.ExpressionSolver.ScriptObject;
import com.etone.ark.kernel.service.JavaScriptEngine;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

/**
 * 测试业务开始节点执行程序
 * @version 1.1
 * @author 张浩
 *
 */
public class NodeStartHandler extends NodeHandler {
	
	static Logger logger = Logger.getLogger(NodeStartHandler.class);
	
	public NodeStartHandler(Task task,Node node) {
	    super(task,node);
	}

	@Override
	public void run() {
		//只有在挂起状态时开始节点才执行
		if(task.isRunState(Task.STATUS_HANG)){
			try{
			    boolean isRun = true;
			    final NodeStartHandler handler = this;
		        for(String key : task.getResources().keySet()){
		            Resource resource = task.getResources().get(key);
		            Resource temp1 = DeviceManager.findResourceByRtdModule(resource.getRtdSerialNum(), resource.getRtdModuleNum());
		            if(temp1 != null && Resource.INTERRUPT == temp1.getBindStatus()){
		            	String eventName = "unbind_" + temp1.getImsi();
		            	if(EventController.addListen(eventName, new IEventHandler<Object>(){
                            @Override
                            public void execute(Object obj) {
                                handler.run();
                            }

                            @Override
                            public void timeout() {
                                handler.run();
                            }
                        },300L)){
                            isRun = false;
                        }
		            }
		            if(isRun && resource.getSidSerialNum() != null && resource.getSidSlotNum() != null){
		            	 Resource temp2 = DeviceManager.findResourceBySidSlot(resource.getSidSerialNum(), resource.getSidSlotNum());
		            	 if(temp2 != null && Resource.INTERRUPT == temp2.getBindStatus()){
		            		 String eventName = "unbind_" + temp1.getImsi();
				            	if(EventController.addListen(eventName, new IEventHandler<Object>(){
		                            @Override
		                            public void execute(Object obj) {
		                                handler.run();
		                            }

		                            @Override
		                            public void timeout() {
		                                handler.run();
		                            }
		                        },300L)){
		                            isRun = false;
		                        }
		            	 }
		            }
		        }
		        if(isRun && task.tryRun()){
    				//设置测试业务开始执行时间
    				task.setStartDate(new Date(System.currentTimeMillis()));
    				//将自身加入到正在执行的节点程序集合中
    				task.getNodeRunnables().put(node.getId(),this);
    				//将参数加入JS脚本中//====新脚本引擎=====//
    				task.script = JavaScriptEngine.getScriptRegion();
    				for(String key : task.getParameters().keySet()){
    					Object value = task.getParameters().get(key);
    					task.script.put(key, value);
    				}
    				StringBuffer loggerResourceInfo = new StringBuffer();
    				//TODO 将资源加入到脚本中，可以优化
    				for(String key : task.getResources().keySet()){
    					Resource resource = task.getResources().get(key);
    					ScriptObject object = new ScriptObject(key);
    					object.put("testNum", resource.getTestNum());
    					object.put("imsi", resource.getImsi());
    					object.put("rtdSerialNum",resource.getRtdSerialNum());
    					object.put("rtdModuleNum",resource.getRtdModuleNum());
    					object.put("netType",resource.getNetType());
    					object.put("cardType",resource.getCardType());
    					if(resource.getSidSlot() != null){
    						object.put("sidSerialNum",resource.getSidSerialNum());
    						object.put("sidSlotNum", resource.getSidSlotNum());
    					}
    					//task.getHistory().put(object);
    					//task.getHistory().put(key + "close",false); //不关机
    					//====新脚本引擎=====//
    					task.script.put(key, object.getBingdings());
    					task.script.put(key + "close",false);
    					//用于日志输出
    					loggerResourceInfo.append(",{"+key+":"+object.toString()+"}");
    				}
    				logger.info("[TASK]["+task.getId()+"]["+task.getStructure().getCode()+"][taskParam]"+task.getParameters().toString()+"[resourceParam]"+loggerResourceInfo.substring(1));
    				//将运行参数加入到脚本中
    				//task.getHistory().put(task.getParameters());
    				//task.getHistory().put("taskType", task.getType()); //业务类型
    				task.script.put("taskType",task.getType());
    				//创建节点变量到脚本中
    				for(Node node:task.getStructure().getNodeGroup().values()){
    					//task.getHistory().put(new ScriptObject(node.getId()));
    					//====新脚本引擎=====//
    					task.script.put(node.getId(),new HashMap<String,Object>());
    				}
    				
    				//执行下一个节点
    				getNextNodeHandler().run();
		        }
			}catch(Exception e){
				logger.error("[TASK]["+task.getId()+"]["+node.getId()+"][error]",e);
				TaskManager.InterruptTask(task,Constants.ACTION_ERROR,"微内核错误");
			} finally{
				//移除自身在正在执行的节点程序集合的缓存
				task.getNodeRunnables().remove(this);
			}
		}
	}
}

