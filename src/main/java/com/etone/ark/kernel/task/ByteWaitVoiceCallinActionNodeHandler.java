package com.etone.ark.kernel.task;

import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class ByteWaitVoiceCallinActionNodeHandler extends ByteActionNodeHandler{
    
    public ByteWaitVoiceCallinActionNodeHandler(Task task, Node node) {
        super(task, node);
    }

    @Override
    public void send() throws Exception {
        //监听同步事件
        EventController.listen("sync_" + DeviceManager.getDeviceID(resource.getRtdSerialNum(), resource.getRtdModuleNum()),new ByteSyncActionHandler(resource,Integer.valueOf(task.getId()),1),60);
        //发送呼叫报文
        super.send();
    }
}
