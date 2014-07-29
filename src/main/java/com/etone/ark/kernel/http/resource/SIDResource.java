/* SIDResponse.java	@date 2013-2-23
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.StatusChangeService;
import com.etone.ark.communication.protocol.ResponseMessage;
import com.etone.ark.communication.protocol.SIDInfoMessage;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;

/**
 * SID设备相关请求的响应程序
 * 
 * @version 0.1
 * @author 张浩
 * 
 */
@Path("sid")
public class SIDResource {

    static Logger logger = Logger.getLogger(SIDResource.class);

    /**
     * 查询所有注册的SID设备，访问路径：sid/list
     * 
     * @param token
     *            请求的客户端标识
     * @param timestamp
     *            请求时间戳
     * @return 返回SID设备的JSON对象
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public SIDInfoMessage querySidList(@QueryParam("token") String token, @QueryParam("timestamp") String timestamp) {
        try {
            List<SIDInfoMessage.SIDInfo> data = new ArrayList<SIDInfoMessage.SIDInfo>();
            List<SidDevice> list =  DeviceManager.findSidDevice();
            SIDInfoMessage msg = new SIDInfoMessage(ResultCode.SUCCESS, "查询SID设备成功");
            for (SidDevice sid : list) {
                SIDInfoMessage.SIDInfo sidInfo = new SIDInfoMessage.SIDInfo();
                sidInfo.setDeviceIp(sid.getDeviceIp());
                sidInfo.setModel(sid.getModel());
                sidInfo.setSidSerialNum(sid.getSerialNum());
                sidInfo.setSlotCount(sid.getSlotCount());
                sidInfo.setSoftVersion(sid.getSoftVersion());
                // SID设备状态
                sidInfo.setSidStatus(StatusChangeService.getSidStatus(sid.getStatus()));
                sidInfo.setLinkTime(DateUtils.convertDateToString(sid.getLinkTime()));
                if (sid.getHeartBeatTime() != null) {
                    sidInfo.setHeartBeatTime(DateUtils.convertDateToString(sid.getHeartBeatTime()));
                }

                // 遍历SID卡槽设备
                sidInfo.setSlot(new ArrayList<SIDInfoMessage.SlotInfo>());
                List<SidSlotDevice> sidSlots = DeviceManager.findSidSlotDevice(sid.getId(), sid.getSystemId());
                if(sidSlots != null){
	                for (SidSlotDevice slot : sidSlots) {
                        SIDInfoMessage.SlotInfo slotInfo = new SIDInfoMessage.SlotInfo();
                        slotInfo.setImsi(slot.getSlotImsi());
                        slotInfo.setSlotNum(slot.getSlotNum().toString());
                        slotInfo.setSlotStatus(StatusChangeService.getSlotStatus(slot.getStatus()));
                        sidInfo.getSlot().add(slotInfo);
	                }
                }
                data.add(sidInfo);
                msg.setSid(data);
            }
            return msg;

        } catch (Exception e) {
            logger.error("查询SID设备错误", e);
        }

        return new SIDInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "查询SID设备失败");
    }

    /**
     * 更新SID
     * 
     * @param token
     * @param timestamp
     * @param version
     * @param path
     * @param deviceJson
     * @return
     */
    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseMessage updateSid(@FormParam("token") String token, @FormParam("timestamp") String timestamp,
            @FormParam("version") String version, @FormParam("path") String path, @FormParam("device") String deviceJson) {
        /*try {
            if (StringUtils.isEmpty(path) || StringUtils.isEmpty(version) || StringUtils.isEmpty(deviceJson)) {
                return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR, "升级SID失败，参数未空错误");
            }
            SIDInfoMessage msg = JsonUtils.fromJson(deviceJson, SIDInfoMessage.class);
            if (msg != null) {
                String resultMsg = ""; // 返回描述
                for (com.etone.ark.communication.protocol.SIDInfoMessage.SIDInfo sidInfo : msg.getSid()) {
                    // 获取设备
                    DeviceSid device = DeviceManager.getDeviceSid(DeviceManager.buildAddress(Constants.SYSTEM_ID, sidInfo.getSidSerialNum(),null));
                    if (device != null) {
                        boolean isFree = true;
                        device.setStatus(Constants.DEVICE_STATUS_UPDATE);
                        // 遍历修改模块状态
                        for (String key : device.getSlots().values()) {
                            Device temp = DeviceManager.getDevice(key);
                            if (temp != null) {
                                temp.setStatus(Constants.DEVICE_STATUS_UPDATE);
                                if (temp.getBusy()) {
                                    EventController.listen("sidUpdate_"+key, new DeviceUpdateWaitFreeEventHandler(key,version,path), 20*60);
                                    isFree = false;
                                }
                            }
                        }
                        // 如果设备空闲则向设备发送升级命令
                        if (isFree) {
                            // 如果发送升级命令失败
                            if (!CommandServer.updateSID(device, version, path)) {
                                device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                // 遍历修改模块状态
                                for (String key : device.getSlots().values()) {
                                    Device temp = DeviceManager.getDevice(key);
                                    if (temp != null) {
                                        temp.setStatus(Constants.DEVICE_STATUS_DISCONNECT);// 将状态改为超时，有设备心跳恢复
                                    }
                                }
                                // 触发设备升级回调
                                ClientManager.callbackEvent(ParameterCode.DEVICE_TYPE_RTD, sidInfo.getSidSerialNum(),
                                        ResultCode.ER_KERNEL_DEVICE_UPDATE_FAIL, version, "向SID设备发送升级消息失败");
                                resultMsg += ",\"" + sidInfo.getSidSerialNum() + "\":\"" + ResultCode.ER_KERNEL_DEVICE_UPDATE_FAIL + "\"";
                            } else {
                                resultMsg += ",\"" + sidInfo.getSidSerialNum() + "\":\"" + ResultCode.SUCCESS + "\"";
                            }
                            //TODO 触发设备变更
                            //ClientManager.callbackEvent(new EventSidInfoChange(device));
                        }
                        // 如果设备不空闲则等待设备空闲后再发送升级命令
                        else {
                            resultMsg += ",\"" + sidInfo.getSidSerialNum() + "\":\"设备繁忙延迟发送升级命令\"";
                            // TODO 触发设备变更
                            //ClientManager.callbackEvent(new EventSidInfoChange(device));
                        }
                    } else {
                        resultMsg += ",\"" + sidInfo.getSidSerialNum() + "\":\"" + ResultCode.ER_KERNEL_RESOURCE_NOT_EXIS + "\"";
                    }
                }
                if (resultMsg.length() > 0) {
                    resultMsg = "[" + resultMsg.substring(1) + "]";
                    logger.info("[HTTP][UPDATE-RTD][SUCCESS]" + resultMsg);
                    return new ResponseMessage(ResultCode.SUCCESS, resultMsg);
                } else {
                    return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "微内核内部错误:未触发升级功能");
                }
            } else {
                return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR, "升级RTD失败，device参数值错误");
            }
        } catch (Exception e) {
            logger.error("升级RTD错误", e);
            return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "升级RTD失败");
        }*/
    	return null;
    }
}
