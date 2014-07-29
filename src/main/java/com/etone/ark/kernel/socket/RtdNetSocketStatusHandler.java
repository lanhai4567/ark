package com.etone.ark.kernel.socket;

import com.etone.ark.communication.socket.ISocketStatusHandler;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.service.RtdNetService;

public class RtdNetSocketStatusHandler implements ISocketStatusHandler{

    @Override
    public void open(SocketChannel ch) {
        RtdNetService.initial(ch);
    }

    @Override
    public boolean close(SocketChannel ch) {
        return true;
    }

}
