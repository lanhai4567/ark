package com.etone.ark.kernel.task;

import java.util.Date;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventActionFinish;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.service.model.SidSlotDevice;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class JsonActionNodeHandler extends ActionNodeHandler<SocketJsonMessage> {

    // 动作结果回调函数
    protected EventActionFinish event;

    public JsonActionNodeHandler(Task task, Node node) {
        super(task, node);
        this.event = new EventActionFinish();
    }

    @Override
    public void send() throws Exception {
        event.setBeginDate(new Date(System.currentTimeMillis()));
        int error = CommandServer.requestAction(node.getActionType(), resource, Integer.valueOf(task.getId()),
                this.parameters.getBingdings(), this);
        if (error == 2) {
            parameters.put("resultCode",Constants.ACTION_ERROR);
            parameters.put("errorDescription", "构建报文失败");
            task.getHistory().put(parameters);
            this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
            this.triggerActionFinishEvent(); // 触发节点完成事件
            // 下一个节点
            this.getNextNodeHandler().run();
        } else if (error == 3) {
            parameters.put("resultCode", Constants.ACTION_ERROR);
            parameters.put("errorDescription", "通讯失败");
            //task.getHistory().put(parameters);
            task.script.putIn(node.getId(), parameters.getBingdings());
            this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
            this.triggerActionFinishEvent(); // 触发节点完成事件
            // 下一个节点
            this.getNextNodeHandler().run();
        } else if (error == 1 && (node.getActionType().equals("register") || node.getActionType().equals("androidOpen"))) {
        	DeviceManager.updateResourceBindStatus(resource.getRtdSerialNum(),resource.getRtdModuleNum(),resource.getImsi(),Resource.BINDING);
        }
    }

    @Override
    public void receice(SocketJsonMessage msg) throws Exception {
        if (("unregister".equals(node.getActionType()) || "androidClose".equals(node.getActionType()))
        		&& Constants.ACTION_SUCCESS.equals(parameters.getString("resultCode"))) {
        	DeviceManager.updateResourceBindStatus(resource.getRtdSerialNum(),resource.getRtdModuleNum(),resource.getImsi(),Resource.UNBIND);
        }
        if ("rtdRelease".equals(node.getActionType())) {
        	DeviceManager.updateResourceBindStatus(resource.getRtdSerialNum(),resource.getRtdModuleNum(),resource.getImsi(),Resource.UNBIND);
        }
        //this.parameters.put(msg.getContent());
        //task.getHistory().put(parameters);
        task.script.putIn(node.getId(), msg.getContent());
        // 将节点参数加入到测试业务参数中
        this.setIfTaskResult(msg.readString("resultCode"),msg.readString("errorDescription"));
        logger.debug("[TASK][" + task.getId() + "][" + node.getId() + "][receiveMsg]" + msg.getMsg());
        this.triggerActionFinishEvent(); // 触发节点完成事件
        // 下一个节点
        this.getNextNodeHandler().run();
    }

    protected void triggerActionFinishEvent() {
        this.event.setEndDate(new Date(System.currentTimeMillis()));
        this.event.setTask(task);
        this.event.setNode(node);
        this.event.setResource(resource);
        if (StringUtils.isEmpty(this.event.getActionResult())) {
            try {
            	Object obj = task.script.get(node.getId() +".resultCode");
            	if(obj != null){
            		this.event.setActionResult(obj.toString());
            	}
			} catch (ScriptException e) {
				e.printStackTrace();
				this.event.setActionResult(Constants.ACTION_ERROR);
			}
        }
        ClientManager.callbackEvent(event);
    }

    @Override
    public void receiceTimeout() throws Exception {
        // 超时处理
        parameters.put("resultCode", Constants.ACTION_TIMEOUT);
        parameters.put("errorDescription","动作超时");
        // 判断设备是否故障
        if (Constants.RTD_MODULE_STATUS_NORMAL != resource.getRtdModule().getStatus()) {
            parameters.put("resultCode", resource.getRtdModule().getStatus());
            parameters.put("errorDescription","模块故障");
        }

        SidSlotDevice slot = resource.getSidSlot();
        if (slot != null && Constants.SID_SLOT_STATUS_NORMAL != slot.getStatus()) {
            parameters.put("resultCode", slot.getStatus());
            parameters.put("errorDescription","卡槽故障");
        }
        //task.getHistory().put(parameters);
        task.script.putIn(node.getId(), parameters.getBingdings());
        //this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
        logger.debug("[TASK][" + task.getId() + "][" + node.getId() + "][timeout]");
        this.triggerActionFinishEvent(); // 触发节点完成事件
        TaskManager.InterruptTask(task,parameters.getString("resultCode"),parameters.getString("errorDescription"));
    }

}
