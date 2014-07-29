package com.etone.ark.kernel.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.http.AbsHttpFilter;
import com.etone.ark.kernel.http.Client;
import com.etone.ark.kernel.service.ClientManager;
import com.sun.jersey.spi.container.ContainerRequest;

public class TokenFilter extends AbsHttpFilter{

    @Override
    public ContainerRequest execute(ContainerRequest request) {
        if("authorize".equals(request.getPath())){
            return request;
        }
        String token = this.getFirstParameter(request,"token");
        if(StringUtils.isNotEmpty(token)){
            Client client = ClientManager.getClient(token);
            if(client == null){
                Map<String,Object> temp = new HashMap<String,Object>();
                temp.put("timestamp", System.currentTimeMillis()/1000);
                temp.put("path",request.getPath());
                temp.put("errcode",ResultCode.ER_KERNEL_TOKEN_INVALID);
                temp.put("msg", "token无效");
                request = fromTo(request,"error/" +request.getMethod().toLowerCase(),temp);
            }
        }else{
            Map<String,Object> temp = new HashMap<String,Object>();
            temp.put("timestamp", System.currentTimeMillis()/1000);
            temp.put("path",request.getPath());
            temp.put("errcode",ResultCode.ER_KERNEL_TOKEN_INVALID);
            temp.put("msg", "token无效");
            request = fromTo(request,"error/" +request.getMethod().toLowerCase(),temp);
        }
        return request;
    }

}
