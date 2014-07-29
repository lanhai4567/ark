package com.etone.ark.kernel.socket;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketChannel;

public class ByteRtdReleaseMessageHandler extends AbsSocketMessageHandler<SocketByteMessage>{

    public ByteRtdReleaseMessageHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
        if(msg.getMessageHeader().getCommand() == 0x8203){
            return true;
        }
        return false;
    }

}
