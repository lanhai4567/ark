package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.JsonUtils;
import com.etone.ark.communication.json.ModuleNetInfo;
import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.RtdNetService;

public class JsonRtdNetMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{

    static Logger logger = Logger.getLogger(JsonRtdNetMessageHandler.class);
    
    public JsonRtdNetMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if(msg.getMessageHeader() == null){
            logger.error("[RESOURCE][RTD-LOGIN]错误报文:"+msg.getMsg());
            return true;
        }
        if("rtdCell".equals(msg.getMessageHeader().getCommand())){
            try{
                ModuleNetInfo info = msg.getContent(ModuleNetInfo.class);
                info.setModuleSerialNum(DeviceManager.getDeviceID(msg.getSourceSerialId(), msg.getSourceIndex()));
                SocketJsonMessage temp = new SocketJsonMessage();
                temp.setMessageHeader(msg.getMessageHeader());
                temp.setBody(JsonUtils.toJson(info));
                if(!RtdNetService.upRtdNet(temp)){
                    logger.debug("未转发网络信息:" + temp.getMsg());
                }
            }catch(Exception e){
                logger.error("[rtdCell][error]", e);
            }
            return true;
        }
        return false;
    }
}
