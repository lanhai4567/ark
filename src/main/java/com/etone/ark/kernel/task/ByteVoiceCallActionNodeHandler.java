package com.etone.ark.kernel.task;

import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class ByteVoiceCallActionNodeHandler extends ByteActionNodeHandler{
    
    public ByteVoiceCallActionNodeHandler(Task task, Node node) {
        super(task, node);
    }

    @Override
    public void send() throws Exception {
        EventController.listen("sync_" + DeviceManager.getDeviceID(resource.getRtdSerialNum(), resource.getRtdModuleNum()),new ByteSyncActionHandler(resource,Integer.valueOf(task.getId()),1),60);
        //发送呼叫报文
        super.send();
    }

}
