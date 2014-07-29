/* NodeForkRunnable.java @date 2012-12-9
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.ContainerService;
import com.etone.ark.kernel.service.ExpressionSolver;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;
import com.etone.ark.kernel.task.model.Transition;

/**
 * 分支节点执行程序
 * @version 1.1
 * @author 张浩
 *
 */
public class NodeForkHandler extends NodeHandler {
	
	static Logger logger = Logger.getLogger(NodeForkHandler.class);

	public NodeForkHandler(Task task,Node node) {
	    super(task,node);
	}
	
	@Override
	public void run() {
		//只有在运行状态时
		if(task.isRunState(Task.STATUS_EXECUTE)){
			try{
				//将自身加入到正在执行的节点程序集合中
				task.getNodeRunnables().put(node.getId(),this);
				Object joinId = node.getParameter().get("join");
				if(joinId != null){
					Node joinNode = task.getStructure().getNodeGroup().get(node.getParameter().get("join"));
					//设置同步计数器
					AtomicInteger forkCount = new AtomicInteger(0);
					NodeJoinHandler jnRun = new NodeJoinHandler(task,joinNode,forkCount);
					task.getNodeRunnables().put(joinNode.getId(), jnRun);
					boolean isFork = false;
					//遍历所有连接线，计算同步支线个数
					for(int i=0;i<node.getTransitions().size();i++){
						Transition line = node.getTransitions().get(i);
						//判断是否是JS表达式
						if(StringUtils.isNotBlank(line.getExpression())){
							//判断是否是JS表达式
							String expression = ExpressionSolver.getExpression(line.getExpression());
							if(StringUtils.isNotBlank(expression)){
								Object obj = task.script.evel(expression);
								if(obj != null){
									if(Boolean.parseBoolean(obj.toString())){
										forkCount.getAndIncrement();
										ContainerService.execute(getNodeHandler(line.getToNodeId()));
										isFork = true;
									}
								}
							}else if(Boolean.parseBoolean(line.getExpression())){
								forkCount.getAndIncrement();
								ContainerService.execute(getNodeHandler(line.getToNodeId()));
								isFork = true;
							}
						}else{
							forkCount.getAndIncrement();
							ContainerService.execute(getNodeHandler(line.getToNodeId()));
							isFork = true;
						}
					}
					if(isFork == false){ //如果没有触发任何支线
						jnRun.run();
					}
				}else{
					throw new Exception("未设置JOIN节点");
				}
			}catch(Exception e){
				logger.error("[TASK]["+task.getToken()+"]["+task.getId()+"]["+node.getId()+"]error",e);
				TaskManager.InterruptTask(task,Constants.ACTION_ERROR,"微内核错误");
			}finally{
				//移除自身在正在执行的节点程序集合的缓存
				task.getNodeRunnables().remove(this);
			}
		}
	}
}
