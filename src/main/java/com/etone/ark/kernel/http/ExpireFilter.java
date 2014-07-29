package com.etone.ark.kernel.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.http.AbsHttpFilter;
import com.sun.jersey.spi.container.ContainerRequest;

public class ExpireFilter extends AbsHttpFilter{

    @Override
    public ContainerRequest execute(ContainerRequest request) {
        if("favicon.ico".equals(request.getPath())){
            return request;
        }
        String timestamp = this.getFirstParameter(request,"timestamp");
        if(StringUtils.isNotEmpty(timestamp)){
            //如果时间戳大于5分钟则请求超时
            if(Math.abs((System.currentTimeMillis()/1000) - Long.valueOf(timestamp)) > 5 * 60){
                //请求超时
                Map<String,Object> temp = new HashMap<String,Object>();
                temp.put("timestamp", System.currentTimeMillis()/1000);
                temp.put("path",request.getPath());
                temp.put("errcode",ResultCode.ER_KERNEL_REQUEST_EXPIRED);
                temp.put("msg", "请求过期");
                request = fromTo(request,"error/" +request.getMethod().toLowerCase(),temp);
            }
        }else{
            //无效请求
            Map<String,Object> temp = new HashMap<String,Object>();
            temp.put("timestamp", System.currentTimeMillis()/1000);
            temp.put("path",request.getPath());
            temp.put("errcode",ResultCode.ER_KERNEL_PARAMETER_ERROR);
            temp.put("msg","请求不合法");
            request = fromTo(request,"error/" +request.getMethod().toLowerCase(),temp);
        }
        return request;
    }
}
