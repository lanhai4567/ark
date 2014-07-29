package com.etone.ark.kernel.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.Protocol;
import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.socket.ISocketMessage;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketByteMessageHeader;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.communication.socket.SocketJsonMessageHeader;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.task.ActionNodeHandler;

/**
 * 功能描述：
 * <p>
 * 版权所有：宜通世纪
 * <p>
 * 未经本公司许可，不得以任何方式复制或使用本程序任何部分
 * <p>
 * 
 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
 * @version 1.0.0
 * @since 1.0.0 create on: 2013-6-3
 */
public class CommandServer {
    
    static Logger logger = Logger.getLogger(CommandServer.class);
    
    //保存转发时的消息ID
    public final static Map<Integer,Integer> relayMessageIdStore = new HashMap<Integer,Integer>();

    /**
     * 功能描述：回复RTD登陆
     * 
     * @param channel
     * @param serialNum
     * @param messageId
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-6-3
     */
    public static boolean responseRtdLogin(RtdDevice rtd, Integer messageId,Integer error) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", error);
        params.put("systemId", Constants.SYSTEM_ID);
        params.put("systemTime", DateUtils.getCurrentTimeInMillisToString());
        params.put("ftpRootDir", Constants.FTP_PATH);
        params.put("heartBeatPeriod", Constants.DEVICE_HEART_BEAT_PERIOD);
        SocketChannel channel = DeviceManager.getSocketChannel(rtd.getSocketChannelId());
        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"rtdLogin", ISocketMessage.TYPE_RESPONSE, messageId, params);
            if (msg != null) {
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setCommandStatus(error);
                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setSourceLowAddress(0);
                        msg.getMessageHeader().setTargetHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setTargetLowAddress(rtd.getSerialNum());
                        
                        if(channel.write(msg)){
                            return true;
                        }
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdLogin", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, params);
            if(msg != null){
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtd.getSystemId(),rtd.getSerialNum(),null));

                        if(channel.write(msg)){
                            return true;
                        }
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return false;
    }
    
    /**
     * 功能描述：回复Android设备登陆
     * 
     * @param channel
     * @param serialNum
     * @param messageId
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-6-3
     */
    public static boolean responseAndroidLogin(RtdDevice rtd, Integer messageId,Integer error) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", error);
        params.put("systemId", Constants.SYSTEM_ID);
        params.put("systemTime", DateUtils.getCurrentTimeInMillisToString());
        params.put("ftpRootDir", Constants.FTP_PATH);
        params.put("heartBeatPeriod", Constants.DEVICE_HEART_BEAT_PERIOD);
        params.put("deviceIp", rtd.getDeviceIp());
        SocketChannel channel = DeviceManager.getSocketChannel(rtd.getSocketChannelId());
        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"androidLogin", ISocketMessage.TYPE_RESPONSE, messageId, params);
            if (msg != null) {
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setCommandStatus(error);
                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setSourceLowAddress(0);
                        msg.getMessageHeader().setTargetHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setTargetLowAddress(rtd.getSerialNum());
                        
                        if(channel.write(msg)){
                            return true;
                        }
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"androidLogin", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, params);
            if(msg != null){
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtd.getSystemId(),rtd.getSerialNum(),null));

                        if(channel.write(msg)){
                            return true;
                        }
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return false;
    }
    
    /**
     * 功能描述：回复Android心跳
     * 
     * @param channel
     * @param messageId
     * @param rtdInfo
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-5-30
     */
    public static boolean responseAndroidHeart(RtdDevice rtd, Integer messageId,Integer error) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("systemTime", DateUtils.getCurrentTimeInMillisToString());
        params.put("status", error);
        SocketChannel channel = DeviceManager.getSocketChannel(rtd.getSocketChannelId());
        if(channel != null){
	        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
	            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"androidHeart", SocketByteMessage.TYPE_RESPONSE, messageId, params);
	            if (msg != null) {
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setCommandStatus(error);
	                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
	                        msg.getMessageHeader().setSourceLowAddress(0);
	                        msg.getMessageHeader().setTargetHighAddress(Constants.SYSTEM_ID);
	                        msg.getMessageHeader().setTargetLowAddress(rtd.getSerialNum());
	                        
	                        if(channel.write(msg)){
	                            return true;
	                        }
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
	            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"androidHeart", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, params);
	            if(msg != null){
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
	                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtd.getSystemId(),rtd.getSerialNum(),null));

	                        if(channel.write(msg)){
	                            return true;
	                        }
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        }
        }
        return false;
    }

    /**
     * 功能描述：回复RTD心跳
     * 
     * @param channel
     * @param messageId
     * @param rtdInfo
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-5-30
     */
    public static boolean responseRtdHeart(RtdDevice rtd, Integer messageId,Integer error) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("systemTime", DateUtils.getCurrentTimeInMillisToString());
        params.put("softVersion", rtd.getSoftVersion());
        params.put("ftpRootDir", Constants.FTP_PATH);
        params.put("heartBeatPeriod", Constants.DEVICE_HEART_BEAT_PERIOD);
        SocketChannel channel = DeviceManager.getSocketChannel(rtd.getSocketChannelId());
        if(channel != null){
	        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
	            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"rtdHeart", SocketByteMessage.TYPE_RESPONSE, messageId, params);
	            if (msg != null) {
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setCommandStatus(error);
	                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
	                        msg.getMessageHeader().setSourceLowAddress(0);
	                        msg.getMessageHeader().setTargetHighAddress(Constants.SYSTEM_ID);
	                        msg.getMessageHeader().setTargetLowAddress(rtd.getSerialNum());
	                        
	                        if(channel.write(msg)){
	                            return true;
	                        }
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
	            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdHeart", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, params);
	            if(msg != null){
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
	                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtd.getSystemId(),rtd.getSerialNum(),null));

	                        if(channel.write(msg)){
	                            return true;
	                        }
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        }
        }
        return false;
    }
    
    /**
     * 回复SID设备登陆
     * @param sid
     * @param messageId
     * @param error
     * @return
     */
    public static boolean responseSidLogin(SidDevice sid, Integer messageId,Integer error) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", error.toString());
        params.put("systemID", Constants.SYSTEM_ID);
        params.put("systemTime", DateUtils.getCurrentTimeInMillisToString());
        params.put("heartBeatPeriod", Constants.DEVICE_HEART_BEAT_PERIOD.toString());
        SocketChannel channel = DeviceManager.getSocketChannel(sid.getSocketChannelId());
        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"sidLogin", ISocketMessage.TYPE_RESPONSE, messageId, params);
            if (msg != null) {
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setCommandStatus(error);
                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setSourceLowAddress(0);
                        msg.getMessageHeader().setTargetHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setTargetLowAddress(sid.getSerialNum());
                        
                        if(channel.write(msg)){
                            return true;
                        }
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"sidLogin", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, params);
            if(msg != null){
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(sid.getSystemId(),sid.getSerialNum(),null));

                        if(channel.write(msg)){
                            return true;
                        }
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return false;
    }
    
    /**
     * 回复SID设备心态
     * @param sid
     * @param messageId
     * @param error
     * @return
     */
    public static boolean responseSidHeart(SidDevice sid, Integer messageId,Integer error) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("systemTime", DateUtils.getCurrentTimeInMillisToString());
        params.put("heartBeatPeriod", Constants.DEVICE_HEART_BEAT_PERIOD);
        SocketChannel channel = DeviceManager.getSocketChannel(sid.getSocketChannelId());
        if(channel != null){
	        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
	            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"sidHeart", SocketByteMessage.TYPE_RESPONSE, messageId, params);
	            if (msg != null) {
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setCommandStatus(error);
	                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
	                        msg.getMessageHeader().setSourceLowAddress(0);
	                        msg.getMessageHeader().setTargetHighAddress(Constants.SYSTEM_ID);
	                        msg.getMessageHeader().setTargetLowAddress(sid.getSerialNum());
	                        
	                        if(channel.write(msg)){
	                            return true;
	                        }
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
	            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"sidHeart", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, params);
	            if(msg != null){
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
	                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(sid.getSystemId(),sid.getSerialNum(),null));
	
	                        if(channel.write(msg)){
	                            return true;
	                        }
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        }
        }
        return false;
    }

    /**
     * 功能描述：重置RTD模块 
     * 
     * guint32 TaskId; 
     * unsigned char RtdModuleNo; 
     * char SimMsisdn[21];
     * 
     * @param channel
     * @param rtdInfo
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-6-3
     */
    public static boolean requestRtdModuleRelease(RtdModuleDevice rtdModule) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", 0);
        params.put("simMsisdn", "");
        params.put("rtdModuleNo", rtdModule.getModuleNum());
        SocketChannel channel = DeviceManager.getSocketChannel(rtdModule.getSocketChannelId());
        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"rtdRelease", ISocketMessage.TYPE_REQUEST,
                    SocketMessageBuilder.getMessageId(), params);
            if (msg != null) {
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                        msg.getMessageHeader().setSourceLowAddress(0);
                        msg.getMessageHeader().setTargetHighAddress(rtdModule.getSystemId());
                        msg.getMessageHeader().setTargetLowAddress(rtdModule.getId());
                        return channel.write(msg);
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdRelease",ISocketMessage.TYPE_REQUEST, SocketJsonMessage.CONTENT_TYPE_JSON,params);
            if (msg != null) {
                try{
                    if(channel.isAvailable()){
                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtdModule.getSystemId(), rtdModule.getId(), rtdModule.getModuleNum()));
                        return channel.write(msg);
                    }
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return true;
    }
    
    /**
     * 重置RTD模块并接收返回信息
     * @param rtdModule
     * @param handler
     * @return
     */
    public static boolean requestRtdModuleRelease(RtdModuleDevice rtdModule,IEventHandler<?> handler) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", 0);
        params.put("simMsisdn", "");
        params.put("rtdModuleNo", rtdModule.getModuleNum());
        SocketChannel channel = DeviceManager.getSocketChannel(rtdModule.getSocketChannelId());
        if(channel != null){
	        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
	            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,"rtdRelease", ISocketMessage.TYPE_REQUEST,
	                    SocketMessageBuilder.getMessageId(), params);
	            if (msg != null) {
	                try{
	                    if(channel.isAvailable()){
	                        if(rtdModule.getSystemId() != Constants.SYSTEM_ID){
	                            msg.getMessageHeader().setSourceHighAddress(Constants.REMOTE_SYSTEM_ID);
	                        }else{
	                            msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
	                        }
	                        msg.getMessageHeader().setSourceLowAddress(0);
	                        msg.getMessageHeader().setTargetHighAddress(rtdModule.getSystemId());
	                        msg.getMessageHeader().setTargetLowAddress(rtdModule.getId());
	                        Protocol protocol = ProtocolManager.getProtocol(Constants.BYTE_PROTOCOL_VERSION,"rtdRelease");
	                        String ekey =  msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getTargetHighAddress() + "_" + DeviceManager.getDeviceID(msg.getMessageHeader().getTargetLowAddress(),msg.getActionHeader().getModuleNo());
	                        if(protocol.getResponse() != null){
	                            ekey =  protocol.getResponse().getCommand() + "_" + msg.getMessageHeader().getTargetHighAddress() + "_" + DeviceManager.getDeviceID(msg.getMessageHeader().getTargetLowAddress(),msg.getActionHeader().getModuleNo());
	                        }
	                        EventController.multiListen(ekey,handler,msg.getTimeout());
	                        return channel.write(msg);
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
	            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdRelease",ISocketMessage.TYPE_REQUEST, SocketJsonMessage.CONTENT_TYPE_JSON,params);
	            if (msg != null) {
	                try{
	                    if(channel.isAvailable()){
	                        msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
	                        msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtdModule.getSystemId(), rtdModule.getId(), rtdModule.getModuleNum()));
	                        String ekey =  msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getTargetObject();
	                        Protocol protocol = ProtocolManager.getProtocol(Constants.JSON_PROTOCOL_VERSION,"rtdRelease");
	                        if(protocol.getResponse() != null){
	                            ekey =  protocol.getResponse().getCommand() + "_" + msg.getMessageHeader().getTargetObject();
	                        }
	                        EventController.multiListen(ekey,handler,msg.getTimeout());
	                        return channel.write(msg);
	                    }
	                }catch(Exception e){
	                    logger.error(e.getMessage(),e);
	                }
	            }
	        }
        }
        return false;
    }
    
    /**
     * 重置资源
     * @param resource
     */
    public static void requestResourceRelease(final Resource resource){
        final String eventName = "unbind_" + resource.getImsi();
        if(DeviceManager.isCanReleaseResource(resource.getRtdSerialNum(), resource.getRtdModuleNum(), resource.getImsi())){
            // 日志输出
            logger.info("[RESOURCE][" + resource.getRtdSerialNum() + "]["+resource.getRtdModuleNum()+"]发送中断");
            EventController.multiListen(eventName, new IEventHandler<Object>(){
                @Override
                public void execute(Object obj) {}

                @Override
                public void timeout() {}
                
            },300L);
            RtdModuleDevice rtdModule = DeviceManager.findRtdModuleDevice(resource.getRtdSerialNum(), resource.getSystemId(), resource.getRtdModuleNum());
            if(rtdModule != null){
	            requestRtdModuleRelease(rtdModule,new IEventHandler<Object>(){
	
	                @Override
	                public void execute(Object obj) {
	                	DeviceManager.updateResourceBindStatus(resource.getRtdSerialNum(), resource.getRtdModuleNum(), resource.getImsi(),Resource.UNBIND);
	                    DeviceManager.removeResource(resource.getRtdSerialNum(), resource.getRtdModuleNum(), resource.getImsi());
	                    EventController.trigger(eventName);
	                }
	
	                @Override
	                public void timeout() {
	                	DeviceManager.updateResourceBindStatus(resource.getRtdSerialNum(), resource.getRtdModuleNum(), resource.getImsi(),Resource.UNBIND);
	                	DeviceManager.removeResource(resource.getRtdSerialNum(), resource.getRtdModuleNum(), resource.getImsi());
	                    EventController.trigger(eventName);
	                }
	                
	            });
            }
        }
    }
    
    /**
     * 升级RTD
     * @param rtd
     * @param version
     * @param path
     * @return
     */
    public static boolean updateRTD(RtdDevice rtd,String version,String path){
       /* Map<String,Object> params = new HashMap<String,Object>();
        params.put("version", version);
        params.put("path", path);
        if(Protocol.TYPE_JSON.equals(rtd.getChannel().getType())){
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"rtdUpdate", ISocketMessage.TYPE_REQUEST, SocketJsonMessage.CONTENT_TYPE_JSON, params);
            if(msg != null){
                msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(rtd.getSystemId(), rtd.getSerialNum(), null));
                EventController.listen("rtdUpdate" + msg.getMessageHeader().getTargetObject(), new IEventHandler<SocketJsonMessage>(){

                    @Override
                    public void execute(SocketJsonMessage msg) {
                        try{
                            String status = msg.readString("status");
                            String errorDescription = msg.readString("errorDescription");
                            //如果不成功修改状态，如果成功有设备重新登记
                            if(!"0".equals(status)){
                                DeviceRtd device = DeviceManager.getDeviceRtd(msg.getMessageHeader().getSourceObject());
                                device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                // 遍历修改模块状态
                                for (String key : device.getModules().values()) {
                                    DeviceRtdModule module = DeviceManager.getDeviceRtdModule(key);
                                    if (module != null) {
                                        module.setStatus(Constants.DEVICE_STATUS_DISCONNECT);// 将状态改为超时，有设备心跳恢复
                                    }
                                }
                                // TODO 触发设备变更
                                //ClientManager.callbackEvent(new EventRtdInfoChange(device));
                            }
                            //触发回调
                            ClientManager.callbackEvent(ParameterCode.DEVICE_TYPE_SID, rtd.getSerialNum(),status, rtd.getSoftVersion(), errorDescription);
                        }catch(Exception e){
                            logger.error(e.getMessage(),e);
                        }
                    }

                    @Override
                    public void timeout() {
                        DeviceRtd device = DeviceManager.getDeviceRtd(DeviceManager.buildAddress(rtd.getSystemId(), rtd.getSerialNum(), null));
                        device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                        // 遍历修改模块状态
                        for (String key : device.getModules().values()) {
                            DeviceRtdModule module = DeviceManager.getDeviceRtdModule(key);
                            if (module != null) {
                                module.setStatus(Constants.DEVICE_STATUS_DISCONNECT);// 将状态改为超时，有设备心跳恢复
                            }
                        }
                        // TODO 触发设备变更 
                        //ClientManager.callbackEvent(new EventRtdInfoChange(device));
                      //触发回调
                        ClientManager.callbackEvent(ParameterCode.DEVICE_TYPE_SID, rtd.getSerialNum(),"1", rtd.getSoftVersion(), "升级超时");
                    }
                
                }, msg.getTimeout());
                return true;
            }
            
        }*/
        return false;
    }
    
    /**
     * 升级SID
     * @param sid
     * @param version
     * @param path
     * @return
     */
    public static boolean updateSID(SidDevice sid,String version,String path){
        /*Map<String,Object> params = new HashMap<String,Object>();
        params.put("version", version);
        params.put("path", path);
        if(Protocol.TYPE_JSON.equals(sid.getChannel().getType())){
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"sidUpdate", ISocketMessage.TYPE_REQUEST, SocketJsonMessage.CONTENT_TYPE_JSON, params);
            if(msg != null){
                msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(sid.getSystemId(), sid.getSerialNum(), null));
                EventController.listen("sidUpdate" + msg.getMessageHeader().getTargetObject(), new IEventHandler<SocketJsonMessage>(){

                    @Override
                    public void execute(SocketJsonMessage msg) {
                        try{
                            String status = msg.readString("status");
                            String errorDescription = msg.readString("errorDescription");
                            //如果不成功修改状态，如果成功有设备重新登记
                            if(!"0".equals(status)){
                                DeviceSid device = DeviceManager.getDeviceSid(msg.getMessageHeader().getSourceObject());
                                device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                // 遍历修改模块状态
                                for (String key : device.getSlots().values()) {
                                    Device module = DeviceManager.getDevice(key);
                                    if (module != null) {
                                        module.setStatus(Constants.DEVICE_STATUS_DISCONNECT);// 将状态改为超时，有设备心跳恢复
                                    }
                                }
                                //TODO 触发设备变更
                                //ClientManager.callbackEvent(new EventSidInfoChange(device));
                            }
                            //触发回调
                            ClientManager.callbackEvent(ParameterCode.DEVICE_TYPE_SID, sid.getSerialNum(),status, sid.getSoftVersion(), errorDescription);
                        }catch(Exception e){
                            logger.error(e.getMessage(),e);
                        }
                    }

                    @Override
                    public void timeout() {
                        DeviceSid device = DeviceManager.getDeviceSid(DeviceManager.buildAddress(sid.getSystemId(), sid.getSerialNum(), null));
                        device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                        // 遍历修改模块状态
                        for (String key : device.getSlots().values()) {
                            Device module = DeviceManager.getDevice(key);
                            if (module != null) {
                                module.setStatus(Constants.DEVICE_STATUS_DISCONNECT);// 将状态改为超时，有设备心跳恢复
                            }
                        }
                        //TODO 触发设备变更
                        //ClientManager.callbackEvent(new EventSidInfoChange(device));
                        //触发回调
                        ClientManager.callbackEvent(ParameterCode.DEVICE_TYPE_SID, sid.getSerialNum(),"1", sid.getSoftVersion(), "升级超时");
                    }
                    
                }, msg.getTimeout());
                return true;
            }
            
        }*/
        return false;
    }
    
    /**
     * 功能描述：发送动作报文请求
     * 
     * @param actionType
     * @param resource
     * @param taskId
     * @param param
     * @param handler
     * @return 0:未知错误 1:向设备发送动作成功 2:构建报文失败 3:发送报文命令失败
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-6-3
     */
    public static int requestAction(String actionType, Resource resource, int taskId, Object param, ActionNodeHandler<?> handler) {
        SocketChannel channel = resource.getChannel();
        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,actionType, SocketByteMessage.TYPE_REQUEST,
                    SocketMessageBuilder.getMessageId(), taskId, resource.getRtdModuleNum(), param);
            if (msg == null) {
                return 2;
            }
            
            try{
                if(channel.isAvailable()){
                    if(resource.getSystemId() != Constants.SYSTEM_ID){
                        msg.getMessageHeader().setSourceHighAddress(Constants.REMOTE_SYSTEM_ID);
                    }else{
                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                    }
                    msg.getMessageHeader().setSourceLowAddress(0);
                    msg.getMessageHeader().setTargetHighAddress(resource.getSystemId());
                    msg.getMessageHeader().setTargetLowAddress(resource.getRtdSerialNum().intValue());
                    Protocol protocol = ProtocolManager.getProtocol(Constants.BYTE_PROTOCOL_VERSION,actionType);
                    String ekey =  msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getTargetHighAddress() + "_" + DeviceManager.getDeviceID(msg.getMessageHeader().getTargetLowAddress(),msg.getActionHeader().getModuleNo());
                    if(protocol.getResponse() != null){
                        ekey =  protocol.getResponse().getCommand() + "_" + msg.getMessageHeader().getTargetHighAddress() + "_" + DeviceManager.getDeviceID(msg.getMessageHeader().getTargetLowAddress(),msg.getActionHeader().getModuleNo());
                    }
                    EventController.listen(ekey,handler,msg.getTimeout());
                    if(channel.write(msg)){
                        return 1;
                    }
                }
                return 3;
            }catch(Exception e){
                logger.error(e.getMessage(),e);
            }
            return 0;
        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
            Protocol protocol = ProtocolManager.getProtocol(Constants.JSON_PROTOCOL_VERSION,actionType);
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(protocol,SocketByteMessage.TYPE_REQUEST,SocketJsonMessage.CONTENT_TYPE_JSON,param);
            if (msg == null) {
                return 2;
            }
      
            try{
                if(channel.isAvailable()){
                    msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                    msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(resource.getSystemId(),resource.getRtdSerialNum(), resource.getRtdModuleNum()));
                    String ekey =  msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getTargetObject();
                    if(protocol.getResponse() != null){
                        ekey =  protocol.getResponse().getCommand() + "_" + msg.getMessageHeader().getTargetObject();
                    }
                    EventController.listen(ekey,handler,msg.getTimeout());
                    if(channel.write(msg)){
                        logger.debug("[TASK][" + taskId+ "]["+handler.getNode().getId()+"][sendMsg]" + msg.getMsg());
                        return 1;
                    }
                }
                return 3;
            }catch(Exception e){
                logger.error(e.getMessage(),e);
            }
            return 0;
        }
        return -1;
    }
    
    public static int responseAction(String actionType, Resource resource, int taskId, Integer messageId,Object param) {
        SocketChannel channel = resource.getChannel();
        if (Protocol.TYPE_BYTE.equals(channel.getType())) {
            SocketByteMessage msg = SocketMessageBuilder.buildByteMessage(Constants.BYTE_PROTOCOL_VERSION,actionType, SocketByteMessage.TYPE_RESPONSE,
                    messageId, taskId, resource.getRtdModuleNum(), param);
            if (msg == null) {
                return 2;
            }
            
            try{
                if(channel.isAvailable()){
                    if(resource.getSystemId() != Constants.SYSTEM_ID){
                        msg.getMessageHeader().setSourceHighAddress(Constants.REMOTE_SYSTEM_ID);
                    }else{
                        msg.getMessageHeader().setSourceHighAddress(Constants.SYSTEM_ID);
                    }
                    msg.getMessageHeader().setSourceLowAddress(0);
                    msg.getMessageHeader().setTargetHighAddress(resource.getSystemId());
                    msg.getMessageHeader().setTargetLowAddress(resource.getRtdSerialNum().intValue());
                    
                    if(channel.write(msg)){
                        return 1;
                    }
                }
                return 3;
            }catch(Exception e){
                logger.error(e.getMessage(),e);
            }
        } else if (Protocol.TYPE_JSON.equals(channel.getType())) {
            SocketJsonMessage msg = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,actionType, SocketByteMessage.TYPE_RESPONSE,SocketJsonMessage.CONTENT_TYPE_JSON,param);
            if (msg == null) {
                return 2;
            }
            
            try{
                if(channel.isAvailable()){
                    msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
                    msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(resource.getSystemId(),resource.getRtdSerialNum(), resource.getRtdModuleNum()));
                    if(channel.write(msg)){
                        return 1;
                    }
                }
                return 3;
            }catch(Exception e){
                logger.error(e.getMessage(),e);
            }
        }
        return 0;
    }
    
    /**
     * 转发BYTE报文
     * @param channel
     * @param obj
     * @return
     */
    public static boolean relayByteMessage(SocketChannel channel,SocketByteMessage obj){
        try{
            if(Protocol.TYPE_JSON.equals(channel.getType())){
                relayMessageIdStore.put(obj.getMessageHeader().getSourceLowAddress(), obj.getMessageHeader().getMessageId());
                SocketJsonMessage msg = new SocketJsonMessage();
                msg.setMessageHeader(new SocketJsonMessageHeader());
                msg.getMessageHeader().setCommand(commandChange(obj.getMessageHeader().getCommand()));
                msg.getMessageHeader().setContentType(SocketJsonMessage.CONTENT_TYPE_BYTE);
                msg.getMessageHeader().setSourceObject(DeviceManager.buildAddress(obj.getMessageHeader().getSourceHighAddress(), obj.getMessageHeader().getSourceLowAddress(), null));
                msg.getMessageHeader().setTargetObject(DeviceManager.buildAddress(obj.getMessageHeader().getTargetHighAddress(), obj.getMessageHeader().getTargetLowAddress(), null));
                msg.setBody(obj.readBytesToString());
                return channel.write(msg);
            }else if(Protocol.TYPE_BYTE.equals(channel.getType())){
                return channel.write(obj); 
            }
        }catch(Exception e){
            
        }
        return false;
    }
    
    /**
     * 转发JSON报文
     * @param channel
     * @param obj
     * @return
     */
    public static boolean relayJsonMessage(SocketChannel channel,SocketJsonMessage obj){
        try{
        	if(channel != null){
	            if(Protocol.TYPE_BYTE.equals(channel.getType())){
	                Integer messageId = relayMessageIdStore.get(obj.getTargetSerialId());
	                SocketByteMessage msg = new SocketByteMessage();
	                msg.setMessageHeader(new SocketByteMessageHeader());
	                msg.getMessageHeader().setCommand(commandChange(obj.getMessageHeader().getCommand()));
	                msg.getMessageHeader().setMessageId(messageId);
	                msg.getMessageHeader().setSourceHighAddress(obj.getSourceSystemId());
	                msg.getMessageHeader().setSourceLowAddress(obj.getSourceSerialId());
	                msg.getMessageHeader().setTargetHighAddress(obj.getTargetSystemId());
	                msg.getMessageHeader().setTargetLowAddress(obj.getTargetSerialId());
	                for(int i =0;i<obj.getBody().length();i++){
	                    if((++i) < obj.getBody().length()){
	                        msg.writeUnsignedByte(char2int(obj.getBody().charAt((i-1)), obj.getBody().charAt(i)));
	                    }
	                }
	                return channel.write(msg);
	            }else{
	               return channel.write(obj);
	            }
        	}
        }catch(Exception e){
            logger.error("转发报文错误", e);
        }
        return false;
    }
    
    public static byte char_to_i(char ch) {
        byte b = 0;
        if (ch >= 'A' && ch <= 'F') {
            b = (byte)(10 + (ch - 'A'));
        } else if (ch >= 'a' && ch <= 'f') {
            b = (byte)(10 + (ch - 'a'));
        } else
            b = (byte)(ch - '0');

        return b;
    }
    
    public static int char2int(char ch1,char ch2){
        byte x = char_to_i(ch1);
        byte y = char_to_i(ch2);
        return (x*16 + y);
    }
    
    public static String commandChange(int commandId){
        String command = null;
        if(commandId == 0x0101){
            command = "Sim_Me_T1";
        }else if(commandId == 0x0102){
            command = "Sim_Me_T2";
        }else if(commandId == 0x0103){
            command = "Sim_Me_T3";
        }else if(commandId == 0x0104){
            command = "Sim_Me_T4";
        }else if(commandId == 0x0105){
            command = "Sim_Me_T5";
        }else if(commandId == 0x0109){
            command = "Sim_Me_T9";
        }else if(commandId == 0x010A){
            command = "Sim_Me_T10";
        }else if(commandId == 0x010B){
            command = "Sim_Me_T11";
        }else if(commandId == 0x010C){
            command = "Sim_Me_T12";
        }
        return command;
    }
    
    public static Integer commandChange(String command){
        Integer commandId = null;
        if(StringUtils.isEmpty(command)){
            return null;
        }
        if(command.equals("Sim_Me_T1")){
            commandId = 0x8101;
        }else if(command.equals("Sim_Me_T2")){
            commandId = 0x8102;
        }else if(command.equals("Sim_Me_T3")){
            commandId = 0x8103;
        }else if(command.equals("Sim_Me_T4")){
            commandId =  0x8104;
        }else if(command.equals("Sim_Me_T5")){
            commandId = 0x8105;
        }else if(command.equals("Sim_Me_T9")){
            commandId =0x8109;
        }else if(command.equals("Sim_Me_T10")){
            commandId = 0x810A;
        }else if(command.equals("Sim_Me_T11")){
            commandId = 0x810B;
        }else if(command.equals("Sim_Me_T12")){
            commandId = 0x810C;
        }
        return commandId;
    }

}
