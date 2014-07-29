package com.etone.ark.kernel.remote;

import org.apache.log4j.Logger;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.solver.EventController;

public class RemoteCityMessageResponseHandler extends AbsSocketMessageHandler<SocketByteMessage>{

    static Logger logger = Logger.getLogger(RemoteCityMessageResponseHandler.class);
    
    public RemoteCityMessageResponseHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
        if(msg.getMessageHeader().getCommand() == 2002 || msg.getMessageHeader().getCommand() == 2004){
            //logger.debug("local-receive: command:"+msg.getMessageHeader().getCommand()+" " + msg.getMessageHeader().getSourceHighAddress() + msg.getMessageHeader().getSourceLowAddress() + "->" + msg.getMessageHeader().getTargetHighAddress() + msg.getMessageHeader().getTargetLowAddress() + " length:" + msg.getLength());
            String kye = msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getTargetHighAddress() + "_" + msg.getMessageHeader().getTargetLowAddress();
            EventController.trigger(kye, msg);
            return true;
        }
        return false;
    }

}
