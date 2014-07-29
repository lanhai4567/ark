package com.etone.ark.kernel.remote;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.DeviceManager;

public class RemoteCityActionResponseHandler extends AbsSocketMessageHandler<SocketByteMessage>{

    static Logger logger = Logger.getLogger(RemoteCityActionResponseHandler.class);
    
    public RemoteCityActionResponseHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
        if(msg.getMessageHeader().getTargetHighAddress() == Constants.REMOTE_SYSTEM_ID && msg.getMessageHeader().getTargetLowAddress() == 0){
            logger.debug("local-receive: command:"+msg.getMessageHeader().getCommand()+" " + msg.getMessageHeader().getSourceHighAddress() + msg.getMessageHeader().getSourceLowAddress() + "->" + msg.getMessageHeader().getTargetHighAddress() + msg.getMessageHeader().getTargetLowAddress() + " length:" + msg.getLength());
            String kye = msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getSourceHighAddress() + "_" + DeviceManager.getDeviceID(msg.getMessageHeader().getSourceLowAddress(),msg.getActionHeader().getModuleNo());
            EventController.trigger(kye, msg);
            return true;
        }
        return false;
    }

}
