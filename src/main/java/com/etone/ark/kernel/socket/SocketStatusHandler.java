package com.etone.ark.kernel.socket;

import java.util.List;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.ISocketStatusHandler;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.http.EventSidInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;

public class SocketStatusHandler implements ISocketStatusHandler{
    
    static final Logger logger = Logger.getLogger(SocketStatusHandler.class);

    @Override
    public void open(SocketChannel ch) {
        DeviceManager.saveSocketChannel(ch);
    }

    @Override
    public boolean close(SocketChannel ch) {
    	DeviceManager.removeSocketChannel(ch.getId());
    	List<RtdDevice> rtdList = DeviceManager.findRtdDeviceBySocketChannelId(ch.getId());
    	if(rtdList != null){
    		EventRtdInfoChange event = new EventRtdInfoChange();
    		for(RtdDevice rtd : rtdList){
    			logger.info("RTD设备"+rtd.getId()+"通讯通道端口断开");
    			rtd.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
    			rtd.setSocketChannelId(null);
    			DeviceManager.updateRtdDevice(rtd);
    			List<RtdModuleDevice> rtdModules = DeviceManager.findRtdModuleDevice(rtd.getId(), rtd.getSystemId());
    			if(rtdModules != null){
    				for(RtdModuleDevice rtdModule : rtdModules){
    					rtdModule.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
    					rtdModule.setSocketChannelId(null);
    					DeviceManager.updateRtdModuleDevice(rtdModule);
    				}
    			}
    			event.getLiRtd().add(rtd);
    		}
    		ClientManager.callbackEvent(event);
    	}
    	List<SidDevice> sidList = DeviceManager.findSidDeviceBySocketChannelId(ch.getId());
    	if(sidList != null){
    		EventSidInfoChange event = new EventSidInfoChange();
    		for(SidDevice sid : sidList){
    			logger.info("SID设备"+sid.getId()+"通讯通道端口断开");
    			sid.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
    			sid.setSocketChannelId(null);
    			DeviceManager.updateSidDevice(sid);
    			List<SidSlotDevice> sidSlots = DeviceManager.findSidSlotDevice(sid.getId(), sid.getSystemId());
    			if(sidSlots != null){
    				for(SidSlotDevice sidSlot : sidSlots){
    					sidSlot.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
    					sidSlot.setSocketChannelId(null);
    					DeviceManager.updateSidSlotDevice(sidSlot);
    				}
    			}
    			event.getLiSid().add(sid);
    		}
    		ClientManager.callbackEvent(event);
    	}
    	
        return true;
    }

}
