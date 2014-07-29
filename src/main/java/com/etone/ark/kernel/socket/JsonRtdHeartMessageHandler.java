/* JsonRtdHeartMessageHandler.java	@date 2012-12-24
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.socket;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.json.RtdInfo;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
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
public class JsonRtdHeartMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
	
    static Logger logger = Logger.getLogger(JsonRtdHeartMessageHandler.class);
    
	public JsonRtdHeartMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if("rtdHeart".equals(msg.getMessageHeader().getCommand())){
            try{
            	RtdInfo info = msg.getContent(RtdInfo.class);
            	if(info != null){
            		RtdDevice device = DeviceManager.findRtdDevice(msg.getSourceSerialId(),msg.getSourceSystemId());
            		//如果是升级状态的RTD设备则不进行心跳
                    if(device == null || Constants.DEVICE_STATUS_UPDATE == device.getStatus()){
                        return true;
                    }
                    boolean isChange = false; //变更开关
                    if(info.getRtdStatus() != device.getStatus()){
                        isChange = true;
                        device.setStatus(info.getRtdStatus());
                    }
                    if(info.getModuleCount() != device.getModuleCount()){
                        isChange = true;
                        device.setModuleCount(info.getModuleCount());
                    }
                	//心跳时间
                	device.setHeartBeatTime(new Date());
                	DeviceManager.updateRtdDevice(device);
                    //检查RTD模块
                    for(RtdInfo.ModuleInfo item:info.getModules()){
                    	RtdModuleDevice rtdModule =  DeviceManager.findRtdModuleDevice(device.getId(), device.getSystemId(), item.getModuleNum());
                    	if(rtdModule != null){
                    		boolean isModuleChange = false;
                    		if(rtdModule.getStatus() != item.getModuleStatus()){
                    			isModuleChange = true;
                    			rtdModule.setStatus(item.getModuleStatus());
                            }
                            if(!StringUtils.equals(rtdModule.getModuleType(), item.getModuleType())){
                            	isModuleChange = true;
                                rtdModule.setModuleType(item.getModuleType());
                            }
                            if(!StringUtils.equals(rtdModule.getModuleImsi(), item.getModuleImsi())){
                            	isModuleChange = true;
                                rtdModule.setModuleImsi(item.getModuleImsi());
                            }
                            if(isModuleChange){
                            	isChange = true;
                            	DeviceManager.updateRtdModuleDevice(rtdModule);
                            }
                    	}else{
                    		 rtdModule = new RtdModuleDevice();
		                	 rtdModule.setId(device.getId());
		                	 rtdModule.setModuleImsi(item.getModuleImsi());
		                	 rtdModule.setModuleNum(item.getModuleNum());
		                	 rtdModule.setModuleType(item.getModuleType());
		                	 rtdModule.setNetType(item.getNetType());
		                	 rtdModule.setSocketChannelId(channel.getId());
		                	 rtdModule.setStatus(item.getModuleStatus());
		                	 rtdModule.setSystemId(device.getSystemId());
		                	 rtdModule.setWorkStatus(0);
		                	 DeviceManager.saveRtdModuleDevice(rtdModule);
		                	 isChange = true;
                    	}
                    }
                    //设备变更
                    if(isChange){
                    	ClientManager.callbackEvent(new EventRtdInfoChange(device));
                    }
                    CommandServer.responseRtdHeart(device,null,1);
            	}
            	
                /*final DeviceRtd device = DeviceManager.getDeviceRtd(msg.getMessageHeader().getSourceObject());
                //如果是升级状态的RTD设备则不进行心跳
                if(device == null || Constants.DEVICE_STATUS_UPDATE == device.getStatus()){
                    return true;
                }
                device.setHeartBeatTime(new Date(System.currentTimeMillis())); //设置心跳时间
                boolean isChange = false; //变更开关
                RtdInfo info = msg.readObject(RtdInfo.class);
                if(info != null){
                    if(info.getRtdStatus() != device.getStatus()){
                        isChange = true;
                        device.setStatus(info.getRtdStatus());
                    }
                    if(info.getModuleCount() != device.getModuleCount()){
                        isChange = true;
                        device.setModuleCount(info.getModuleCount());
                    }
                    //检查RTD模块
                    for(RtdInfo.ModuleInfo item:info.getModules()){
                        String key = DeviceManager.buildAddress(device.getSystemId(), device.getSerialNum(), item.getModuleNum());
                        final DeviceRtdModule temp =  DeviceManager.getDeviceRtdModule(key);
                        if(temp != null){
                            temp.setHeartBeatTime(new Date(System.currentTimeMillis())); //设置心跳时间
                            if(temp.getStatus() != item.getModuleStatus()){
                                isChange = true;
                                temp.setStatus(item.getModuleStatus());
                            }
                            if(!StringUtils.equals(temp.getModuleType(), item.getModuleType())){
                                isChange = true;
                                temp.setModuleType(item.getModuleType());
                            }
                            if(!StringUtils.equals(temp.getModuleImsi(), item.getModuleImsi())){
                                isChange = true;
                                temp.setModuleImsi(item.getModuleImsi());
                                DeviceManager.addInteriorDevice(temp); //缓存imsi关联关系
                            }
                        }
                    }
                    //设备变更
                    if(isChange){
                        //TODO 触发设备变更事件
                        //ClientManager.callbackEvent(new EventRtdInfoChange(device));
                    }
                    CommandServer.responseRtdHeart(device,null,1);
                }*/
            }catch(Exception e){
                logger.error("[RESOURCE][SID-HEART]error", e);
               /* SocketJsonMessage temp = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdHeart", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, null);
                if(temp != null && channel.isAvailable()){
                    temp.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                    temp.getMessageHeader().setTargetObject(DeviceManager.buildAddress(msg.getSourceSystemId(), msg.getSourceSerialId(), msg.getSourceIndex()));
                    channel.write(temp);
                }*/
            }
            return true;
        }
        return false;
    }
}
