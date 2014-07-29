/* RelayMessageHandle.java	@date 2012-12-21
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.Constants;

/**
 * <p>转发的消息处理类</p>
 * 该消息处理必须是责任链中最后一个消息处理
 * @serial 0.1
 * @version 0.1
 * @author 张浩 
 *
 */
public class JsonActionResponseMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{
    
    static final Logger logger = Logger.getLogger(JsonActionResponseMessageHandler.class);
	
	public JsonActionResponseMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        if(Constants.SYSTEM_ID == msg.getTargetSystemId() && 0 == msg.getTargetSerialId()){
            String ekey = msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getSourceObject();
            EventController.trigger(ekey, msg);
            return true;
        }
        return false;
    }
}
