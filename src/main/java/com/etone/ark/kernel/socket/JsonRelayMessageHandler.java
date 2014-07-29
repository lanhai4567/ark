/* JsonRelayMessageHandler.java	@date 2012-12-21
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;
import com.etone.ark.kernel.service.model.TemporaryDevice;

/**
 * 功能描述：转发非微内核消息
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 * 
 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
 * @version 1.4.0
 * @since 1.0.0 
 * create on: 2013-9-25 
 */
public class JsonRelayMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
    
    static final Logger logger = Logger.getLogger(JsonRelayMessageHandler.class);
	
	public JsonRelayMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
    	 TemporaryDevice tempDevice = DeviceManager.findTemporayDevice(msg.getTargetSerialId(),msg.getTargetSystemId(),msg.getTargetIndex());
    	 if(tempDevice != null){
             SocketChannel relayChannel = DeviceManager.getSocketChannel(tempDevice.getSocketChannelId());
             if(!CommandServer.relayJsonMessage(relayChannel, msg)){
                 logger.error("[RESOURCE][RELAY-MESSAGE]Socket通讯通道错误，无法转发MSG:" + msg.getMsg());
             }
         }else{
        	 if(msg.getTargetSerialId() != 0){
//        		 RtdModuleDevice rtdModule = DeviceManager.findRtdModuleDevice(msg.getTargetSerialId(), msg.getTargetSystemId(), msg.getTargetIndex());
        		 RtdDevice rtd=DeviceManager.findRtdDevice(msg.getTargetSerialId(), msg.getTargetSystemId());
        		 if(rtd != null){
        			 SocketChannel relayChannel = DeviceManager.getSocketChannel(rtd.getSocketChannelId());
        			 if(!CommandServer.relayJsonMessage(relayChannel, msg)){
                         logger.error("[RESOURCE][RELAY-MESSAGE]Socket通讯通道错误，无法转发MSG:" + msg.getMsg());
                         return true;
                     }
        		 }else{
//        			 SidSlotDevice sidSlot = DeviceManager.findSidSlotDevice(msg.getTargetSerialId(), msg.getTargetSystemId(), msg.getTargetIndex());
        			 SidDevice sid=DeviceManager.findSidDevice(msg.getTargetSerialId(), msg.getTargetSystemId());
        			 if(sid != null){
        				 SocketChannel relayChannel = DeviceManager.getSocketChannel(sid.getSocketChannelId());
            			 if(!CommandServer.relayJsonMessage(relayChannel, msg)){
                             logger.error("[RESOURCE][RELAY-MESSAGE]Socket通讯通道错误，无法转发MSG:" + msg.getMsg());
                             return true;
                         }
        			 }
        		 }
        	 }else{
        		 logger.error("[RESOURCE][RELAY-MESSAGE]未能找到目标设备，无法转发MSG:" + msg.getMsg());
        	 }
         }
         return true;
    }
}
