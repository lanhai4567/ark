/* JsonRtdLoginMessageHandler.java	@date 2013-4-2
 * @JDK 1.6
 * @encoding UTF-8
 * <p>版权所有：宜通世纪</p>
 * <p>未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel.socket;

import java.util.Date;

import org.apache.log4j.Logger;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.json.RtdInfo;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventExceptionLog;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;

/**
 * <p>RTD登记消息处理程序</p>
 * @version 1.2
 * @author 张浩 
 *
 */
public class JsonRtdLoginMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
    
    static Logger logger = Logger.getLogger(JsonRtdLoginMessageHandler.class);
	
	public JsonRtdLoginMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }
	
    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if("rtdLogin".equals(msg.getMessageHeader().getCommand())){
            try{
            	RtdInfo info = msg.getContent(RtdInfo.class);
            	if(info != null){
		    		RtdDevice device = new RtdDevice();
		    		device.setDeviceIp(info.getDeviceIp());
		    		device.setId(msg.getSourceSerialId());
		    		device.setModel(info.getModel());
		    		device.setModuleCount(info.getModuleCount());
		    		if(info.getSerialNum() != null){
		    			device.setSerialNum(info.getSerialNum());
		    		}else{
		    			device.setSerialNum(msg.getSourceSerialId());
		    		}
		    		device.setSocketChannelId(channel.getId());
		    		device.setSoftVersion(info.getSoftVersion());
		    		device.setStatus(info.getRtdStatus());
		    		device.setSystemId(msg.getSourceSystemId());
		    		device.setVendor(info.getVendor());
		    		device.setWorkStatus(0);
		    		device.setLinkTime(new Date());
		    		device.setHeartBeatTime(new Date());
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
		                	 //获取RTD模块信息
			                 for(RtdInfo.ModuleInfo item:info.getModules()){
			                	 RtdModuleDevice rtdModule = new RtdModuleDevice();
			                	 rtdModule.setId(device.getId());
			                	 rtdModule.setModuleImsi(item.getModuleImsi());
			                	 rtdModule.setModuleNum(item.getModuleNum());
			                	 rtdModule.setModuleType(item.getModuleType());
			                	 rtdModule.setBusinessType(Constants.RTD_TYPE_NORMAL);
			                	 rtdModule.setNetType(item.getNetType());
			                	 rtdModule.setSocketChannelId(channel.getId());
			                	 rtdModule.setStatus(item.getModuleStatus());
			                	 rtdModule.setSystemId(device.getSystemId());
			                	 rtdModule.setWorkStatus(0);
			                	 DeviceManager.saveRtdModuleDevice(rtdModule);
			                	 //如果是第一次登录的RTD，需要对该RTD的所有的模块发中断进行初始化
			                	 if(error == 1){
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
            	}
            	
                /*RtdInfo info = msg.readObject(RtdInfo.class);
                if(info != null){
                    DeviceRtd rtd = new DeviceRtd();
                    rtd.setChannel(channel);
                    rtd.setDeviceIp(info.getDeviceIp());
                    rtd.setModel(info.getModel());
                    rtd.setModuleCount(info.getModuleCount());
                    rtd.setSoftVersion(info.getSoftVersion());
                    rtd.setVendor(info.getVendor());
                    if(info.getRtdStatus() != null){
                        rtd.setStatus(info.getRtdStatus());
                    }else{
                        rtd.setStatus(0);
                    }
                    
                    rtd.setSystemId(msg.getSourceSystemId());
                    rtd.setSerialNum(msg.getSourceSerialId());
                    rtd.setHeartBeatTime(new Date(System.currentTimeMillis()));
                    rtd.setLinkTime(new Date(System.currentTimeMillis()));
                    
                    //缓存RTD设备信息
                    int error = DeviceManager.addDevice(rtd);
                    if(error == 2){
                        //发送微内核异常日志
                        EventExceptionLog event = new EventExceptionLog();
                        event.setCode(ResultCode.ER_KERNEL_DEVICE_ILLEGAL);
                        event.setMsg("设备序列号重复/非法，设备IP："+rtd.getDeviceIp()+"，设备序列号："+rtd.getSerialNum());
                        ClientManager.callbackEvent(event);
                        CommandServer.responseRtdLogin(rtd,null,0);
                        logger.info("[RESOURCE]["+rtd.getSerialNum()+"]RTD设备序列号重复/非法,IP:"+rtd.getDeviceIp());
                        return true;
                    }else{ 
                        //获取RTD模块信息
                        for(RtdInfo.ModuleInfo item:info.getModules()){
                            DeviceRtdModule module = new DeviceRtdModule();
                            module.setChannel(channel);
                            module.setDeviceIp(rtd.getDeviceIp());
                            module.setModuleImsi(item.getModuleImsi());
                            module.setModuleNum(item.getModuleNum());
                            if(item.getModuleStatus() != null){
                                module.setStatus(item.getModuleStatus());
                            }else{
                                module.setStatus(0);
                            }
                            module.setModuleType(item.getModuleType());
                            module.setSystemId(rtd.getSystemId());
                            module.setSerialNum(rtd.getSerialNum());
                            module.setHeartBeatTime(new Date(System.currentTimeMillis()));
                            module.setLinkTime(new Date(System.currentTimeMillis()));
                            DeviceManager.addInteriorDevice(module);
                            rtd.getModules().put(module.getIndex(),DeviceManager.buildAddress(rtd.getSystemId(), module.getSerialNum(), module.getIndex()));
                            //如果是第一次登录的RTD，需要对该RTD的所有的模块发中断进行初始化
                            if(error == 0){
                                CommandServer.requestRtdModuleRelease(module);
                            }
                        }
                    }
                    if(CommandServer.responseRtdLogin(rtd,null,1)){
                        logger.info("[RESOURCE]["+rtd.getSerialNum()+"]RTD设备登记,IP："+rtd.getDeviceIp());
                        //触发设备变更
                        ClientManager.callbackEvent(new EventRtdInfoChange(rtd));
                    }
                }*/
            }catch(Exception e){
                logger.error("[RESOURCE][RTD-LOGIN]error", e);
                SocketJsonMessage temp = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdLogin", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, null);
                if(temp != null){
                    if(channel.isAvailable()){
                        temp.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        temp.getMessageHeader().setTargetObject(DeviceManager.buildAddress(msg.getSourceSystemId(),msg.getSourceSerialId(),msg.getSourceIndex()));
                        channel.write(temp);
                        channel.close();
                    }
                }
            }
            return true;
        }
        return false;
    }
	
}
