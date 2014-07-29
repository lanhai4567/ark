/* RTDResponse.java	@date 2013-2-23
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.StatusChangeService;
import com.etone.ark.communication.protocol.RTDInfoMessage;
import com.etone.ark.communication.protocol.RTDModuleInfoMessage;
import com.etone.ark.communication.protocol.ResponseMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;

/**
 * RTD设备相关的数据查询响应处理程序
 * 
 * @version 0.1
 * @author 张浩
 * 
 */
@Path("rtd")
public class RTDResource {

    static Logger logger = Logger.getLogger(RTDResource.class);

    /**
     * 查询所有RTD信息
     * 
     * @param token
     * @param timestamp
     * @return
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public RTDInfoMessage queryRtdList(@QueryParam("token") String token, @QueryParam("timestamp") String timestamp) {
        try {
            List<RTDInfoMessage.RTDInfo> data = new ArrayList<RTDInfoMessage.RTDInfo>();
            List<RtdDevice> list = DeviceManager.findRtdDevice();
            // 查询所有RTD设备信息
            for (RtdDevice rtd : list) {
                RTDInfoMessage.RTDInfo rtdInfo = new RTDInfoMessage.RTDInfo();
                rtdInfo.setDeviceIp(rtd.getDeviceIp());
                rtdInfo.setModel(rtd.getModel());
                rtdInfo.setModuleCount(rtd.getModuleCount());
                rtdInfo.setSoftVersion(rtd.getSoftVersion());
                rtdInfo.setRtdSerialNum(rtd.getSerialNum());
                // 动态信息
                rtdInfo.setLinkTime(DateUtils.convertDateToString(rtd.getLinkTime()));
                rtdInfo.setRtdStatus(StatusChangeService.getRtdStatus(rtd.getStatus()));
                if (rtd.getHeartBeatTime() != null) {
                    rtdInfo.setHeartBeatTime(DateUtils.convertDateToString(rtd.getHeartBeatTime()));
                }

                // 遍历RTD模块信息
                rtdInfo.setModule(new ArrayList<RTDInfoMessage.ModuleInfo>());
                List<RtdModuleDevice> rtdModules = DeviceManager.findRtdModuleDevice(rtd.getId(), rtd.getSystemId());
                for (RtdModuleDevice rtdModule : rtdModules) {
                    // 创建报文中的模块信息
                    RTDInfoMessage.ModuleInfo moduleInfo = new RTDInfoMessage.ModuleInfo();
                    moduleInfo.setImsi(rtdModule.getModuleImsi());
                    moduleInfo.setModuleSerialNum(DeviceManager.getDeviceID(rtdModule.getId(), rtdModule.getModuleNum()));
                    moduleInfo.setModuleType(rtdModule.getModuleType());
                    moduleInfo.setBusinessType(rtdModule.getBusinessType());
                    if(StringUtils.isNotBlank(rtdModule.getNetType())){
                        moduleInfo.setNetType(rtdModule.getNetType());
                    }else{
                        moduleInfo.setNetType(StatusChangeService.moduleType2NetType(rtdModule.getModuleType()));
                    }
                    // 动态信息
                    moduleInfo.setModuleStatus(StatusChangeService.getModuleStatus(rtdModule.getStatus()));
                    if (1 == rtdModule.getWorkStatus()) {
                        moduleInfo.setWorkStatus("BUSY");
                    }else{
                        moduleInfo.setWorkStatus("IDLE");
                    } 
                    // 加入到报文中
                    rtdInfo.getModule().add(moduleInfo);
                }
                data.add(rtdInfo);
            }
            RTDInfoMessage msg = new RTDInfoMessage(ResultCode.SUCCESS, "查询RTD设备成功");
            msg.setRtd(data);
            return msg;
        } catch (Exception e) {
            logger.error("查询RTD设备错误", e);
            return new RTDInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "查询RTD设备失败");
        }
    }

    /**
     * 查询可用的RTD模块，访问路径：rtd/module/list
     * 
     * @param token
     * @param timestamp
     * @param normal
     * @param busy
     * @return
     */
    @GET
    @Path("module/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RTDModuleInfoMessage queryModuleList(@QueryParam("token") String token, @QueryParam("timestamp") String timestamp) {
        try {
            List<RTDModuleInfoMessage.ModuleInfo> data = new ArrayList<RTDModuleInfoMessage.ModuleInfo>();
            // 查询所有模块信息
            List<RtdModuleDevice> list = DeviceManager.findRtdModuleDeviceByStatus(Constants.RTD_MODULE_STATUS_NORMAL);
            for (RtdModuleDevice rtdModule : list) {
            	RTDModuleInfoMessage.ModuleInfo moduleInfo = new RTDModuleInfoMessage.ModuleInfo();
                moduleInfo.setRtdSerialNum(rtdModule.getId());
                moduleInfo.setImsi(rtdModule.getModuleImsi());
                // 计算模块序列号
                moduleInfo.setModuleSerialNum(DeviceManager.getDeviceID(rtdModule.getId(), rtdModule.getModuleNum()));
                moduleInfo.setModuleType(rtdModule.getModuleType());
                moduleInfo.setBusinessType(rtdModule.getBusinessType());
                if(StringUtils.isNotBlank(rtdModule.getNetType())){
                    moduleInfo.setNetType(rtdModule.getNetType());
                }else{
                    moduleInfo.setNetType(StatusChangeService.moduleType2NetType(rtdModule.getModuleType()));
                }
                if (1 == rtdModule.getWorkStatus()) {
                    moduleInfo.setWorkStatus("BUSY");
                }else{
                    moduleInfo.setWorkStatus("IDLE");
                } 
                data.add(moduleInfo);
            }
            RTDModuleInfoMessage msg = new RTDModuleInfoMessage(ResultCode.SUCCESS, "查询可用的RTD模块成功");
            msg.setModule(data);
            return msg;
        } catch (Exception e) {
            logger.error("查询可用的RTD模块错误", e);
            return new RTDModuleInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR, "查询可用的RTD模块失败");
        }
    }

    /**
     * 升级RTD，访问路径：rtd/update
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
    public ResponseMessage updateRtd(@FormParam("token") String token, @FormParam("timestamp") String timestamp,
            @FormParam("version") String version, @FormParam("path") String path, @FormParam("device") String deviceJson) {
       /* try {
            if (StringUtils.isEmpty(path) || StringUtils.isEmpty(version) || StringUtils.isEmpty(deviceJson)) {
                return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR, "升级RTD失败，参数未空错误");
            }
            RTDInfoMessage msg = JsonUtils.fromJson(deviceJson, RTDInfoMessage.class);
            if (msg != null) {
                String resultMsg = ""; // 返回描述
                for (com.etone.ark.communication.protocol.RTDInfoMessage.RTDInfo rtdInfo : msg.getRtd()) {
                    // 获取设备
                    DeviceRtd device = DeviceManager.getDeviceRtd(DeviceManager.buildAddress(Constants.SYSTEM_ID, rtdInfo.getRtdSerialNum(),null));
                    if (device != null) {
                        boolean isFree = true;
                        device.setStatus(Constants.DEVICE_STATUS_UPDATE);
                        // 遍历修改模块状态
                        for (String key : device.getModules().values()) {
                            DeviceRtdModule module = DeviceManager.getDeviceRtdModule(key);
                            if (module != null) {
                                module.setStatus(Constants.DEVICE_STATUS_UPDATE);
                                if (module.getBusy()) {
                                    EventController.listen("rtdUpdate_"+key, new DeviceUpdateWaitFreeEventHandler(key,version,path), 20*60);
                                    isFree = false;
                                }
                            }
                        }
                        // 如果设备空闲则向设备发送升级命令
                        if (isFree) {
                            // 如果发送升级命令失败
                            if (!CommandServer.updateRTD(device, version, path)) {
                                device.setStatus(Constants.DEVICE_STATUS_DISCONNECT);
                                // 遍历修改模块状态
                                for (String key : device.getModules().values()) {
                                    DeviceRtdModule module = DeviceManager.getDeviceRtdModule(key);
                                    if (module != null) {
                                        module.setStatus(Constants.DEVICE_STATUS_DISCONNECT);// 将状态改为超时，有设备心跳恢复
                                    }
                                }
                                // 触发设备升级回调
                                ClientManager.callbackEvent(ParameterCode.DEVICE_TYPE_RTD, rtdInfo.getRtdSerialNum(),
                                        ResultCode.ER_KERNEL_DEVICE_UPDATE_FAIL, version, "向RTD设备发送升级消息失败");
                                resultMsg += ",\"" + rtdInfo.getRtdSerialNum() + "\":\"" + ResultCode.ER_KERNEL_DEVICE_UPDATE_FAIL + "\"";
                            } else {
                                resultMsg += ",\"" + rtdInfo.getRtdSerialNum() + "\":\"" + ResultCode.SUCCESS + "\"";
                            }
                            //TODO 触发设备变更
                            //ClientManager.callbackEvent(new EventRtdInfoChange(device));
                        }
                        // 如果设备不空闲则等待设备空闲后再发送升级命令
                        else {
                            resultMsg += ",\"" + rtdInfo.getRtdSerialNum() + "\":\"设备繁忙延迟发送升级命令\"";
                            //TODO 触发设备变更
                            //ClientManager.callbackEvent(new EventRtdInfoChange(device));
                        }
                    } else {
                        resultMsg += ",\"" + rtdInfo.getRtdSerialNum() + "\":\"" + ResultCode.ER_KERNEL_RESOURCE_NOT_EXIS + "\"";
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
