package com.etone.ark.kernel.socket;

import com.etone.ark.communication.socket.AbsSocketMessageHandler;
import com.etone.ark.communication.socket.ISocketMessageDecode;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.communication.socket.SocketByteMessageActionHeader;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.service.DeviceManager;

public class ByteSyncMessageHandler extends AbsSocketMessageHandler<SocketByteMessage> {

    public ByteSyncMessageHandler(ISocketMessageDecode<SocketByteMessage> nextDecode) {
        super(nextDecode);
    }

    @Override
    public boolean execute(SocketChannel channel, SocketByteMessage msg) {
        if (msg.getMessageHeader().getCommand() == 0x0206) {
            try {
                msg.setActionHeader(new SocketByteMessageActionHeader());
                msg.getActionHeader().setTaskId(msg.readInt());
                msg.getActionHeader().setModuleNo(msg.readUnsignedByte());
                msg.getActionHeader().setOperationNum(msg.readUnsignedByte());
                msg.getActionHeader().setOperationSequence(msg.readInt());
                msg.getActionHeader().setLineLength(msg.readUnsignedByte());
                msg.getActionHeader().setOperationLine(msg.readString(msg.getActionHeader().getLineLength()));

                EventController.trigger("sync_"+ DeviceManager.getDeviceID(msg.getMessageHeader().getSourceLowAddress(), msg.getActionHeader().getModuleNo()), msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
