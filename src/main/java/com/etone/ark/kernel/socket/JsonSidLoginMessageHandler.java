/* JsonSidLoginMessageHandler.java	@date 2013-4-7
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪</p>
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel.socket;

import java.util.Date;

import org.apache.log4j.Logger;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.json.SidInfo;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventExceptionLog;
import com.etone.ark.kernel.http.EventSidInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;

/**
 * <p>SID登记消息处理程序</p>
 * @version 1.2
 * @author 张浩 
 *
 */
public class JsonSidLoginMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{

    static Logger logger = Logger.getLogger(JsonSidLoginMessageHandler.class);
    
	public JsonSidLoginMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if("sidLogin".equals(msg.getMessageHeader().getCommand())){
            try{
            	SidInfo info = msg.getContent(SidInfo.class);
            	if(info != null){
		    		SidDevice device = new SidDevice();
		    		device.setDeviceIp(info.getDeviceIp());
		    		device.setId(msg.getSourceSerialId());
		    		device.setModel(info.getModel());
		    		device.setSlotCount(info.getSlotCount());
		    		if(info.getSerialNum() != null){
		    			device.setSerialNum(info.getSerialNum());
		    		}else{
		    			device.setSerialNum(msg.getSourceSerialId());
		    		}
		    		device.setSocketChannelId(channel.getId());
		    		device.setSoftVersion(info.getSoftVersion());
		    		device.setStatus(info.getSidStatus());
		    		device.setSystemId(msg.getSourceSystemId());
		    		device.setVendor(info.getVendor());
		    		device.setWorkStatus(0);
		    		device.setLinkTime(new Date());
		    		device.setHeartBeatTime(new Date());
		    		int error = DeviceManager.saveSidDevice(device);
		    		if(error == 3){
		                 //发送微内核异常日志
		                 EventExceptionLog event = new EventExceptionLog();
		                 event.setCode(ResultCode.ER_KERNEL_DEVICE_ILLEGAL);
		                 event.setMsg("SID设备序列号重复/非法，设备IP："+device.getDeviceIp()+"，设备序列号："+device.getSerialNum());
		                 ClientManager.callbackEvent(event);
		                 CommandServer.responseSidLogin(device,null,0);
		                 logger.info("[RESOURCE]["+device.getSerialNum()+"]SID设备序列号重复/非法,IP:"+device.getDeviceIp());
		                 return true;
		    		}else if(error == 2){
		    			 EventExceptionLog event = new EventExceptionLog();
		                 event.setCode(ResultCode.ER_KERNEL_DEVICE_ILLEGAL);
		                 event.setMsg("SID设备参数错误，设备IP："+device.getDeviceIp()+"，设备序列号："+device.getSerialNum());
		                 ClientManager.callbackEvent(event);
		                 CommandServer.responseSidLogin(device,null,0);
		                 logger.info("[RESOURCE]["+device.getSerialNum()+"]SID设备参数错误,IP:"+device.getDeviceIp());
		                 return true;
		    		}else if(error == 1){
		                 if(CommandServer.responseSidLogin(device,null,1)){
		                	 //获取SID卡槽信息
			                 for(SidInfo.SlotInfo item:info.getSlots()){
			                	 SidSlotDevice sidSlot = new SidSlotDevice();
			                	 sidSlot.setId(device.getId());
			                	 sidSlot.setSlotImsi(item.getSlotImsi());
			                	 sidSlot.setSlotNum(item.getSlotNum());
			                	 sidSlot.setSocketChannelId(channel.getId());
			                	 sidSlot.setStatus(item.getSlotStatus());
			                	 sidSlot.setSystemId(device.getSystemId());
			                	 sidSlot.setWorkStatus(0);
			                	 DeviceManager.saveSidSlotDevice(sidSlot);
			                 }
			                logger.info("[RESOURCE]["+device.getSerialNum()+"]SID设备登记,IP："+device.getDeviceIp());
			                //触发设备变更
			                ClientManager.callbackEvent(new EventSidInfoChange(device));
		                 }
		    		}else{
		    			 CommandServer.responseSidLogin(device,null,0);
		    		}
            	}
            	
               /* SidInfo info = msg.readObject(SidInfo.class);
                if(info != null){
                    DeviceSid sid = new DeviceSid();
                    sid.setChannel(channel);
                    sid.setDeviceIp(info.getDeviceIp());
                    sid.setModel(info.getModel());
                    sid.setSlotCount(info.getSlotCount());
                    sid.setSoftVersion(info.getSoftVersion());
                    sid.setVendor(info.getVendor());
                    if(info.getSidStatus() != null){
                        sid.setStatus(info.getSidStatus());
                    }else{
                        sid.setStatus(0);
                    }
                    
                    sid.setSystemId(msg.getSourceSystemId());
                    sid.setSerialNum(msg.getSourceSerialId());
                    sid.setHeartBeatTime(new Date(System.currentTimeMillis()));
                    sid.setLinkTime(new Date(System.currentTimeMillis()));
                    
                    //缓存SID设备信息
                    int error = DeviceManager.addDevice(sid);
                    if(error == 2){
                        //发送微内核异常日志
                        EventExceptionLog event = new EventExceptionLog();
                        event.setCode(ResultCode.ER_KERNEL_DEVICE_ILLEGAL);
                        event.setMsg("设备序列号重复/非法，设备IP："+sid.getDeviceIp()+"，设备序列号："+sid.getSerialNum());
                        ClientManager.callbackEvent(event);
                        CommandServer.responseSidLogin(sid,null,0);
                        logger.info("[RESOURCE]["+sid.getSerialNum()+"]SID设备序列号重复/非法,IP:"+sid.getDeviceIp());
                        return true;
                    }else{ 
                        //获取SID卡槽信息
                        for(SidInfo.SlotInfo item:info.getSlots()){
                            DeviceSidSlot slot = new DeviceSidSlot();
                            slot.setChannel(channel);
                            slot.setDeviceIp(sid.getDeviceIp());
                            slot.setSlotImsi(item.getSlotImsi());
                            slot.setSlotNum(item.getSlotNum());
                            if(item.getSlotStatus() != null){
                                slot.setStatus(item.getSlotStatus());
                            }else{
                                slot.setStatus(0);
                            }
                            slot.setSystemId(sid.getSystemId());
                            slot.setSerialNum(sid.getSerialNum());
                            slot.setHeartBeatTime(new Date(System.currentTimeMillis()));
                            slot.setLinkTime(new Date(System.currentTimeMillis()));
                            sid.getSlots().put(slot.getIndex(),DeviceManager.buildAddress(sid.getSystemId(), sid.getSerialNum(), slot.getIndex()));
                            DeviceManager.addInteriorDevice(slot);
                        }
                    }
                    
                    if(CommandServer.responseSidLogin(sid,null,1)){
                        logger.info("[RESOURCE]["+sid.getSerialNum()+"]SID设备登记，IP："+sid.getDeviceIp());
                        //触发设备变更
                        ClientManager.callbackEvent(new EventSidInfoChange(sid));
                    }
                }*/
            }catch(Exception e){
                logger.error("[RESOURCE][SID-LOGIN]error", e);
                SocketJsonMessage temp = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION, "sidLogin", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, null);
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
