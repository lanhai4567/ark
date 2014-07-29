package com.etone.ark.kernel.task;

import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class ByteReceiveSmsActionNodeHandler extends ByteActionNodeHandler{
    
    public ByteReceiveSmsActionNodeHandler(Task task, Node node) {
        super(task, node);
    }

    @Override
    public void send() throws Exception {
        //监听同步事件
        EventController.listen("sync_" + DeviceManager.getDeviceID(resource.getRtdSerialNum(), resource.getRtdModuleNum()),new ByteSyncActionHandler(resource,Integer.valueOf(task.getId()),0),60);
        //发送接收短信报文
        super.send();
    }

}
