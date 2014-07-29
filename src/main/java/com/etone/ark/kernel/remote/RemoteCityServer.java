package com.etone.ark.kernel.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.etone.ark.communication.Protocol;
import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.Constants;

public class RemoteCityServer{
    
    static final Logger logger = Logger.getLogger(RemoteCityServer.class);
    
    private static SocketChannel channel;
    private static AtomicBoolean isLink;
    private static Timer timer;
    
    private RemoteCityServer(){}
    
    public static void initial(SocketChannel ch){
        channel = ch;
        isLink = new AtomicBoolean(false);
        timer = new Timer();
    }
    
    public static void sendLogin(){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("systemId", Constants.REMOTE_SYSTEM_ID);
        params.put("localLic","8F00B204E9800998");
        params.put("version","1");
        params.put("type",1);
        Protocol protocol = ProtocolManager.getProtocol(Constants.BYTE_PROTOCOL_VERSION,"localLogin");
        SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(protocol,SocketByteMessage.TYPE_REQUEST,SocketMessageBuilder.getMessageId(),params);
        if(msg != null){
            msg = SocketMessageBuilder.setFromAndTo(msg, Constants.REMOTE_SYSTEM_ID, 0, Constants.SYSTEM_ID, 0);
            EventController.one(protocol.getResponse().getCommand() + "_" + Constants.REMOTE_SYSTEM_ID + "_" + 0, new IEventHandler<SocketByteMessage>(){
                public void execute(SocketByteMessage msg) {
                    try{
                        if(msg.readUnsignedByte() == 0){
                            logger.debug("sendLogin -> success");
                            isLink.set(true); //如果登陆成功则发送心跳
                            timer.schedule(new TimerTask(){
                                public void run() {
                                    sendHeart();
                                }
                                
                            }, 30*1000);
                            return;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
  
                    //登陆失败,继续发送登陆
                    isLink.set(false); 
                    timer.schedule(new TimerTask(){
                        public void run() {
                            sendLogin();
                        }
                    },  30*1000);
                }

                public void timeout() {
                    isLink.set(false); 
                    timer.schedule(new TimerTask(){
                        public void run() {
                            sendLogin();
                        }
                    }, 30*1000);
                }
            },msg.getTimeout());
            
            channel.write(msg);
        }
    }
    
    public static void sendHeart(){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("systemId", Constants.REMOTE_SYSTEM_ID);
        params.put("systemStatus",0);
        Protocol protocol = ProtocolManager.getProtocol(Constants.BYTE_PROTOCOL_VERSION,"localHeart");
        SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(protocol,SocketByteMessage.TYPE_REQUEST,SocketMessageBuilder.getMessageId(),params);
        if(msg != null){
            msg = SocketMessageBuilder.setFromAndTo(msg, Constants.REMOTE_SYSTEM_ID, 0, Constants.SYSTEM_ID, 0);
            EventController.listen(protocol.getResponse().getCommand() + "_" + Constants.REMOTE_SYSTEM_ID + "_" + 0, new IEventHandler<Object>(){
                public void execute(Object param) {
                    isLink.set(true);
                    timer.schedule(new TimerTask(){
                        public void run() {
                            sendHeart();
                        }
                    }, 30*1000);
                }

                @Override
                public void timeout() {
                    //心跳超时,重新登陆
                    isLink.set(false);
                    timer.schedule(new TimerTask(){
                        public void run() {
                            sendLogin();
                        }
                    }, 30*1000);
                }
            }, msg.getTimeout());
            channel.write(msg);
        }
    }

    public static SocketChannel getChannel(){
        return channel;
    }
}
