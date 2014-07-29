package com.etone.ark.kernel.task;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.Resource;

public class ByteSyncActionHandler implements IEventHandler<SocketByteMessage>{
    
    static Logger logger = Logger.getLogger(ByteSyncActionHandler.class);
    
    private Integer size;
    private final Resource resource;
    private int taskId;
    private int messageId;

    private SyncInfo info;
    
    public ByteSyncActionHandler(Resource resource,int taskId,int size){
        this.resource = resource;
        this.size = size;
        this.taskId = taskId;
    }

    @Override
    public void execute(SocketByteMessage msg) {
        try{
            if(msg != null){
                this.messageId = msg.getMessageHeader().getMessageId();
                this.info = new SyncInfo();
                info.setOperation(msg.readString(2));
                info.setIsGo(msg.readChar());
                if(size > 0){
                    EventController.multiListen("sync_" + DeviceManager.getDeviceID(resource.getRtdSerialNum(), resource.getRtdModuleNum()),new ByteSyncActionHandler(resource,taskId,(size-1)),60);
                }
                //发送同步命令回复
                int error = CommandServer.responseAction("sync",resource,taskId,messageId,info);
                if(error == 1){
                    logger.debug("同步命令回复:task:"+taskId +",deviceId:"+resource.getRtdSerialNum()+",moduleId:"+resource.getRtdModuleNum());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void timeout() {
        //----
    }
    
    public void cancel(){
        
    }
    
    public static class SyncInfo{
        private String operation;
        private int isGo;
        public String getOperation() {
            return operation;
        }
        public void setOperation(String operation) {
            this.operation = operation;
        }
        public int getIsGo() {
            return isGo;
        }
        public void setIsGo(int isGo) {
            this.isGo = isGo;
        }
    }
   
}
