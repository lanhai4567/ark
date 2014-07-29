package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.SidDevice;

public class ByteRelayMessageHandler extends AbsSocketMessageHandler<SocketByteMessage>{

    static final Logger logger = Logger.getLogger(ByteRelayMessageHandler.class);
    
    public ByteRelayMessageHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
    	RtdDevice rtd = DeviceManager.findRtdDevice(msg.getMessageHeader().getTargetLowAddress(), msg.getMessageHeader().getTargetHighAddress());
    	if(rtd != null){
    		 SocketChannel relayChannel = DeviceManager.getSocketChannel(rtd.getSocketChannelId());
             if(!CommandServer.relayByteMessage(relayChannel, msg)){
                 logger.error("[RESOURCE][RELAY-MESSAGE]Socket通讯通道错误，无法转发MSG:" + msg.getMsg());
                 return true;
             }
    	}else{
    		SidDevice sid = DeviceManager.findSidDevice(msg.getMessageHeader().getTargetLowAddress(), msg.getMessageHeader().getTargetHighAddress());
    		if(sid != null){
    			 SocketChannel relayChannel = DeviceManager.getSocketChannel(sid.getSocketChannelId());
                 if(!CommandServer.relayByteMessage(relayChannel, msg)){
                     logger.error("[RESOURCE][RELAY-MESSAGE]Socket通讯通道错误，无法转发MSG:" + msg.getMsg());
                     return true;
                 }
    		}
    	}
    	
    	logger.error("[RESOURCE][RELAY-MESSAGE]未能找到目标设备，无法转发MSG:" + msg.getMsg());
        return true;
    }
}
