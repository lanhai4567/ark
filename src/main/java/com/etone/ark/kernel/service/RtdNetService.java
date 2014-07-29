package com.etone.ark.kernel.service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.etone.ark.communication.Protocol;
import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.socket.ISocketMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.Constants;

public class RtdNetService {
    
    static final Logger logger = Logger.getLogger(RtdNetService.class);

    private static RtdNetCallEntity entity;
    
    private static Timer timer;
    
    public static void initial(SocketChannel channel){
        logger.debug("[SOCKET][RTD-NET][connection...]");
        entity = new  RtdNetCallEntity(channel);
        timer = new Timer();
        timer.schedule(new Heart(), 0);
    }
    
    public static boolean upRtdNet(SocketJsonMessage msg ){
        if(entity != null && entity.getIsLink()){
            //logger.debug("[SOCKET][RTD-NET][send]" + msg.getMsg());
            return entity.getChannel().write(msg);
        }
        return false;
    }
    
    public static class Heart extends TimerTask{
        
        public void run() {
            Protocol protocol = ProtocolManager.getProtocol(Constants.JSON_PROTOCOL_VERSION,"rtdModuleNetInfoHeart");
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(protocol, ISocketMessage.TYPE_REQUEST, SocketJsonMessage.CONTENT_TYPE_JSON, null);  
            if(msg != null){
                msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 3,null));
                String eventName = protocol.getResponse().getCommand() + "_" + msg.getMessageHeader().getTargetObject();
                EventController.one(eventName, new IEventHandler<SocketJsonMessage>(){

                    @Override
                    public void execute(SocketJsonMessage obj) {
                        //logger.debug("[SOCKET][RTD-NET][heart-success]");
                        entity.setIsLink(true);
                        timer.schedule(new Heart(),30*1000);
                        //==========测试========
                        /*SocketJsonMessage temp = new SocketJsonMessage();
                        temp.setMessageHeader(new SocketJsonMessageHeader());
                        temp.getMessageHeader().setCommand("rtdCell");
                        temp.getMessageHeader().setContentType(SocketJsonMessage.CONTENT_TYPE_JSON);
                        temp.getMessageHeader().setType(ISocketMessage.TYPE_REQUEST);
                        temp.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        temp.getMessageHeader().setTargetObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 3,null));
                        //设置报文超时时间
                        temp.setBody("{\"moduleSerialNum\":1,\"moduleType\":\"AD-C-01\",\"netInfos\":[{\"time\":\"2103-07-08\",\"netInfo\":\"CDMA-1X|460030902405838,283,2512,7,9,9,13828,-105.666,-9.18,-64.0,28,32,5,3,3,4,5,3,3,2,3,3,1,30,-7.5,33,184,-19.0,228,-12.5,132,-31.5,352,-31.5,16,-31.5,366,-31.5,198,-31.5,436,-31.5,144,-31.5,400,-31.5,476,-31.5,82,-31.5,430,-31.5,358,-31.5,308,-31.5,168,-31.5,260,-31.5,490,-31.5,340,-31.5,250,-31.5,92,-31.5,222,-31.5,300,-31.5,140,-31.5,96,-31.5,34,-31.5,242,-31.5,254,-31.5,410,-31.5,380,-31.5,190,-31.5,36,-31.5,22,-31.5\"},{\"time\":\"2103-07-09\",\"netInfo\":\"CDMA-EVDO|460030902405838,37,2512,-20.53,7.00,-58.81,-29,6,4,7,3,1,30,-1.0,26,366,-31.5,198,-31.5,436,-31.5,184,-31.5,410,-31.5,132,-31.5,400,-31.5,352,-31.5,476,-31.5,82,-31.5,430,-31.5,358,-31.5,308,-31.5,168,-31.5,260,-31.5,490,-31.5,340,-31.5,250,-31.5,92,-31.5,222,-31.5,300,-31.5,140,-31.5,96,-31.5,242,-31.5,254,-31.5,228,-31.5\"}]}");
                        upRtdNet(temp);*/
                    }

                    @Override
                    public void timeout() {
                        logger.debug("[SOCKET][RTD-NET][heart-timout]");
                        entity.setIsLink(false);
                        timer.schedule(new Heart(),2*1000);
                    }
                },28);
                if(!entity.getChannel().write(msg)){
                    EventController.cancel(eventName);
                    logger.debug("[SOCKET][RTD-NET][send-error]");
                    entity.setIsLink(false);
                }
            }else{
                entity.setIsLink(false);
            }
        }
    }
    
    public static class RtdNetCallEntity{
        
        private SocketChannel channel;
        private AtomicBoolean isLink;
        
        public RtdNetCallEntity(SocketChannel channel){
            this.isLink = new AtomicBoolean(true);
            this.channel = channel;
        }
        
        public SocketChannel getChannel() {
            return channel;
        }
        public void setChannel(SocketChannel channel) {
            this.channel = channel;
        }
        public boolean getIsLink() {
            return isLink.get();
        }
        public void setIsLink(boolean isLink) {
            this.isLink.set(isLink);
        }
    }
    
}
