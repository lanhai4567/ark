package com.etone.ark.kernel.http;

import javax.script.ScriptException;

import com.etone.ark.communication.DateUtils;
import com.etone.ark.communication.JsonUtils;
import com.etone.ark.communication.http.HttpMessage;
import com.etone.ark.communication.protocol.DeviceUpdateResultMessage;
import com.etone.ark.communication.protocol.RTDInfoMessage;
import com.etone.ark.communication.protocol.RTDModuleNetinfo;
import com.etone.ark.communication.protocol.SIDInfoMessage;
import com.sun.jersey.api.representation.Form;

public class HttpMessageBuilder {

    /**
     * 构建测试动作执行完成事件回调报文
     * @param event 动作执行完成事件
     * @return  事件回调报文
     */
    public static HttpMessage buildMessage(EventActionFinish event){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        Form form = new Form();
        form.add("token", event.getToken());
        form.add("code", event.getTaskCode());
        form.add("moduleSerialNum", event.getModuleSerialNum());
        form.add("imsi", event.getImsi());
        form.add("actionType", event.getActionType());
        form.add("actionName", event.getActionName());
        form.add("actionResult", event.getActionResult());
        form.add("beginTime", DateUtils.convertDateToString(event.getBeginDate()));
        form.add("endTime", DateUtils.convertDateToString(event.getEndDate()));
        form.add("timestamp", System.currentTimeMillis()/1000);
        //小区信息
        if(event.getActionType().equals("checkRegister") || event.getActionType().equals("register")){
        	try {
				form.add("cellInfoMessage", event.getTask().script.get(event.getNode().getId()+".cellInfo"));
				form.add("cellSignal",event.getTask().script.get(event.getNode().getId()+".cellSignal"));
			} catch (ScriptException e) {
				e.printStackTrace();
			}
        }
        
        msg.setForm(form);
        return msg;
    }
    
    /**
     * 构建测试业务执行完成事件回调报文
     * @param event
     * @return
     */
    public static HttpMessage buildMessage(EventTaskFinish event){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        Form form = new Form();
        form.add("token", event.getToken());
        form.add("timestamp", System.currentTimeMillis()/1000);
        form.add("code", event.getTaskCode());
        form.add("type", event.getTaskType());
        form.add("beginTime", DateUtils.convertDateToString(event.getBeginDate()));
        form.add("endTime", DateUtils.convertDateToString(event.getEndDate()));
        form.add("taskResult", event.getTaskResult());
        msg.setForm(form);
        return msg;
    }
    
    /**
     * 构建异常日志事件回调报文
     * @param event
     * @return
     */
    public static HttpMessage buildMessage(String token,EventExceptionLog event){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        Form form = new Form();
        form.add("code", event.getCode());
        form.add("msg", event.getMsg());
        form.add("time", DateUtils.convertDateToString(event.getTime()));
        form.add("token", token);
        form.add("timestamp", System.currentTimeMillis()/1000);
        msg.setForm(form);
        return msg;
    }
    
    /**
     * 构建RTD设备信息变更事件回调报文
     * @param event
     * @return
     */
    public static HttpMessage buildMessage(String token,RTDInfoMessage info){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        //组建表单
        Form form = new Form();
        form.add("token", token);
        form.add("timestamp", System.currentTimeMillis()/1000);
        form.add("info", JsonUtils.toJson(info));
        msg.setForm(form);
        return msg;
    }
    
    /**
     * 构建SID设备信息变更事件回调报文
     * @param event
     * @return
     */
    public static HttpMessage buildMessage(String token,SIDInfoMessage info){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        //组建表单
        Form form = new Form();
        form.add("token", token);
        form.add("timestamp", System.currentTimeMillis()/1000);
        form.add("info", JsonUtils.toJson(info));
        msg.setForm(form);
        return msg;
    }
    
    /**
     * 构建设备升级结果事件回调报文
     * @param token
     * @param info
     * @return
     */
    public static HttpMessage buildMessage(String token,DeviceUpdateResultMessage info){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        //组建表单
        Form form = new Form();
        form.add("token", token);
        form.add("timestamp", System.currentTimeMillis()/1000);
        form.add("info", JsonUtils.toJson(info));
        msg.setForm(form);
        return msg;
    }
    
    /**
     * 构建RTD模块网络信息事件回调报文
     * @param event
     * @return
     */
    public static HttpMessage buildMessage(String token,RTDModuleNetinfo info){
        HttpMessage msg = new HttpMessage();
        msg.setDateType(HttpMessage.DATA_TYPE_JSON);
        msg.setType(HttpMessage.TYPE_POST);
        //组建表单
        Form form = new Form();
        form.add("token", token);
        form.add("timestamp", System.currentTimeMillis()/1000);
        form.add("rtdSerialNum", info.getRtdSerialNum());
        form.add("moduleSerialNum", info.getModuleSerialNum());
        form.add("netType", info.getNetType());
        form.add("moduleType", info.getModuleType());
        form.add("netInfo", info.getNetInfo());
        msg.setForm(form);
        return msg;
    }
}
