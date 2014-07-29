package com.etone.ark.kernel.remote;

import com.etone.ark.communication.socket.ISocketStatusHandler;
import com.etone.ark.communication.socket.SocketChannel;

public class RemoteCityLinkHandler implements ISocketStatusHandler{

    @Override
    public void open(SocketChannel ch) {
        RemoteCityServer.initial(ch);
        RemoteCityServer.sendLogin();
    }

    @Override
    public boolean close(SocketChannel ch) {
        return true;
    }
}
