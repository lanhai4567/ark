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
public class JsonAndroidHeartMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
	
    static Logger logger = Logger.getLogger(JsonAndroidHeartMessageHandler.class);
    
	public JsonAndroidHeartMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if("androidHeart".equals(msg.getMessageHeader().getCommand())){
            try{
            	RtdInfo info = msg.getContent(RtdInfo.class);
            	if(info != null){
            		RtdDevice device = DeviceManager.findRtdDevice(msg.getSourceSerialId(),msg.getSourceSystemId());
            		//如果是升级状态的RTD设备则不进行心跳
                    if(device == null){
                    	 CommandServer.responseAndroidHeart(device,null,2);
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
		                	 rtdModule.setBusinessType(Constants.RTD_TYPE_ANDROID);
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
                    CommandServer.responseAndroidHeart(device,null,1);
            	}
            }catch(Exception e){
                logger.error("[RESOURCE][SID-HEART]error", e);
                SocketJsonMessage temp = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"androidHeart", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, null);
                if(temp != null && channel.isAvailable()){
                    temp.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                    temp.getMessageHeader().setTargetObject(DeviceManager.buildAddress(msg.getSourceSystemId(), msg.getSourceSerialId(), msg.getSourceIndex()));
                    channel.write(temp);
                }
            }
            return true;
        }
        return false;
    }
}
