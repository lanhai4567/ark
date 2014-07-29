/* NodeJoinRunnable.java @date 2012-12-9
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

/**
 * 聚合节点执行程序
 * @version 1.2
 * @author 张浩
 *
 */
public class NodeJoinHandler extends NodeHandler {
	
	static Logger logger = Logger.getLogger(NodeJoinHandler.class);

	private final AtomicInteger forkCount;

	public NodeJoinHandler(Task task,Node node,AtomicInteger forkCount) {
	    super(task,node);
		this.forkCount = forkCount;
	}
	
	@Override
	public void run() {
		//只有在运行状态时
		if(task.isRunState(Task.STATUS_EXECUTE)){
			try{
				//将自身加入到正在执行的节点程序集合中
				task.getNodeRunnables().put(node.getId(),this);
				if(forkCount == null || forkCount.decrementAndGet() <= 0){
					//下一个节点
				    this.getNextNodeHandler().run();
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
