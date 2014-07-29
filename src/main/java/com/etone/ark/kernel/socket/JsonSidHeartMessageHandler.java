/* JsonSidHeartMessageHandler.java	@date 2012-12-24
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
import com.etone.ark.communication.json.SidInfo;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventSidInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;

/**
 * <p>SID心跳消息处理类</p>
 * @version 1.2
 * @author 张浩 
 *
 */
public class JsonSidHeartMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
	
    static Logger logger = Logger.getLogger(JsonSidHeartMessageHandler.class);
    
	public JsonSidHeartMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if("sidHeart".equals(msg.getMessageHeader().getCommand())){
            try{
            	SidInfo info = msg.getContent(SidInfo.class);
            	if(info != null){
            		SidDevice device = DeviceManager.findSidDevice(msg.getSourceSerialId(),msg.getSourceSystemId());
            		//如果是升级状态的SID设备则不进行心跳
                    if(device == null || Constants.DEVICE_STATUS_UPDATE == device.getStatus()){
                        return true;
                    }
                    boolean isChange = false; //变更开关
                    if(info.getSidStatus() != device.getStatus()){
                        isChange = true;
                        device.setStatus(info.getSidStatus());
                    }
                    if(info.getSlotCount() != device.getSlotCount()){
                        isChange = true;
                        device.setSlotCount(info.getSlotCount());
                    }
                	device.setHeartBeatTime(new Date());
                	DeviceManager.updateSidDevice(device);
                    //检查SID卡槽
                    for(SidInfo.SlotInfo item:info.getSlots()){
                    	SidSlotDevice sidSlot =  DeviceManager.findSidSlotDevice(device.getId(), device.getSystemId(), item.getSlotNum());
                    	if(sidSlot != null){
                    		boolean isSlotChange = false;
                    		if(sidSlot.getStatus() != item.getSlotStatus()){
                    			isSlotChange = true;
                                sidSlot.setStatus(item.getSlotStatus());
                            }
                            if(!StringUtils.equals(sidSlot.getSlotImsi(), item.getSlotImsi())){
                            	isSlotChange = true;
                                sidSlot.setSlotImsi(item.getSlotImsi());
                            }
                            if(isSlotChange){
                            	isChange = true;
                            	DeviceManager.updateSidSlotDevice(sidSlot);
                            }
                    	}else{
                    		 sidSlot = new SidSlotDevice();
		                	 sidSlot.setId(device.getId());
		                	 sidSlot.setSlotImsi(item.getSlotImsi());
		                	 sidSlot.setSlotNum(item.getSlotNum());
		                	 sidSlot.setSocketChannelId(channel.getId());
		                	 sidSlot.setStatus(item.getSlotStatus());
		                	 sidSlot.setSystemId(device.getSystemId());
		                	 sidSlot.setWorkStatus(0);
		                	 DeviceManager.saveSidSlotDevice(sidSlot);
		                	 isChange = true;
                    	}
                    }
                    //设备变更
                    if(isChange){
                    	ClientManager.callbackEvent(new EventSidInfoChange(device));
                    }
                    CommandServer.responseSidHeart(device,null,1);
            	}
                /*final DeviceSid device = DeviceManager.getDeviceSid(msg.getMessageHeader().getSourceObject());
                //如果是升级状态的RTD设备则不进行心跳
                if(device == null || Constants.DEVICE_STATUS_UPDATE == device.getStatus()){
                    return true;
                }
                device.setHeartBeatTime(new Date(System.currentTimeMillis())); //设置心跳时间
                boolean isChange = false; //变更开关
                SidInfo info = msg.readObject(SidInfo.class);
                if(info != null){
                    if(info.getSidStatus() != device.getStatus()){
                        isChange = true;
                        device.setStatus(info.getSidStatus());
                    }
                    if(info.getSlotCount() != device.getSlotCount()){
                        isChange = true;
                        device.setSlotCount(info.getSlotCount());
                    }
                    //检查RTD模块
                    for(SidInfo.SlotInfo item:info.getSlots()){
                        String key = DeviceManager.buildAddress(device.getSystemId(), device.getSerialNum(), item.getSlotNum());
                        final DeviceSidSlot temp =  DeviceManager.getDeviceSidSlot(key);
                        if(temp != null){
                            temp.setHeartBeatTime(new Date(System.currentTimeMillis())); //设置心跳时间
                            if(temp.getStatus() != item.getSlotStatus()){
                                isChange = true;
                                temp.setStatus(item.getSlotStatus());
                            }
                            if(!StringUtils.equals(temp.getSlotImsi(), item.getSlotImsi())){
                                isChange = true;
                                temp.setSlotImsi(item.getSlotImsi());
                                DeviceManager.addInteriorDevice(temp);//缓存imsi关联关系
                            }
                        }
                    }
                    //设备变更
                    if(isChange){
                        //触发设备变更
                        ClientManager.callbackEvent(new EventSidInfoChange(device));
                    }
                    CommandServer.responseRtdHeart(device,null,1);
                }*/
            }catch(Exception e){
                logger.error("[RESOURCE][SID-HEART]error", e);
                SocketJsonMessage temp = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"sidHeart", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, null);
                if(temp != null){
                    if(channel.isAvailable()){
                        temp.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        temp.getMessageHeader().setTargetObject(DeviceManager.buildAddress(msg.getSourceSystemId(),msg.getSourceSerialId(),msg.getSourceIndex()));
                        channel.write(temp);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
