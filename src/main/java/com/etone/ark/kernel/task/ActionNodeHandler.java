package com.etone.ark.kernel.task;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.ExpressionSolver;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.task.model.ActionNode;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public abstract class ActionNodeHandler<T> extends NodeHandler implements Runnable,IEventHandler<T>{
    
    static Logger logger = Logger.getLogger(ActionNodeHandler.class);
    
    protected final Task task;
    protected ActionNode node;
    protected final Resource resource;
    
    protected final ExpressionSolver.ScriptObject parameters;
    
    public ActionNodeHandler(Task task, Node node) {
        super(task,node);
        this.task = task;
        this.node = new ActionNode();
        this.node.setActionType(node.getParameter().get("actionType").toString());
        this.node.setId(node.getId());
        this.node.setResourcePrefix(node.getParameter().get("resourcePrefix").toString());
        this.node.setParameter(node.getParameter());
        this.node.setTransitions(node.getTransitions());
        this.resource = task.getResources().get(this.node.getResourcePrefix());
        this.parameters = new ExpressionSolver.ScriptObject(this.node.getId());
    }
    
    public abstract void send() throws Exception;
    
    public abstract void receice(T msg) throws Exception;

    public abstract void receiceTimeout()throws Exception;
    
    
    public void run() {
        if (task.isRunState(Task.STATUS_EXECUTE)) {
            try{
                // 将自身加入到正在执行的节点程序集合中
                task.getNodeRunnables().put(node.getId(), this);
                // 初始化节点参数
                if (this.node.getParameter() != null) {
                	//将全局变量加入到节点参数中
                	this.node.getParameter().remove(this.task.getParameters());//???????
                    for (String name : this.node.getParameter().keySet()) {
                        Object value = this.node.getParameter().get(name);
                        String expression = ExpressionSolver.getExpression(value.toString());
                        if(expression != null){
                        	try{
                        		Object obj = task.script.get(expression);
                        		if(obj != null && !StringUtils.isBlank(obj.toString())){
                        			this.parameters.put(name, obj);
                        		}
                        	}catch(ScriptException e){
                        		logger.warn("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][JavaScriptError]未找到"+name+"参数");
                        	}
                        }else{
                        	this.parameters.put(name, value);
                        }
                        /*if (ExpressionSolver.isExpression(value.toString())) {
                            Object obj =  ExpressionSolver.eval(value.toString(), this.task.getHistory());
                            if(obj != null){
                                this.parameters.put(name, obj);
                            }
                        } else {
                            this.parameters.put(name, value);
                        }*/
                    }
                }
                //task.getHistory().put(parameters); 
                // 将节点参数加入到测试业务参数中
                task.script.putIn(node.getId(), this.parameters.getBingdings());
                send();
            }catch (Exception e) {
                logger.error("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][error]", e);
                TaskManager.InterruptTask(task,Constants.ACTION_ERROR,"微内核错误");
            } finally {
                task.getNodeRunnables().remove(node.getId());
            }
        }
    }

    public void execute(T param) {
        if (task.isRunState(Task.STATUS_EXECUTE)) {
            try{
                // 将自身加入到正在执行的节点程序集合中
                task.getNodeRunnables().put(node.getId(), this);
                receice(param);
            }catch (Exception e) {
                logger.error("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][error]", e);
                TaskManager.InterruptTask(task,Constants.ACTION_ERROR,"微内核错误");
            } finally {
                task.getNodeRunnables().remove(node.getId());
            }
        }
    }

    public void timeout() {
        if (task.isRunState(Task.STATUS_EXECUTE)) {
            try{
                // 将自身加入到正在执行的节点程序集合中
                task.getNodeRunnables().put(node.getId(), this);
                receiceTimeout();
            }catch (Exception e) {
                logger.error("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][error]", e);
                TaskManager.InterruptTask(task,Constants.ACTION_ERROR,"微内核错误");
            } finally {
                task.getNodeRunnables().remove(node.getId());
            }
        }
    }
    
    protected void setIfTaskResult(String result,String errorDescription) {
    	//不处理彩铃和通知音两个业务的语音呼叫
    	if(!Constants.ACTION_SUCCESS.equals(result) && 
    			(task.getType().equals("rbt") || task.getType().equals("notifyVoice")) && 
    			node.getActionType().equals("voiceCall")){
    		return;
    	}
        // 检查网络登记动作的结果不计算到测试项结果中
        if (!node.getActionType().equals("checkRegister") && !node.getActionType().equals("pesq")) {
            if (StringUtils.isNotEmpty(result)) {
            	if("ENCOUNTER_VERIFICATION".equals(task.getResultCode())){
            		 task.setResultCode(result);
                     task.setErrorDescription(errorDescription);
            	}else if (Constants.ACTION_SUCCESS.equals(task.getResultCode()) && !Constants.ACTION_SUCCESS.equals(result)) {
                    task.setResultCode(result);
                    task.setErrorDescription(errorDescription);
                }
            }
        }
    }

    public ActionNode getNode() {
        return node;
    }
    
}
