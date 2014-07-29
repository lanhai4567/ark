package com.etone.ark.kernel.task;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.etone.ark.communication.Protocol;
import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.communication.socket.SocketByteMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.EventActionFinish;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.CommandServer;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.service.model.SidSlotDevice;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.Task;

public class ByteActionNodeHandler extends ActionNodeHandler<SocketByteMessage> {

    // 动作结果回调函数
    protected EventActionFinish event;

    public ByteActionNodeHandler(Task task, Node node) {
        super(task, node);
        this.event = new EventActionFinish();
    }

    @Override
    public void send() throws Exception {
        event.setBeginDate(new Date(System.currentTimeMillis()));
        int error = CommandServer.requestAction(node.getActionType(), resource, Integer.valueOf(task.getId()),
                this.parameters.getBingdings(), this);
        if (error == 2) {
            parameters.put("resultCode",Constants.ACTION_ERROR);
            parameters.put("errorDescription", "构建报文失败");
            task.getHistory().put(parameters);
            this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
            this.triggerActionFinishEvent(); // 触发节点完成事件
            // 下一个节点
            this.getNextNodeHandler().run();
        } else if (error == 3) {
            parameters.put("resultCode", Constants.ACTION_ERROR);
            parameters.put("errorDescription", "通讯错误");
            task.getHistory().put(parameters);
            this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
            this.triggerActionFinishEvent(); // 触发节点完成事件
            // 下一个节点
            this.getNextNodeHandler().run();
        } else if (error == 1) {
            logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][sendMsg]");
        }
    }

    @Override
    public void receice(SocketByteMessage msg) throws Exception {
        // 获取动作回复报文
        Protocol protocol = ProtocolManager.getProtocol(Constants.BYTE_PROTOCOL_VERSION, node.getActionType());
        if (protocol != null && protocol.getResponse() != null) {
            for (Protocol.Parameter item : protocol.getResponse().getParameters()) {
                Integer length = Integer.valueOf(item.getLength());
                if ("Integer".equals(item.getType())) {
                    if (1 == length) {
                        this.parameters.put(item.getName(), msg.readUnsignedByte());
                    } else if (2 == length) {
                        this.parameters.put(item.getName(), msg.readChar());
                    } else if (4 == length) {
                        this.parameters.put(item.getName(), msg.readInt());
                    } else if (8 == length) {
                        this.parameters.put(item.getName(), msg.readLong());
                    }
                } else if ("String".equals(item.getType())) {
                    this.parameters.put(item.getName(), msg.readString(length).trim());
                }
            }
        }
        String[] result = errorCodeChange(msg.getActionHeader().getErrorCode());
        this.parameters.put("resultCode", result[0]);
        this.parameters.put("errorDescription", result[1]);
        task.getHistory().put(parameters); // 将节点参数加入到测试业务参数中
        this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
        logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][receiveMsg]"
                + this.parameters.get("resultCode"));
        this.triggerActionFinishEvent(); // 触发节点完成事件
        // 下一个节点
        this.getNextNodeHandler().run();
    }

    @Override
    public void receiceTimeout() throws Exception {
        // 超时处理
        parameters.put("resultCode", Constants.ACTION_TIMEOUT);
        parameters.put("errorDescription","动作超时");
        // 判断设备是否故障
        if (Constants.RTD_MODULE_STATUS_NORMAL != resource.getRtdModule().getStatus()) {
            parameters.put("resultCode", resource.getRtdModule().getStatus());
            parameters.put("errorDescription","模块故障");
        }

        SidSlotDevice slot = resource.getSidSlot();
        if (slot != null && Constants.SID_SLOT_STATUS_NORMAL != slot.getStatus()) {
            parameters.put("resultCode", slot.getStatus());
            parameters.put("errorDescription","卡槽故障");
        }
        task.getHistory().put(parameters);
        //this.setIfTaskResult(parameters.getString("resultCode"),parameters.getString("errorDescription"));
        logger.debug("[TASK][" + task.getToken() + "][" + task.getId() + "][" + node.getId() + "][timeout]");
        this.triggerActionFinishEvent(); // 触发节点完成事件
        TaskManager.InterruptTask(task,parameters.getString("resultCode"),parameters.getString("errorDescription"));

    }

    protected void triggerActionFinishEvent() {
        this.event.setEndDate(new Date(System.currentTimeMillis()));
        this.event.setTask(task);
        this.event.setNode(node);
        this.event.setResource(resource);
        if (StringUtils.isEmpty(this.event.getActionResult())) {
            this.event.setActionResult(this.parameters.getString("resultCode"));
        }
        ClientManager.callbackEvent(event);
    }

    public static String[] errorCodeChange(int errorCode) {
        String[] result = new String[2];
        if (0 == errorCode) {
            result[0] = "SUCCESS";
            result[1] = "成功";
        } else if (0x00010001 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "无信号";
        } else if (0x00010002 == errorCode) {
            result[0] = "ER_RTD_SERVICE_CREG_FAIL";
            result[1] = "网络登记失败";
        } else if (0x00010003 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "获取场强失败";
        } else if (0x00020001 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "检测不到SIM卡";
        } else if (0x00020002 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "模块错误";
        } else if (0x00030001 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "初始化失败";
        } else if (0x00030002 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "登记呼转失败";
        } else if (0x00040005 == errorCode) {
            result[0] = "ER_RTD_SERVICE_NO_CALLIN";
            result[1] = "没有电话呼入";
        } else if (0x00040007 == errorCode) {
            result[0] = "ER_RTD_SERVICE_NO_ANSWER";
            result[1] = "呼叫无应答";
        } else if (0x00040008 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "被叫方忙";
        } else if (0x0004000B == errorCode) {
            result[0] = "ER_RTD_SERVICE_DTMF_FAIL";
            result[1] = "发送DTMF失败";
        } else if (0x0004000C == errorCode) {
            result[0] = "ER_RTD_SERVICE_PLAYSOUND_FAIL";
            result[1] = "播音失败";
        } else if (0x0004000D == errorCode) {
            result[0] = "ER_RTD_SERVICE_RECORD_FAIL";
            result[1] = "录音失败";
        } else if (0x00040012 == errorCode) {
            result[0] = "ER_RTD_SERVICE_DROP_CALL";
            result[1] = "掉话";
        } else if (0x00050001 == errorCode) {
            result[0] = "ER_RTD_SERVICE_RECV_SMSREPORT_FAIL";
            result[1] = "接收短信状态报告失败";
        } else if (0x00050002 == errorCode) {
            result[0] = "ER_RTD_SERVICE_RECV_SMS_FAIL";
            result[1] = "接收短信失败";
        } else if (0x00050003 == errorCode) {
            result[0] = "ER_RTD_SERVICE_SEND_SMS_FAIL";
            result[1] = "发送短信失败";
        } else if (0x00070001 == errorCode) {
            result[0] = "ER_RTD_SERVICE_PING_FAIL";
            result[1] = "PING失败";
        } else if (0x00080001 == errorCode) {
            result[0] = "ER_RTD_SERVICE_PING_FAIL";
            result[1] = "HTTP失败";
        } else if (0x000C0002 == errorCode) {
            result[0] = "ER_RTD_MODEM_PDP_FAIL";
            result[1] = "PDP失败";
        } else if (851978 == errorCode) {
            result[0] = "ER_RTD_SERVICE_FILE_DOWNLOAD_FAIL";
            result[1] = "资源文件下载失败";
        } else if (917506 == errorCode) {
            result[0] = String.valueOf(errorCode);
            result[1] = "获取状态报告超时";
        } else {
            result[0] = String.valueOf(errorCode);
            result[1] = "未知错误";
        }
        return result;
    }
}
