/* JsonTempLoginMessageHandler.java	@date 2012-12-21
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.SocketMessageBuilder;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.TemporaryDevice;

/**
 * 功能描述：临时设备登陆消息处理
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 * 
 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
 * @version 1.4.0
 * @since 1.0.0 
 * create on: 2013-9-25 
 */
public class JsonTempLoginMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
    
    static final Logger logger = Logger.getLogger(JsonTempLoginMessageHandler.class);
	
	public JsonTempLoginMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if(msg.getMessageHeader() != null && "tempLogin".equals(msg.getMessageHeader().getCommand())){
            try{
            	 TemporaryDevice device = new TemporaryDevice();
                 device.setId(msg.getSourceSerialId());
                 device.setSystemId(msg.getSourceSystemId());
                 device.setIndex(msg.getSourceIndex());
                 device.setSocketChannelId(channel.getId());
                 int i = DeviceManager.saveOrUpdateTemporayDevice(device);
                 if(i == 1){
                     if(sendResponseMessage(channel,msg)){
                         logger.info("[RESOURCE]["+msg.getMessageHeader().getSourceObject()+"]临时设备登记");
                     }else{
                         logger.error("[RESOURCE][TEMP-LOGIN]error "+msg.getMessageHeader().getSourceObject()+"设备登记回复失败");
                     }
                 }else if(i == 2){
                     logger.error("[RESOURCE][TEMP-LOGIN]error "+msg.getMessageHeader().getSourceObject()+"设备参数为空");
                 }else{
                     logger.error("[RESOURCE][TEMP-LOGIN]error "+msg.getMessageHeader().getSourceObject()+"设备登记失败，微内核内部错误");
                 }
            }catch(Exception e){
                logger.error("[RESOURCE][TEMP-LOGIN]error", e);
            }
            return true;
        }
        return false;
    }
    
    private boolean sendResponseMessage(SocketChannel channel,SocketJsonMessage requestMessage){
        SocketJsonMessage response = SocketMessageBuilder.buildJsonMessage(Constants.JSON_PROTOCOL_VERSION,"tempLogin", SocketByteMessage.TYPE_RESPONSE, SocketJsonMessage.CONTENT_TYPE_JSON, null);
        response.getMessageHeader().setSourceObject(DeviceManager.buildAddress(Constants.SYSTEM_ID, 0, null));
        response.getMessageHeader().setTargetObject(DeviceManager.buildAddress(requestMessage.getSourceSystemId(),requestMessage.getSourceSerialId(),requestMessage.getSourceIndex()));
        if(channel.write(response)){
            return true;
        }
        return false;
    }
}
