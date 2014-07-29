/* AuthorizeResource.java	@date 2013-3-18
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http.resource;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import com.etone.ark.communication.protocol.AuthorizeResponseMessage;
import com.etone.ark.communication.protocol.ClientInfoMessage;
import com.etone.ark.communication.protocol.ResponseMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.http.ClientInfo;
import com.etone.ark.kernel.http.Client;
import com.etone.ark.kernel.service.ClientManager;

/**
 * 微内核鉴权请求处理程序
 * 
 * @author 张浩
 *
 */
@Path("")
public class AuthorizeResource {
	
	static Logger logger = Logger.getLogger(AuthorizeResource.class);
	
	/**
	 * 鉴权服务处理 ，访问路径：/authorize
	 * @param uri
	 * @return
	 */
	@GET
	@Path("authorize")
	@Produces(MediaType.APPLICATION_JSON)
	public AuthorizeResponseMessage authorize(@QueryParam("sign") String sign,
			@QueryParam("timestamp") String timestamp,
			@QueryParam("client") String name) {
		
		//验证请求
		if(StringUtils.isEmpty(name)||StringUtils.isEmpty(sign)){
			AuthorizeResponseMessage responseMessage = new AuthorizeResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"请求不合法");
			return responseMessage;
		}
		try{
            String serverSign = MD5(timestamp);
            if(!sign.equals(serverSign)){
                return new AuthorizeResponseMessage(ResultCode.ER_KERNEL_SIGN_INVALID,"鉴权失败，签名无效");
            }
            String token = UUID.randomUUID().toString();
            //创建客户端信息
            ClientInfo info = new ClientInfo(token,name);
            ClientManager.addClient(info);
            //返回鉴权成功报文
            AuthorizeResponseMessage responseMessage = new AuthorizeResponseMessage(ResultCode.SUCCESS,"鉴权成功");
            responseMessage.setToken(token);
            return responseMessage;
        }catch(Exception e){
            logger.error("鉴权错误",e);
            return new AuthorizeResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"鉴权失败，系统内部错误，服务器端生成MD5失败");
        }
	}
	
	/**
	 * 心跳响应，访问路径：/sys/heart
	 * @param token
	 * @param timestamp
	 * @return
	 */
	@POST
	@Path("sys/heart")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public ResponseMessage sysHeartBeat(@FormParam("token") String token,@FormParam("timestamp") String timestamp){
	  //修改客户端心跳时间
        Client client = ClientManager.getClient(token);
        if(client != null){
            Date heartBeatTime = new Date(System.currentTimeMillis());
            client.setHeartBeatTime(heartBeatTime);
            return new ResponseMessage(ResultCode.SUCCESS,"连接正常");
        }else{
            return  new ResponseMessage(ResultCode.ER_KERNEL_TOKEN_INVALID,"无效的Token信息");
        }
	}

	/**
	 * 查询所有注册的客户端，访问路径：/sys/heart
	 * @param token
	 * @param timestamp
	 * @param client
	 * @return
	 */
	@GET
	@Path("sys/applist")
	@Produces(MediaType.APPLICATION_JSON)
	public ClientInfoMessage sysApplist(@QueryParam("token") String token,
								  @QueryParam("timestamp") String timestamp,
								  @QueryParam("client") String name){
		try {
		    List<ClientInfoMessage.ClientInfo> data = new ArrayList<ClientInfoMessage.ClientInfo>();
	        Collection<Client> clientList = ClientManager.findAllClient();
	        for(Client client : clientList){
	            ClientInfoMessage.ClientInfo clientInfo = new ClientInfoMessage.ClientInfo();
	            clientInfo.setClient(client.getName());
	            clientInfo.setConnectTime(DateUtils.convertDateToString(client.getConnectTime()));
	            clientInfo.setHeartBeatTime(DateUtils.convertDateToString(client.getHeartBeatTime()));
	            clientInfo.setToken(client.getToken());
	            data.add(clientInfo);
	        }
			ClientInfoMessage msg = new ClientInfoMessage(ResultCode.SUCCESS,"查询注册的客户端成功");
			msg.setApp(data);
			return msg;
		} catch (Exception e) {
			logger.error("查询注册的客户端错误",e);
		}
		
		return new ClientInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"查询注册的客户端失败");
	}
	
	@GET
	@Path("error/get")
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseMessage sendGetError(
			@QueryParam("timestamp") String timestamp,
			@QueryParam("errcode") String errcode,
			@QueryParam("msg") String info){
		return new ResponseMessage(errcode,info);
	}
	
	@POST
	@Path("error/post")
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseMessage sendPostError(
			@QueryParam("timestamp") String timestamp,
			@QueryParam("errcode") String errcode,
			@QueryParam("msg") String info){
		return new ResponseMessage(errcode,info);
	}
	
	public static String MD5(String timestamp) throws Exception{
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] digest = md.digest(("timestamp="+timestamp+"&digestKey="+Constants.DIGEST_KEY).getBytes("utf-8"));
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i<digest.length; i++){
            sb.append(Integer.toHexString((digest[i]&0xFF)|0x100).toUpperCase().substring(1,3));
        }
        
        return sb.toString();
    }
}
