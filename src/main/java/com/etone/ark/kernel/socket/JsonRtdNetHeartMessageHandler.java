package com.etone.ark.kernel.socket;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.socket.SocketJsonMessage;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.Constants;

public class JsonRtdNetHeartMessageHandler extends AbsSocketMessageHandler<SocketJsonMessage>{

    static final Logger logger = Logger.getLogger(JsonRtdNetHeartMessageHandler.class);
    
    public JsonRtdNetHeartMessageHandler(ISocketMessageDecode<SocketJsonMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketJsonMessage msg) {
        logger.debug("[SOCKET][RTD-NET]" + msg.getMsg());
        if(Constants.SYSTEM_ID == msg.getTargetSystemId() && 0 == msg.getTargetSerialId()){
            String ekey = msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getSourceObject();
            EventController.trigger(ekey, msg);
            return true;
        }
        return false;
    }

}
