/* RTDHeartMessageHandle.java	@date 2012-12-24
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.socket;

import java.util.Date;

import org.apache.log4j.Logger;

import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessage;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;

/**
 * <p>RTD心跳消息处理程序</p>
 * @version 1.2
 * @author 张浩 
 *
 */
public class ByteRtdHeartMessageHandler extends AbsSocketMessageHandler<SocketByteMessage>{
    
    static Logger logger = Logger.getLogger(ByteRtdHeartMessageHandler.class);
	
	public ByteRtdHeartMessageHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }
	
	@Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
	    if(msg.getMessageHeader().getCommand() == 0x0202){
            try{
                if(msg.getMessageHeader().getSourceLowAddress() > 20000){
                    msg.getMessageHeader().setSourceLowAddress(msg.getMessageHeader().getSourceLowAddress() - 20000);
                }
                
                RtdDevice device = DeviceManager.findRtdDevice(msg.getMessageHeader().getSourceLowAddress(),msg.getMessageHeader().getSourceHighAddress());
        		//如果是升级状态的RTD设备则不进行心跳
                if(device == null || Constants.DEVICE_STATUS_UPDATE == device.getStatus()){
                    return true;
                }
                boolean isChange = false; //变更开关
                int status = msg.readUnsignedByte();
                if(status != device.getStatus()){
                    isChange = true;
                    device.setStatus(status);
                }
                int moduleCount = msg.readUnsignedByte();
                if(moduleCount != device.getModuleCount()){
                    isChange = true;
                    device.setModuleCount(moduleCount);
                }
            	//心跳时间
            	device.setHeartBeatTime(new Date());
            	DeviceManager.saveRtdDevice(device);
                //检查RTD模块
            	for (int i = 0; i < moduleCount; i++) {
            		int moduleNum = msg.readUnsignedByte();
                	RtdModuleDevice rtdModule =  DeviceManager.findRtdModuleDevice(device.getId(), device.getSystemId(), moduleNum);
                	if(rtdModule != null){
                		boolean isModuleChange = false;
                		int moduleStatus = msg.readUnsignedByte();
                		if(rtdModule.getStatus() != moduleStatus){
                			isModuleChange = true;
                			rtdModule.setStatus(moduleStatus);
                        }
                		if(isModuleChange){
                        	isChange = true;
                        	DeviceManager.updateRtdModuleDevice(rtdModule);
                        }
                	}
                }
                //设备变更
                if(isChange){
                	ClientManager.callbackEvent(new EventRtdInfoChange(device));
                }
                CommandServer.responseRtdHeart(device,null,1);
            }catch(Exception e){
                logger.error("[RESOURCE][RTD-LOGIN]error", e);
                //发送心跳失败报文
                SocketByteMessage response = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"rtdHeart", ISocketMessage.TYPE_RESPONSE, msg.getMessageHeader().getMessageId(), null);
                if (response != null && channel.isAvailable()) {
                    response.getMessageHeader().setCommandStatus(1);
                    response.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                    response.getMessageHeader().setSourceLowAddress(0);
                    response.getMessageHeader().setTargetHighAddress(msg.getMessageHeader().getSourceHighAddress());
                    response.getMessageHeader().setTargetLowAddress(msg.getMessageHeader().getSourceLowAddress());
                    channel.write(response);
                }
            }
            return true;
	    }
        return false;
    }
}
