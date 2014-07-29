package com.etone.ark.kernel.http;

import java.util.Date;

import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.task.model.ActionNode;
import com.etone.ark.kernel.task.model.Task;

public class EventActionFinish {

    private Task task;
    private ActionNode node;
    private Resource resource;
    
    private String actionResult;
    private Date beginDate;
    private Date endDate;
    
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ActionNode getNode() {
        return node;
    }

    public void setNode(ActionNode node) {
        this.node = node;
    }
    
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setActionResult(String actionResult) {
        this.actionResult = actionResult;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getToken() {
        return this.task.getToken();
    }

    public String getTaskCode() {
        return this.task.getId();
    }

    public Integer getModuleSerialNum() {
        if(resource != null){
            return DeviceManager.getDeviceID(resource.getRtdSerialNum(), resource.getRtdModuleNum());
        }
        return null;
    }

    public String getImsi() {
        if(resource != null){
            return resource.getImsi();
        }
        return null;
    }

    public String getActionType() {
        return this.node.getActionType();
    }

    public String getActionResult() {
        return this.actionResult;
    }

    public Date getBeginDate() {
        return this.beginDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public String getActionName() {
        return this.node.getId();
    }
}
