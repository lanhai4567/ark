package com.etone.ark.kernel.socket;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.DeviceManager;

public class ByteActionResponseMessageHandler extends AbsSocketMessageHandler<SocketByteMessage>{

    public ByteActionResponseMessageHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
        if(msg.getMessageHeader().getTargetHighAddress() == Constants.SYSTEM_ID && msg.getMessageHeader().getTargetLowAddress() == 0){
            String ekey = msg.getMessageHeader().getCommand() + "_" + msg.getMessageHeader().getSourceHighAddress()+ "_" + DeviceManager.getDeviceID(msg.getMessageHeader().getSourceLowAddress(),msg.getActionHeader().getModuleNo());
            EventController.trigger(ekey, msg);
            return true;
        }
        return false;
    }

}
