package com.etone.ark.kernel.task;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class ByteSendSmsActionNodeHandler extends ByteActionNodeHandler{
    
    public ByteSendSmsActionNodeHandler(Task task, Node node) {
        super(task, node);
    }

    @Override
    public void send() throws Exception {
        //监听同步事件
        EventController.listen("sync_" + DeviceManager.getDeviceID(resource.getRtdSerialNum(), resource.getRtdModuleNum()),new ByteSyncActionHandler(resource,Integer.valueOf(task.getId()),0),60);
        //发送发送短信报文
        event.setBeginDate(new Date(System.currentTimeMillis()));
        Object time = this.parameters.get("reportTimeout");
        int t = 0;
        if(time != null && StringUtils.isNotEmpty(time.toString())){
           t = Double.valueOf(time.toString()).intValue();
        }
        if(t > 0){
            this.parameters.put("ifWaitForReport", 1);
            this.parameters.put("overTime", t);
        }else{
            this.parameters.put("ifWaitForReport", 0);
            this.parameters.put("overTime", t);
        }
        super.send();
    }

}
