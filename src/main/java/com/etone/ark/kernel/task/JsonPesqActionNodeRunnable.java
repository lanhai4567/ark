package com.etone.ark.kernel.task;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.FTPClientUtils;
import com.etone.ark.kernel.http.EventActionFinish;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.PesqServer;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class JsonPesqActionNodeRunnable extends ActionNodeHandler<SocketJsonMessage> {

    // 动作结果回调函数
    protected EventActionFinish event;

    public JsonPesqActionNodeRunnable(Task task, Node node) {
        super(task, node);
        this.event = new EventActionFinish();
    }

    @Override
    public void send() throws Exception {
    	logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "]PESQ开始");
    	event.setBeginDate(new Date(System.currentTimeMillis()));
    	String voiceFilePath = parameters.getString("voiceFile");
    	String recordFilePath = parameters.getString("recordFile");
    	logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "]PESQ下载语音文件："+voiceFilePath);
    	//下载语音文件
		File voiceFile = FTPClientUtils.get().downFile(voiceFilePath,"./temp/"+task.getId()+"/"+node.getId()+"/");
    	if(voiceFile != null){
    		logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "]PESQ下载语音文件："+recordFilePath);
    		//下载录音文件
    		File recordFile = FTPClientUtils.get().waitDownFile(recordFilePath,"./temp/",(60*1000L));
    		if(recordFile != null){
    			FTPClientUtils.disconnect();
    			parameters.put("resultCode", Constants.ACTION_SUCCESS);
    			//如果PESQ初始化失败，这里会堵塞
    			parameters.put("pesqValue",PesqServer.pesq(voiceFile.getParent(),voiceFile.getName(),recordFile.getPath()));
    			task.getHistory().put(parameters);
    			this.triggerActionFinishEvent(); // 触发节点完成事件
    			logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "]PESQ : "+parameters.getString("pesqValue"));
    	        // 下一个节点
    	        this.getNextNodeHandler().run();
    	        return;
    		}else{
    			FTPClientUtils.disconnect();
    		}
    	}
        parameters.put("resultCode", Constants.ACTION_ERROR);
        task.getHistory().put(parameters);
        this.triggerActionFinishEvent(); // 触发节点完成事件
        logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "]error");
        // 下一个节点
        this.getNextNodeHandler().run();
    }
    
    @Override
    public void receice(SocketJsonMessage msg) throws Exception {}

    protected void triggerActionFinishEvent() {
        this.event.setEndDate(new Date(System.currentTimeMillis()));
        this.event.setTask(task);
        this.event.setNode(node);
        this.event.setResource(resource);
        if (StringUtils.isEmpty(this.event.getActionResult())) {
            this.event.setActionResult(this.parameters.getString("resultCode"));
        }
        ClientManager.callbackEvent(event);
    }

    @Override
    public void receiceTimeout() throws Exception {}
}
