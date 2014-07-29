package com.etone.ark.kernel.task;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.Protocol;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.exception.NoLineException;
import com.etone.ark.kernel.exception.NoNodeException;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.ExpressionSolver;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;
import com.etone.ark.kernel.task.model.Transition;

/**
 * 功能描述：节点执行的抽象对象
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 * 
 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
 * @version 1.0.0
 * @since 1.0.0 
 * create on: 2013-6-4 
 */
public abstract class NodeHandler implements Runnable{
    
    protected Task task;
    protected Node node;
    
    public NodeHandler(Task task, Node node) {
        this.task = task;
        this.node = node;
    }
    
    /**
     * 功能描述：根据节点ID获取节点的处理对象
     * 
     * @param id    节点ID
     * @return 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-6-4
     */
    public NodeHandler getNodeHandler(String id){
        NodeHandler handler = null;
        Runnable runnable = null;
        if((runnable = task.getNodeRunnables().get(id))!= null){
            //TODO 可以修改task对象的存储类型避免转换
            handler = NodeHandler.class.cast(runnable);
        }
        if(handler == null){
            Node node = task.getStructure().getNodeGroup().get(id);
            if(node != null){
                //动作节点包装
                if(Constants.NODE_TYPE_ACTION.equals(node.getType())){
                    handler = getActionNodeHandler(node);
                }
                //分支流程节点包装
                else if(Constants.NODE_TYPE_FORK.equals(node.getType())){
                    handler = new NodeForkHandler(task,node);
                }
            }else if(task.getStructure().getEndNode().getId().equals(id)){
                handler = new NodeEndRunnable(task,task.getStructure().getEndNode());
            }
            
        }
        return handler;
    }
    
    /**
     * 功能描述：获取动作节点的处理对象
     * 
     * @param node
     * @return 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-6-4
     */
    public NodeHandler getActionNodeHandler(Node node){
        //TODO 修改返回值为:ActionNodeHandler<?>
        Resource resource = task.getResources().get(node.getParameter().get("resourcePrefix").toString());
        String actionType = node.getParameter().get("actionType").toString();
        if("pesq".equals(actionType)){
            return new JsonPesqActionNodeRunnable(task,node);
        }else if(Protocol.TYPE_JSON.equals(resource.getChannel().getType())){
            return new JsonActionNodeHandler(task,node); 
        }else if(Protocol.TYPE_BYTE.equals(resource.getChannel().getType())){
            if("powerOn".equals(actionType)){
                return new BytePowerOnActionNodeHandler(task,node);
            }else if("voiceCall".equals(actionType)){
                return new ByteVoiceCallActionNodeHandler(task,node);
            }else if("waitVoiceCallin".equals(actionType)){
                return new ByteWaitVoiceCallinActionNodeHandler(task,node);
            }else if("sendSMS".equals(actionType)){
                return new ByteSendSmsActionNodeHandler(task,node);
            }else if("receiveSMS".equals(actionType)){
                return new ByteReceiveSmsActionNodeHandler(task,node);
            }else{
                return new ByteActionNodeHandler(task,node);
            }
        }
        return null;
    }
    
    /**
     * 功能描述：获取下一个节点的处理对象
     * 
     * @return
     * @throws NoNodeException
     * @throws NoLineException 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @throws ScriptException 
     * @since 1.0.0
     * create on: 2013-6-4
     */
    public NodeHandler getNextNodeHandler() throws NoNodeException, NoLineException, ScriptException{
        Transition transition = null;
        //查找可用转向
        for(int i=0;i<node.getTransitions().size();i++){
            Transition temp = node.getTransitions().get(i);
            //是否是带自定义表达式的连接线
            if(StringUtils.isNotEmpty(temp.getExpression())){
            	String expression = ExpressionSolver.getExpression(temp.getExpression());
            	if(expression != null){
	            	Object obj = task.script.evel(expression);
	                //Object obj = ExpressionSolver.eval(temp.getExpression(),task.getHistory());
	                if(obj != null && Boolean.parseBoolean(obj.toString())){
	                    transition = temp;
	                    break;
	                }
            	}
            }else{
                transition = temp;
                break;
            }
        }
        
        if(transition != null){
            NodeHandler handler = getNodeHandler(transition.getToNodeId());
            if(handler != null){
                return handler;
            }else{
                throw new NoNodeException(node,transition);
            }
        }else{
            throw new NoLineException(node);
        }
    }

    public Task getTask() {
        return task;
    }

    public Node getNode() {
        return node;
    }    
    
}
