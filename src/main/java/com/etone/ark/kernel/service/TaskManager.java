package com.etone.ark.kernel.service;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.ScriptException;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.http.EventTaskFinish;
import com.etone.ark.kernel.task.model.Task;
import com.etone.ark.kernel.task.model.TaskStructure;
import com.etone.ark.kernel.service.model.Resource;

public class TaskManager {

    static Logger logger = Logger.getLogger(TaskManager.class);

    private final static ConcurrentMap<String, Task> STORE = new ConcurrentHashMap<String, Task>();

    /**
     * 中断测试项
     */
    public static void InterruptTask(Task task,String resultCode,String errorDescription) {
        if (!task.isRunState(Task.STATUS_STOP)) {
            // 设置测试业务运行状态为中断
            task.setRunState(Task.STATUS_STOP);
            // 日志输出
            logger.info("[TASK][" + task.getId() + "]interrupt");
            // 遍历资源组，发送中断命令
            try{
                for (Resource resource : task.getResources().values()) {
                	DeviceManager.unemployResource(resource);
                    CommandServer.requestResourceRelease(resource);
                }
            }catch(Exception e){}
            // 添加测试项结束时间
            task.setEndDate(new Date(System.currentTimeMillis()));
            // 设置测试项返回结果为中断
            task.setResultCode(resultCode);
            task.setErrorDescription(errorDescription);
            // 将结果加入到脚本中
            try {
				task.script.put("resultCode", task.getResultCode());
				task.script.put("errorDescription", task.getErrorDescription());
			} catch (ScriptException e) {
				e.printStackTrace();
			}
           /* task.getHistory().put("resultCode", task.getResultCode());
            task.getHistory().put("errorDescription", task.getErrorDescription());*/
            // 移除测试项
            delete(task.getId());
            // 触发测试项结果回调函数
            EventTaskFinish event = new EventTaskFinish();
            event.setTask(task);
            ClientManager.callbackEvent(event);
        }
    }

    /**
     * 功能描述：
     * 
     * @param task
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-6-14
     */
    public static int checkTask(Task task) {
        if (STORE.get(task.getId()) != null) {
            return 1;
        } else {
            TaskStructure structure = TaskStructureManager.get(task.getType());
            if (structure == null) {
                return 2;
            }
            task.setStructure(structure);
        }
        return 0;
    }

    public static boolean saveOrUpdate(Task task) {
        try {
            STORE.put(task.getId(), task);
            return true;
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            return false;
        }
    }

    public static Task get(String key) {
        if (key != null) {
            return STORE.get(key);
        }
        return null;
    }

    public static boolean delete(String key) {
        if (key != null) {
            STORE.remove(key);
            return true;
        }
        return false;
    }

    public static Collection<Task> findAll() {
        return STORE.values();
    }

    public static int count() {
        return STORE.size();
    }
}
