package com.etone.ark.kernel.http;

import java.util.Date;

import com.etone.ark.kernel.task.model.Task;

public class EventTaskFinish {

    private Task task;

    private String taskResult;
    
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Date getBeginDate() {
        return task.getStartDate();
    }

    public Date getEndDate() {
        return task.getEndDate();
    }

    public String getTaskCode() {
        return task.getId();
    }

    public String getTaskResult() {
        return taskResult;
    }
    
    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public String getTaskType() {
        return task.getStructure().getCode();
    }

    public String getToken() {
        return task.getToken();
    }
}
