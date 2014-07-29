package com.etone.ark.kernel.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.ControlServiceEnum;
import com.etone.ark.communication.JsonUtils;
import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.StatusChangeService;
import com.etone.ark.communication.http.HttpChannel;
import com.etone.ark.communication.http.HttpMessage;
import com.etone.ark.communication.http.HttpResponse;
import com.etone.ark.communication.protocol.DeviceUpdateResultMessage;
import com.etone.ark.communication.protocol.DeviceUpdateResultMessage.ResultInfo;
import com.etone.ark.communication.protocol.RTDInfoMessage;
import com.etone.ark.communication.protocol.SIDInfoMessage;
import com.etone.ark.communication.solver.IEventHandler;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.http.Client;
import com.etone.ark.kernel.http.ClientEventInfo;
import com.etone.ark.kernel.http.ClientInfo;
import com.etone.ark.kernel.http.EventActionFinish;
import com.etone.ark.kernel.http.EventExceptionLog;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.http.EventSidInfoChange;
import com.etone.ark.kernel.http.EventTaskFinish;
import com.etone.ark.kernel.http.HttpMessageBuilder;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;
import com.etone.ark.kernel.task.model.Variable;

/**
 * 客户端管理
 * 
 * @version 1.1
 * @author 张浩
 * 
 */
public class ClientManager {

    static Logger logger = Logger.getLogger(ClientManager.class);

    private final static ConcurrentMap<String, Client> STORE = new ConcurrentHashMap<String, Client>();

    /**
     * 初始化微内核后恢复初始化前的客户端信息
     */
    public static void initialBeforeRecoverClient() {
        List<ClientInfo> infoList = H2DataBaseServer.getClientInfoDao().findAll();
        if (infoList != null) {
            for (ClientInfo info : infoList) {
                Client client = new Client();
                client.setToken(info.getToken());
                client.setName(info.getName());
                client.setConnectTime(new Date(System.currentTimeMillis()));
                // 设置当前时间为心跳时间
                client.setHeartBeatTime(new Date(System.currentTimeMillis()));

                List<ClientEventInfo> eventInfos = H2DataBaseServer.getClientEventInfoDao().findByToken(client.getToken());
                if (eventInfos != null && eventInfos.size() > 0) {
                    for (ClientEventInfo eventInfo : eventInfos) {
                        // 默认访问为FORM表单提交
                        HttpChannel channel = new HttpChannel(eventInfo.getCallback(), HttpMessage.TYPE_POST, HttpMessage.DATA_TYPE_JSON);
                        client.getEvents().put(eventInfo.getName(), channel);
                        //Socket通讯时先启动的,所有在注册后进行一些设备上报
                        if (ControlServiceEnum.event_rtd_change.name().equals(eventInfo.getName())) {
                            ClientManager.callbackEvent(client.getToken(),new EventRtdInfoChange(DeviceManager.findRtdDevice()));
                        }else if (ControlServiceEnum.event_sid_change.name().equals(eventInfo.getName())) {
                            ClientManager.callbackEvent(client.getToken(), new EventSidInfoChange(DeviceManager.findSidDevice()));
                        }
                    }
                }

                STORE.put(client.getToken(), client);
            }
        }
    }

    /**
     * 触发动作执行完成回调事件
     * 
     * @param event
     *            事件数据对象
     */
    public static void callbackEvent(EventActionFinish event) {
        Client client = STORE.get(event.getToken());
        if (client != null) {
            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_action_result.name());
            if (channel != null) {
                HttpMessage msg = HttpMessageBuilder.buildMessage(event);
                HttpResponse response = channel.send(msg);
                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
                    logger.debug("[ACTION-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getActionType()
                            + "]{\"actionResult\":\"" + event.getActionResult() + "\"}");
                }else{
                    logger.debug("[ACTION-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getActionName() + "][error]");
                    sendCallbackErrorException(client,ControlServiceEnum.event_action_result.name());
                }
            }else{
                logger.debug("[ACTION-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getActionName() + "][no-event]");
                sendCallbackErrorException(client,ControlServiceEnum.event_action_result.name());
            }
        }else{
            logger.debug("[ACTION-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getActionName() + "][no-client]");
        }
    }

    /**
     * 触发测试业务执行完成回调事件
     * 
     * @param event
     *            事件数据对象
     */
    public static void callbackEvent(EventTaskFinish event) {
        TaskManager.delete(event.getTask().getId()); //移除缓存测试业务
        Client client = STORE.get(event.getToken());
        if (client != null) {
            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_task_result.name());
           if (channel != null) {
                if (event.getTask().getStructure().getResult() != null) {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    for (Variable variable : event.getTask().getStructure().getResult()) {
                    	String expression = ExpressionSolver.getExpression(variable.getValue());
                    	if(expression != null){
                    		try {
								result.put(variable.getCode(),event.getTask().script.evel(expression));
							} catch (ScriptException e) {
								e.printStackTrace();
							}
                    	}else{
                    		result.put(variable.getCode(), variable.getValue());
                    	}
                    	
                       /* Object temp = ExpressionSolver.eval(variable.getValue(), event.getTask().getHistory());
                        if (temp == null) {
                            if (ExpressionSolver.isExpression(variable.getValue())) {
                                result.put(variable.getCode(), "");
                            } else {
                                result.put(variable.getCode(), variable.getValue());
                            }
                        } else {
                            if (temp != null && temp.equals(Double.NaN)) {
                                temp = 0;
                            }
                            result.put(variable.getCode(), temp);
                        }*/
                    }
                    event.setTaskResult(null == result ? "" : JsonUtils.toJson(result));
                }

                HttpMessage msg = HttpMessageBuilder.buildMessage(event);
                HttpResponse response = channel.send(msg);
                TaskManager.delete(event.getTask().getId());
                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
                    logger.info("[TASK-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getTaskType()
                            + "]{\"taskResult\":\"" + event.getTaskResult() + "\"}");
                }else{
                   logger.info("[TASK-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getTaskType() + "][error]");
                   //发送微内核异常警告
                   sendCallbackErrorException(client,ControlServiceEnum.event_task_result.name());
                }
            }else{
                logger.info("[TASK-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getTaskType() + "][no-event]");
                //发送微内核异常警告
                sendCallbackErrorException(client,ControlServiceEnum.event_task_result.name());
          }
        }else{
            logger.info("[TASK-CALLBACK][" + event.getToken() + "][" + event.getTaskCode() + "][" + event.getTaskType() + "][no-client]");
        }
    }

    /**
     * 功能描述：微内核异常告警
     * 
     * @param event 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-6-21
     */
    public static void callbackEvent(EventExceptionLog event) {
        for (Client client : STORE.values()) {
            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_kernel_log.name());
            if (channel != null) {
                HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), event);
                HttpResponse response = channel.send(msg);
                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
                    logger.debug("[LOG-CALLBACK]["+client.getToken()+"]{\"error\":\""+event.getCode()+"\",\"msg\":\""+event.getMsg()+"\"}");
                }else{
                    logger.debug("[LOG-CALLBACK]["+client.getToken()+"][error]");
                }
            }else{
                logger.debug("[LOG-CALLBACK]["+client.getToken()+"][no-event]");
            }
        }
    }

    /**
     * 触发RTD信息变更回调事件 1. 查询所有客户端<br/>
     * 2. 查找所有注册的RTD信息变更事件<br/>
     * 3. 发送回调<br/>
     * 
     * @param event
     *            事件数据对象
     */
    public static void callbackEvent(EventRtdInfoChange event) {
        RTDInfoMessage rtdMsg = RtdChangeToMessageInfo(event);
        if(rtdMsg.getRtd().size() >0){
	        for (Client client : STORE.values()) {
	        	try{
		            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_rtd_change.name());
		            if (channel != null) {
		                HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), rtdMsg);
		                HttpResponse response = channel.send(msg);
		                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
		                    logger.debug("[RTD-CHANGE-CALLBACK]["+client.getToken()+"]{\"size\":"+event.getLiRtd().size()+"}");
		                }else{
		                    logger.debug("[RTD-CHANGE-CALLBACK]["+client.getToken()+"][error]");
		                    sendCallbackErrorException(client,ControlServiceEnum.event_rtd_change.name());
		                }
		            }else{
		                logger.debug("[RTD-CHANGE-CALLBACK]["+client.getToken()+"][no-event]");
		                sendCallbackErrorException(client,ControlServiceEnum.event_rtd_change.name());
		            }
	        	}catch(Exception e){
	        		logger.error("[RTD-CHANGE-CALLBACK]" + e.getMessage());
	        	}
	        }
        }
    }

    /**
     * 功能描述：给指定的客户端发送RTD信息变更
     * 
     * @param token
     * @param event 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-6-21
     */
    public static void callbackEvent(String token, EventRtdInfoChange event) {
        Client client = STORE.get(token);
        if (client != null) {
            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_rtd_change.name());
            if (channel != null) {
                RTDInfoMessage rtdMsg = RtdChangeToMessageInfo(event);
                HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), rtdMsg);
                HttpResponse response = channel.send(msg);
                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
                    logger.debug("[RTD-CHANGE-CALLBACK][" + token + "]{\"size\":"+event.getLiRtd().size()+"}");
                }else{
                    logger.debug("[RTD-CHANGE-CALLBACK][" + token + "][error]");
                    sendCallbackErrorException(client,ControlServiceEnum.event_rtd_change.name());
                }
            }else{
                logger.debug("[RTD-CHANGE-CALLBACK][" + token + "][no-event]");
                sendCallbackErrorException(client,ControlServiceEnum.event_rtd_change.name());
            }
        }else{
            logger.debug("[RTD-CHANGE-CALLBACK][" + token + "][no-client]");
        }
    }

    /**
     * 功能描述：将RTD对象转换成接口输出对象
     * 
     * @param event
     * @return 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-9-17
     */
    private static RTDInfoMessage RtdChangeToMessageInfo(EventRtdInfoChange event) {
        // 构建RTD设备信息报文
        RTDInfoMessage rtdMsg = new RTDInfoMessage();
        rtdMsg.setRtd(new ArrayList<RTDInfoMessage.RTDInfo>());
        for (RtdDevice rtd : event.getLiRtd()) {
            // 设置RTD信息
            RTDInfoMessage.RTDInfo rtdInfo = new RTDInfoMessage.RTDInfo(); // 创建报文的RTD信息
            // 基础信息
            rtdInfo.setRtdSerialNum(rtd.getSerialNum()); // RTD序列号
            rtdInfo.setDeviceIp(rtd.getDeviceIp());
            rtdInfo.setModel(rtd.getModel()); // RTD模式
            rtdInfo.setModuleCount(rtd.getModuleCount());
            rtdInfo.setSoftVersion(rtd.getSoftVersion());
            // 动态信息
            rtdInfo.setRtdStatus(StatusChangeService.getRtdStatus(rtd.getStatus()));
            //rtdInfo.setHeartBeatTime(DateUtils.convertDateToString(rtd.getHeartBeatTime())); // 心跳时间
            //rtdInfo.setLinkTime(DateUtils.convertDateToString(rtd.getLinkTime()));
            List<RtdModuleDevice> rtdModules = DeviceManager.findRtdModuleDevice(rtd.getId(), rtd.getSystemId());
            for (RtdModuleDevice rtdModule : rtdModules) {
                if (rtdModule != null) {
                    RTDInfoMessage.ModuleInfo moduleInfo = new RTDInfoMessage.ModuleInfo(); // 创建报文的RTD模块信息
                    moduleInfo.setModuleSerialNum(DeviceManager.getDeviceID(rtd.getSerialNum(), rtdModule.getModuleNum()));
                    moduleInfo.setImsi(rtdModule.getModuleImsi());
                    moduleInfo.setModuleType(rtdModule.getModuleType());
                    moduleInfo.setBusinessType(rtdModule.getBusinessType());
                    //网络制式，如果设备有上报则使用设备上报信息，如果没有这需要根据设备型号进行映射（Android设备必须上报网络制式）
                    if(StringUtils.isNotBlank(rtdModule.getNetType())){
                        moduleInfo.setNetType(rtdModule.getNetType());
                    }else{
                        moduleInfo.setNetType(StatusChangeService.moduleType2NetType(rtdModule.getModuleType()));
                    }
                    // 动态信息
                    moduleInfo.setModuleStatus(StatusChangeService.getModuleStatus(rtdModule.getStatus()));
                    if (1 == rtdModule.getWorkStatus()) {
                        moduleInfo.setWorkStatus("BUSY");
                    } else {
                        moduleInfo.setWorkStatus("IDLE");
                    }
                    // 添加到报文信息中
                    rtdInfo.getModule().add(moduleInfo);
                }
            }
            // 将RTD信息添加到报文中
            rtdMsg.getRtd().add(rtdInfo);
        }
        return rtdMsg;
    }

    /**
     * 触发SID信息变更回调事件 1. 查询所有客户端<br/>
     * 2. 查找所有注册的SID信息变更事件<br/>
     * 3. 发送回调<br/>
     * 
     * @param event
     *            事件数据对象
     */
    public static void callbackEvent(EventSidInfoChange event) {
        SIDInfoMessage sidMsg = SidChangeToMessageInfo(event);
        if(sidMsg.getSid().size()>0){
	        for (Client client : STORE.values()) {
	        	try{
		            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_sid_change.name());
		            if (channel != null) {
		                HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), sidMsg);
		                HttpResponse response = channel.send(msg);
		                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
		                    logger.debug("[SID-CHANGE-CALLBACK]["+client.getToken()+"]{\"size\":"+event.getLiSid().size()+"}");
		                }else{
		                    logger.debug("[SID-CHANGE-CALLBACK]["+client.getToken()+"][error]");
		                    sendCallbackErrorException(client,ControlServiceEnum.event_sid_change.name());
		                }
		            }else{
		                logger.debug("[SID-CHANGE-CALLBACK]["+client.getToken()+"][no-event]");
		                sendCallbackErrorException(client,ControlServiceEnum.event_sid_change.name());
		            }
	        	}catch(Exception e){
	        		logger.error("[SID-CHANGE-CALLBACK]" + e.getMessage());
	        	}
	        }
        }
    }

    /**
     * 功能描述：给指定的客户端发送SID设备变更
     * 
     * @param token
     * @param event 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-6-21
     */
    public static void callbackEvent(String token, EventSidInfoChange event) {
        Client client = STORE.get(token);
        if (client != null) {
            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_sid_change.name());
            if (channel != null) {
                SIDInfoMessage sidMsg = SidChangeToMessageInfo(event);
                HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), sidMsg);
                HttpResponse response = channel.send(msg);
                if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
                    logger.debug("[SID-CHANGE-CALLBACK][" + token + "]{\"size\":"+event.getLiSid().size()+"}");
                }else{
                    logger.debug("[SID-CHANGE-CALLBACK][" + token + "][error]");
                    sendCallbackErrorException(client,ControlServiceEnum.event_sid_change.name());
                }
            }else{
                logger.debug("[SID-CHANGE-CALLBACK][" + token + "][no-event]");
                sendCallbackErrorException(client,ControlServiceEnum.event_sid_change.name());
            }
        }else{
            logger.debug("[SID-CHANGE-CALLBACK][" + token + "][no-client]");
        }
    }

    private static SIDInfoMessage SidChangeToMessageInfo(EventSidInfoChange event) {
        // 构建SID设备变更信息报文
        SIDInfoMessage sidMsg = new SIDInfoMessage();
        sidMsg.setSid(new ArrayList<SIDInfoMessage.SIDInfo>());
        for (SidDevice sid : event.getLiSid()) {
            // 设置SID信息
            SIDInfoMessage.SIDInfo sidInfo = new SIDInfoMessage.SIDInfo(); // 创建报文的SID信息
            // 基础信息
            sidInfo.setSidSerialNum(sid.getSerialNum()); // SID序列号
            sidInfo.setDeviceIp(sid.getDeviceIp());
            sidInfo.setModel(sid.getModel()); // SID模式
            sidInfo.setSlotCount(sid.getSlotCount());
            sidInfo.setSoftVersion(sid.getSoftVersion());
            // 动态信息
            sidInfo.setSidStatus(StatusChangeService.getSidStatus(sid.getStatus()));
            //sidInfo.setHeartBeatTime(DateUtils.convertDateToString(sid.getHeartBeatTime())); // 心跳时间
            //sidInfo.setLinkTime(DateUtils.convertDateToString(sid.getLinkTime()));
            List<SidSlotDevice> sidSlots = DeviceManager.findSidSlotDevice(sid.getId(), sid.getSystemId());
            for (SidSlotDevice sidSlot : sidSlots) {
                if (sidSlot != null) {
                    SIDInfoMessage.SlotInfo slotInfo = new SIDInfoMessage.SlotInfo(); // 创建报文的SID卡槽信息
                    // 基础信息
                    slotInfo.setSlotNum(sidSlot.getSlotNum().toString());
                    slotInfo.setImsi(sidSlot.getSlotImsi());
                    // 动态信息
                    slotInfo.setSlotStatus(StatusChangeService.getSlotStatus(sidSlot.getStatus()));
                    // 添加到报文信息中
                    sidInfo.getSlot().add(slotInfo);
                }
            }
            // 将SID信息添加到报文中
            sidMsg.getSid().add(sidInfo);
        }
        return sidMsg;
    }

    /**
     * 设备升级回调事件
     * 
     * @param deviceType
     * @param serialNum
     * @param status
     * @param errorDescription
     */
    public static void callbackEvent(String deviceType, Integer serialNum, String status, String softVersion, String errorDescription) {
        if (StringUtils.isNotEmpty(deviceType) && serialNum != null && StringUtils.isNotEmpty(status)) {
            // 失败
            if ("0".equals(status)) {
                status = ResultCode.ER_KERNEL_DEVICE_UPDATE_FAIL;
            } else if ("1".equals(status)) {
                status = ResultCode.SUCCESS;
            }
            DeviceUpdateResultMessage info = new DeviceUpdateResultMessage();
            info.setDevice(new ArrayList<ResultInfo>());
            ResultInfo result = new ResultInfo();
            result.setDeviceType(deviceType);
            result.setSerialNum(serialNum);
            result.setStatus(status);
            result.setSoftVersion(softVersion);
            result.setUpdateTime(DateUtils.convertDateToString(new Date(System.currentTimeMillis())));
            info.getDevice().add(result);

            // 查找客户端
            for (Client client : STORE.values()) {
                HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_device_update.name());
                if (channel != null) {
                    HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), info);
                    HttpResponse response = channel.send(msg);
                    if (response.isSuccess() && response.getState() == HttpResponse.SUCCESS) {
                        logger.debug("[DEVICE-UPDATE-CALLBACK][" + client.getToken() + "]{\"size\":"+info.getDevice().size()+"}");
                    }else{
                        logger.debug("[DEVICE-UPDATE-CALLBACK][" + client.getToken() + "][error]");
                        sendCallbackErrorException(client,ControlServiceEnum.event_device_update.name());
                    }
                }else{
                    logger.debug("[DEVICE-UPDATE-CALLBACK][" + client.getToken() + "][no-event]");
                }
            }
        }
    }

    /**
     * 添加客户端
     * 
     * @param info
     */
    public static void addClient(ClientInfo info) {
        Client client = new Client();
        client.setToken(info.getToken());
        client.setName(info.getName());
        client.setConnectTime(new Date(System.currentTimeMillis()));
        client.setHeartBeatTime(new Date(System.currentTimeMillis()));
        STORE.put(client.getToken(), client);
        // 保存内存数据库
        H2DataBaseServer.getClientInfoDao().saveOrUpdate(info);
    }

    /**
     * 添加客户端事件
     * 
     * @param token
     * @param events
     */
    public static void addClientEvent(String token, List<ClientEventInfo> events) {
        if (events != null) {
            for (ClientEventInfo eventInfo : events) {
                addClientEvent(token, eventInfo);
            }
        }
    }

    /**
     * 添加客户端事件
     * 
     * @param client
     * @param eventInfo
     */
    public static void addClientEvent(String token, ClientEventInfo eventInfo) {
        final Client client = STORE.get(token);
        if (client != null && eventInfo != null) {
            // 默认访问为FORM表单提交
            HttpChannel channel = new HttpChannel(eventInfo.getCallback(), HttpMessage.TYPE_POST, HttpMessage.DATA_TYPE_JSON);
            client.getEvents().put(eventInfo.getName(), channel);
            // 保存内存数据库
            H2DataBaseServer.getClientEventInfoDao().saveOrUpdate(eventInfo);
        }
    }

    /**
     * 移除客户端事件
     * 
     * @param token
     * @param eventName
     */
    public static void removeClientEvent(String token, String eventName) {
        final Client client = STORE.get(token);
        if (client != null && StringUtils.isNotEmpty(eventName)) {
            client.getEvents().remove(eventName);
            // 删除内存数据
            H2DataBaseServer.getClientEventInfoDao().deleteByTokenAndName(token, eventName);
        }
    }

    /**
     * 移除客户端
     * 
     * @param token
     */
    public static void removeClient(String token) {
        final Client client = STORE.remove(token);
        if (client != null) {
            // 删除内存数据
            H2DataBaseServer.getClientInfoDao().delete(token);
            // 删除事件信息内存数据
            H2DataBaseServer.getClientEventInfoDao().deleteByToken(token);
        }
    }

    /**
     * 获取指定客户端对象
     * 
     * @param token
     * @return
     */
    public static Client getClient(String token) {
        return STORE.get(token);
    }

    /**
     * 查找所有客户端对象
     * 
     * @return
     */
    public static Collection<Client> findAllClient() {
        return STORE.values();
    }
    
    private static void sendCallbackErrorException(Client client,String eventName){
        HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_kernel_log.name());
        if (channel != null) {
            EventExceptionLog event = new EventExceptionLog();
            event.setCode(ResultCode.ER_KERNEL_EVENT_CALLBACK_FAIL);
            event.setMsg("事件回调失败，事件：" + eventName + "，回调地址：");
            HttpMessage msg = HttpMessageBuilder.buildMessage(client.getToken(), event);
            HttpResponse response = channel.send(msg);
            if (!response.isSuccess() || response.getState() != HttpResponse.SUCCESS) {
                logger.debug("[LOG-CALLBACK]["+client.getToken()+"]{\"error\":\""+event.getCode()+"\",\"msg\":\""+event.getMsg()+"\"}");
            }
        }
    }

    public static class HttpReceiver implements IEventHandler<HttpResponse> {

        private String eventName;
        private HttpResponse response;
        private String token;

        public HttpReceiver(String eventName, String token, HttpResponse response) {
            this.eventName = eventName;
            this.response = response;
            this.token = token;
        }

        @Override
        public void execute(HttpResponse obj) {
        }

        @Override
        public void timeout() {
            if (!response.isSuccess() || HttpResponse.SUCCESS != response.getState()) {
                // 触发异常日志
                Client client = STORE.get(token);
                if (client != null) {
                    EventExceptionLog event = new EventExceptionLog();
                    event.setCode(ResultCode.ER_KERNEL_EVENT_CALLBACK_FAIL);
                    event.setMsg("事件回调失败，事件：" + eventName);
                    HttpMessage msg = HttpMessageBuilder.buildMessage(token, event);
                    if (msg != null) {
                        if (msg != null) {
                            HttpChannel channel = client.getEvents().get(ControlServiceEnum.event_kernel_log.name());
                            if (channel != null) {
                                channel.send(msg);
                            }
                        }
                    }
                }
            }
        }

        public void cancel() {

        }

    }
}
