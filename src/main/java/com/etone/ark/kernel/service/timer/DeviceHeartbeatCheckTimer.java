/* DeviceHeartbeatCheckTimer.java	@date 2012-12-21
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.service.timer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.http.EventSidInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;


/**
 * <p>心跳超时检测的定时器任务</p>
 * 使用Java定时器重复检测设备心跳超时
 * @version 1.2
 * @author 张浩
 * 
 */
public class DeviceHeartbeatCheckTimer{
	
	static Logger logger = Logger.getLogger(DeviceHeartbeatCheckTimer.class);
	
	//Java定时器
	static Timer timer = new Timer(); 
	
	public static void start(){
		//按配置间隔重复检测
		timer.schedule(new DeviceHeartbeatCheckTask(),0,Constants.DEVICE_HEART_BEAT_TIMEOUT*1000);
		timer.schedule(new DevicePingCheckTask(),Constants.DEVICE_HEART_BEAT_TIMEOUT*1000,Constants.DEVICE_HEART_BEAT_TIMEOUT*1000);
	}

	public Object rtdModules;
	
	public static class DeviceHeartbeatCheckTask extends TimerTask{
        public void run() {
            try{
            	List<RtdDevice> rtds = DeviceManager.findRtdDevice();
            	EventRtdInfoChange event = new EventRtdInfoChange();
            	for(RtdDevice device : rtds){
            		 if(device.getHeartBeatTime()!= null && device.getStatus() != null && device.getStatus().equals(Constants.RTD_STATUS_NORMAL)){
                         //获取心跳间隔
                         long interval = getIntervalTime(device.getHeartBeatTime());
                         if(interval > Constants.DEVICE_HEART_BEAT_TIMEOUT){ //如果心跳间隔大于指定间隔
                             //进行连接检查,如果可达则修改状态为心跳超时
                             if(!isRemAddrReachable(device.getDeviceIp())){
                            	 device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                 logger.info("序列号："+device.getSerialNum()+"的RTD设备状态由正常变为链路故障");
                             }else{
                            	 device.setStatus(Constants.RTD_MODULE_STATUS_LINK_ERROR);
                                 logger.info("序列号："+device.getSerialNum()+"的RTD设备状态由正常变为心跳超时");
                             }
                             DeviceManager.updateRtdDevice(device);
                             List<RtdModuleDevice> rtdModules = DeviceManager.findRtdModuleDevice(device.getId(), device.getSystemId());
                             if(rtdModules != null){
                            	 for(RtdModuleDevice rtdModule : rtdModules){
                            		 rtdModule.setStatus(device.getStatus());
                            		 DeviceManager.updateRtdModuleDevice(rtdModule);
                            	 }
                             }
                            event.getLiRtd().add(device);
                         }
                     }
            	}
            	 //触发状态变更
                ClientManager.callbackEvent(event);
                
                List<SidDevice> sids = DeviceManager.findSidDevice();
                EventSidInfoChange sidChangeEvent = new EventSidInfoChange();
                for(SidDevice device : sids){
	           		 if(device.getHeartBeatTime()!= null && device.getStatus() != null && device.getStatus().equals(Constants.SID_STATUS_NORMAL)){
	                        //获取心跳间隔
	                        long interval = getIntervalTime(device.getHeartBeatTime());
	                        if(interval > Constants.DEVICE_HEART_BEAT_TIMEOUT){ //如果心跳间隔大于指定间隔
	                            //进行连接检查,如果可达则修改状态为心跳超时
	                            if(!isRemAddrReachable(device.getDeviceIp())){
	                           	 device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
	                                logger.info("序列号："+device.getSerialNum()+"的SID设备状态由正常变为链路故障");
	                            }else{
	                           	 device.setStatus(Constants.SID_SLOT_STATUS_LINK_ERROR);
	                                logger.info("序列号："+device.getSerialNum()+"的SID设备状态由正常变为心跳超时");
	                            }
	                            DeviceManager.updateSidDevice(device);
	                            List<SidSlotDevice> sidSlots = DeviceManager.findSidSlotDevice(device.getId(), device.getSystemId());
	                            if(sidSlots != null){
	                           	 for(SidSlotDevice sidSlot : sidSlots){
	                           		sidSlot.setStatus(device.getStatus());
	                           		DeviceManager.updateSidSlotDevice(sidSlot);
	                           	 }
	                            }
	                            sidChangeEvent.getLiSid().add(device);
	                        }
	                    }
	           	}
                //触发状态变更
                ClientManager.callbackEvent(sidChangeEvent);
            }catch(Exception e){
                logger.error("[RESOURCE][CHECK-DEVICE]error", e);
            }
        }
	}
	
	public static class  DevicePingCheckTask extends TimerTask{

        @Override
        public void run() {
            try{
            	List<RtdDevice> rtds = DeviceManager.findRtdDevice();
            	EventRtdInfoChange event = new EventRtdInfoChange();
            	for(RtdDevice device : rtds){
            		if(device.getStatus() != null && (device.getStatus().equals(Constants.DEVICE_STATUS_DISCONNECT)
                            || device.getStatus().equals(Constants.RTD_MODULE_STATUS_LINK_ERROR))){
            			boolean isChange = false;
                         if(!isRemAddrReachable(device.getDeviceIp())){
                        	 if(device.getStatus().equals(Constants.RTD_MODULE_STATUS_LINK_ERROR)){
                                 device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                 logger.info("序列号："+device.getSerialNum()+"的RTD设备状态由链接故障超时变为心跳超时");
                                 isChange = true;
                             }
                         }else{
                             if(device.getStatus().equals(Constants.DEVICE_STATUS_DISCONNECT)){
                                 device.setStatus(Constants.RTD_MODULE_STATUS_LINK_ERROR);
                                 logger.info("序列号："+device.getSerialNum()+"的RTD设备状态由心跳超时变为链路故障");
                                 isChange = true;
                             }
                         }
                         if(isChange){
	                         DeviceManager.updateRtdDevice(device);
	                         List<RtdModuleDevice> rtdModules = DeviceManager.findRtdModuleDevice(device.getId(), device.getSystemId());
	                         if(rtdModules != null){
	                        	 for(RtdModuleDevice rtdModule : rtdModules){
	                        		 rtdModule.setStatus(device.getStatus());
	                        		 DeviceManager.updateRtdModuleDevice(rtdModule);
	                        	 }
	                         }
	                         event.getLiRtd().add(device);
                         }
                     }
            	}
            	 //触发状态变更
                ClientManager.callbackEvent(event);
                
                List<SidDevice> sids = DeviceManager.findSidDevice();
                EventSidInfoChange sidChangeEvent = new EventSidInfoChange();
                for(SidDevice device : sids){
	           		 if(device.getStatus() != null && (device.getStatus().equals(Constants.DEVICE_STATUS_DISCONNECT)
                             || device.getStatus().equals(Constants.SID_SLOT_STATUS_LINK_ERROR))){
	           			 //进行连接检查,如果可达则修改状态为心跳超时
	           			 boolean isChange = false;
                         if(!isRemAddrReachable(device.getDeviceIp())){
                             if(device.getStatus().equals(Constants.SID_SLOT_STATUS_LINK_ERROR)){
                                 device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                 logger.info("序列号："+device.getSerialNum()+"的SID设备状态由链接故障超时变为心跳超时");
                                 isChange = true;
                             }
                         }else{
                             if(device.getStatus().equals(Constants.DEVICE_STATUS_DISCONNECT)){
                                 device.setStatus(Constants.SID_SLOT_STATUS_LINK_ERROR);
                                 logger.info("序列号："+device.getSerialNum()+"的SID设备状态由心跳超时变为链路故障");
                                 isChange = true;
                             }
                         }
                         if(isChange){
	                         DeviceManager.updateSidDevice(device);
	                         List<SidSlotDevice> sidSlots = DeviceManager.findSidSlotDevice(device.getId(), device.getSystemId());
	                         if(sidSlots != null){
	                        	 for(SidSlotDevice sidSlot : sidSlots){
	                        		sidSlot.setStatus(device.getStatus());
	                        		DeviceManager.updateSidSlotDevice(sidSlot);
	                        	 }
	                         }
	                         sidChangeEvent.getLiSid().add(device);
                         }
                    }
	           	}
                //触发状态变更
                ClientManager.callbackEvent(sidChangeEvent);
            }catch(Exception e){
                logger.error("[RESOURCE][DEVICE-PING]error", e);
            }
        }
	    
	}
	
	/**
	 * 获取实际心跳间隔时间
	 * @param heartBeatTime
	 * @return
	 */
	public static long getIntervalTime(Date heartBeatTime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(cal.getTime());
		long currentTimeMillis = cal.getTimeInMillis();
		cal.setTime(heartBeatTime);
		long lastHeartBeatTimeMillis = cal.getTimeInMillis();
		long interval = (currentTimeMillis - lastHeartBeatTimeMillis)/1000;
		return interval;
	}
	
	/**
	 * Ping操作
	 * 对方主机可达则返回：TRUE
	 * 对方主机不可达则返回：FALSE
	 * @param remAddr
	 * @return
	 */
	public static boolean isRemAddrReachable(String remAddr){
		int error = 1;
		for(int i=0;i<3;i++){
			try {
				InetAddress address = InetAddress.getByName(remAddr);
				if(!address.isReachable(5000)){
					error++;
				}
			} catch (UnknownHostException e) {
				error++;
			} catch (IOException e) {
				error++;
			}
		}
		if(error == 3){return false;}else{return true;}
	}
}
