package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessage;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventExceptionLog;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;

public class ByteRtdLoginMessageHandler extends AbsSocketMessageHandler<SocketByteMessage> {

    static Logger logger = Logger.getLogger(ByteRtdLoginMessageHandler.class);

    public ByteRtdLoginMessageHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
        if(msg.getMessageHeader() == null){
            logger.error("[RESOURCE][RTD-LOGIN]BYTE报文错误");
            return true;
        }
        if (msg.getMessageHeader().getCommand() == 0x0201) {
            try {
            	RtdDevice device = new RtdDevice();
            	device.setSerialNum(Integer.valueOf(msg.readString(16).trim()));
            	device.setDeviceIp(msg.readString(16).trim());
            	device.setSoftVersion(msg.readString(8).trim());
                msg.readUnsignedByte(); // ConnectType
                device.setVendor(String.valueOf(msg.readUnsignedByte()).trim());
                device.setStatus(0);
                device.setSystemId(msg.getMessageHeader().getSourceHighAddress());
                int moduleCount = msg.readUnsignedByte();
                device.setModuleCount(moduleCount);
                device.setId(msg.getMessageHeader().getSourceLowAddress());
                
            	int error = DeviceManager.saveRtdDevice(device);
	    		if(error == 4){
	                 //发送微内核异常日志
	                 EventExceptionLog event = new EventExceptionLog();
	                 event.setCode(ResultCode.ER_KERNEL_DEVICE_ILLEGAL);
	                 event.setMsg("RTD设备序列号重复/非法，设备IP："+device.getDeviceIp()+"，设备序列号："+device.getSerialNum());
	                 ClientManager.callbackEvent(event);
	                 CommandServer.responseRtdLogin(device,null,0);
	                 logger.info("[RESOURCE]["+device.getSerialNum()+"]RTD设备序列号重复/非法,IP:"+device.getDeviceIp());
	                 return true;
	    		}else if(error == 3){
	    			 EventExceptionLog event = new EventExceptionLog();
	                 event.setCode(ResultCode.ER_KERNEL_DEVICE_ILLEGAL);
	                 event.setMsg("RTD设备参数错误，设备IP："+device.getDeviceIp()+"，设备序列号："+device.getSerialNum());
	                 ClientManager.callbackEvent(event);
	                 CommandServer.responseRtdLogin(device,null,0);
	                 logger.info("[RESOURCE]["+device.getSerialNum()+"]RTD设备参数错误,IP:"+device.getDeviceIp());
	                 return true;
	    		}else if(error == 1 || error == 2){
	                 if(CommandServer.responseRtdLogin(device,null,1)){
	                	 for (int i = 0; i < moduleCount; i++) {
	                         RtdModuleDevice rtdModule = new RtdModuleDevice();
	                         rtdModule.setSystemId(device.getSystemId());
	                         rtdModule.setId(device.getId());
	                         rtdModule.setModuleNum(msg.readUnsignedByte());
	                         rtdModule.setModuleType(String.valueOf(msg.readUnsignedByte()).trim());
	                         msg.readUnsignedByte();// ModuleMode??
	                         rtdModule.setModuleImsi(msg.readString(20).trim());
	                         rtdModule.setStatus(msg.readUnsignedByte());// 模块状态1、可用  2、不可用
	                         msg.readUnsignedByte(); // DiSignal??
	                         rtdModule.setWorkStatus(0);
		                	 DeviceManager.saveRtdModuleDevice(rtdModule);
	                         //如果是第一次登录的RTD，需要对该RTD的所有的模块发中断进行初始化
	                         if(error == 0){
	                        	 CommandServer.requestRtdModuleRelease(rtdModule);
	                         }
	                     }
		                 logger.info("[RESOURCE]["+device.getSerialNum()+"]RTD设备登记,IP："+device.getDeviceIp());
	                     //触发设备变更
	                     ClientManager.callbackEvent(new EventRtdInfoChange(device));
	                 }
	    		}else{
	    			 CommandServer.responseRtdLogin(device,null,0);
	    		}
            } catch (Exception e) {
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
                    channel.close();
                }
            }
            return true;
        }
        return false;
    }

}
