package com.etone.ark.kernel.task;

import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class BytePowerOnActionNodeHandler extends ByteActionNodeHandler{
    
    public BytePowerOnActionNodeHandler(Task task, Node node) {
        super(task, node);
    }

    @Override
    public void send() throws Exception {
        Object sidId = this.parameters.get("sidId");
        if(sidId != null){
            this.parameters.put("sidId", Integer.valueOf(sidId.toString()) + 10000);
        }else{
            this.parameters.put("sidId", 10000);
        }
        super.send();
    }

}
