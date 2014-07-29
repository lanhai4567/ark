/* NodeEndRunnable.java @date 2012-12-9
 * @old name by ProcessEndNodeRunnable.java	@date 2012-11-13
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task;

import java.util.Date;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventTaskFinish;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

/**
 * 测试业务结束节点执行程序
 * @version 1.2
 * @author 张浩
 *
 */
public class NodeEndRunnable extends NodeHandler {
	
	static Logger logger = Logger.getLogger(NodeEndRunnable.class);
	
	public NodeEndRunnable(Task task,Node node) {
	    super(task,node);
	}

	@Override
	public void run() {
		//只有在运行状态时
		if(task.isRunState(Task.STATUS_EXECUTE)){
			try{
				//设置测试业务状态为完成
				task.setRunState(Task.STATUS_FINISH);
				//设置测试业务完成时间
				task.setEndDate(new Date(System.currentTimeMillis()));
				//释放资源
				for(String key : task.getResources().keySet()){
					Resource resource = task.getResources().get(key);
					DeviceManager.unemployResource(resource);
					DeviceManager.removeResource(resource.getRtdSerialNum(), resource.getRtdModuleNum(), resource.getImsi());
				}
				//将结果加入到脚本中
				//task.getHistory().put("resultCode", task.getResultCode());
				//task.getHistory().put("errorDescription",task.getErrorDescription());
				task.script.put("resultCode", task.getResultCode());
				task.script.put("errorDescription", task.getErrorDescription());
				//日志输出
				logger.debug("[TASK]["+task.getId()+"]["+node.getId()+"]end");
				//触发测试业务完成事件回调函数
				EventTaskFinish event = new EventTaskFinish();
				event.setTask(task);
				ClientManager.callbackEvent(event);
			}catch(Exception e){
				logger.error("[TASK]["+task.getId()+"]["+node.getId()+"][error]",e);
				TaskManager.InterruptTask(task,Constants.ACTION_ERROR,"微内核错误");
			}
		}
	}
}

