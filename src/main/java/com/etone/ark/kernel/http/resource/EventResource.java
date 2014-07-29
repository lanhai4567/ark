/* EventResource.java	@date 2013-3-18
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.etone.ark.communication.JsonUtils;
import com.etone.ark.communication.ControlServiceEnum;
import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.protocol.EventRegisterInfoMessage;
import com.etone.ark.communication.protocol.EventRegisterInfoMessage.EventInfo;
import com.etone.ark.communication.protocol.ResponseMessage;
import com.etone.ark.kernel.http.ClientEventInfo;
import com.etone.ark.kernel.http.EventRtdInfoChange;
import com.etone.ark.kernel.http.EventSidInfoChange;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.DeviceManager;

/**
 * 回调事件注册和注销服务的响应程序
 * 
 * @version 0.1
 * @author 张浩
 * 
 */
@Path("event")
public class EventResource {

    static Logger logger = Logger.getLogger(EventResource.class);

    /**
     * 注册回调服务，请求路径：/event/register
     * 
     * @param register
     * @param timestamp
     * @param token
     * @return
     */
    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ResponseMessage registerFromForm(@FormParam("register") String register, @FormParam("timestamp") String timestamp,
            @FormParam("token") String token) {
        EventRegisterInfoMessage msg = JsonUtils.fromJson(register, EventRegisterInfoMessage.class);
        if (null != msg && msg.getEvent() != null) {
            try {
                boolean triggerRtdChangeEvent = false;
                boolean triggerSidChangeEvent = false;
                for (EventInfo info : msg.getEvent()) {
                    if (ControlServiceEnum.valueOf(info.getName()) != null) {
                        ClientEventInfo eventInfo = new ClientEventInfo();
                        eventInfo.setName(info.getName());
                        eventInfo.setCallback(info.getCallback());
                        eventInfo.setToken(token);
                        eventInfo.setParam(info.getParam());
                        ClientManager.addClientEvent(token, eventInfo);
                        // 如果注册设备变更回调,第一次将所有设备都触发一次回调
                        if (ControlServiceEnum.event_rtd_change.name().equals(eventInfo.getName())) {
                            triggerRtdChangeEvent = true;
                        } else if (ControlServiceEnum.event_sid_change.name().equals(eventInfo.getName())) {
                            triggerSidChangeEvent = true;
                        }

                    }
                }
                if (triggerRtdChangeEvent) {
                    ClientManager.callbackEvent(token,new EventRtdInfoChange(DeviceManager.findRtdDevice()));
                }
                if (triggerSidChangeEvent) {
                    ClientManager.callbackEvent(token, new EventSidInfoChange(DeviceManager.findSidDevice()));
                }
                return new ResponseMessage(ResultCode.SUCCESS, "注册事件成功");
            } catch (Exception e) {
                logger.error("注册回调服务错误", e);
            }
        } else {
            // 无效的参数
            return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR, "无效的参数");
        }

        return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "注册回调服务失败");
    }

    /**
     * 注销回调服务，请求路径：/event/unregister
     * 
     * @param unregister
     * @param timestamp
     * @param token
     * @return
     */
    @POST
    @Path("unregister")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ResponseMessage unregisteFromForm(@FormParam("unregister") String unregister, @FormParam("timestamp") String timestamp,
            @FormParam("token") String token) {
        EventRegisterInfoMessage msg = JsonUtils.fromJson(unregister, EventRegisterInfoMessage.class);
        if (null != msg && msg.getEvent() != null) {
            try {
                for(EventInfo info : msg.getEvent()){
                    if(ControlServiceEnum.valueOf(info.getName()) != null){
                        ClientManager.removeClientEvent(token, info.getName());
                    }
                }
                return new ResponseMessage(ResultCode.SUCCESS,"注销事件成功");
            } catch (Exception e) {
                logger.error("注销回调服务错误", e);
            }
        } else {
            // 无效的参数
            return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR, "无效的参数");
        }

        return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "注销回调服务失败");
    }
}
