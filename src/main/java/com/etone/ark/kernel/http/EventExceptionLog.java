package com.etone.ark.kernel.http;

import java.util.Date;

public class EventExceptionLog {

protected String code;//异常类型  
    
    protected String msg; //异常内容描述
    
    protected Date time; //异常发生时间
    
    public EventExceptionLog(String code,String msg){
        this.code = code;
        this.msg = msg;
        this.time = new Date(System.currentTimeMillis());
    }
    
    public EventExceptionLog() {
        this.time = new Date(System.currentTimeMillis());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
