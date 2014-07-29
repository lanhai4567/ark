/* Task.java	@date 2013-4-10
 * @JDK 1.6
 * @encoding UTF-8
 * <p>版权所有：宜通世纪</p>
 * <p>未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel.task.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.ExpressionSolver.ScriptObject;
import com.etone.ark.kernel.service.ScriptRegion;
import com.etone.ark.kernel.service.model.Resource;

/**
 * 测试项
 * @version 1.2
 * @author 张浩
 *
 */
public class Task {
	
	private final ReentrantLock mainLock = new ReentrantLock();
	
	/**
	 * <p>挂起状态</p>
	 * 当没有开始运行或等待资源解锁时
	 */
	public final static int STATUS_HANG = 0;
	/**
	 * 正在执行状态
	 */
	public final static int STATUS_EXECUTE = 1;
	/**
	 * 中断状态
	 */
	public final static int STATUS_STOP = 2;
	/**
	 * 执行完成状态
	 */
	public final static int STATUS_FINISH = 3;
	
	/**
	 * 测试业务的客户端
	 */
	private String token;
	
	/**
	 * 测试业务ID
	 */
	private String id;
	
	/**
	 * 测试业务类型
	 */
	private String type;
	
	/**
	 * 测试业务结构
	 */
	TaskStructure structure;
	
	/**
	 * 资源组
	 */
	private final Map<String,Resource> resources;
	
	/**
	 * 测试项运行参数 
	 */
	private Map<String,Object> parameters;
	
	/**
	 * <p>流程执行状态</p>
	 * 初始化默认为：0，代表挂起状态
	 */
	volatile int runState = 0;
	
	/**
	 * 所有节点执行的结果
	 */
	private ScriptObject history;
	
	/**
	 * 流程开始时间
	 */
	private Date startDate;
	
	/**
	 * 流程结束时间
	 */
	private Date endDate;
	
	/**
	 * 测试项结果Code
	 */
	private String resultCode = Constants.ACTION_SUCCESS;
	
	private String errorDescription = "执行成功";

	/**
	 * 正在执行的节点程序
	 */
	private Map<String,Runnable> nodeRunnables;
	
	public ScriptRegion script;
	
	public Task(){
		this.history = new ScriptObject("this");
		this.nodeRunnables = new ConcurrentHashMap<String,Runnable>();
		this.resources = new HashMap<String,Resource>();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TaskStructure getStructure() {
		return structure;
	}

	public void setStructure(TaskStructure structure) {
		this.structure = structure;
	}

	public Map<String, Resource> getResources() {
		return resources;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public int getRunState() {
		return runState;
	}
	
	/**
	 * 判断运行状态是否是指定状态
	 * @param runState
	 * @return
	 */
	public boolean isRunState(int runState) {
		mainLock.lock();
		try{
			return (this.runState == runState);
		}finally{
			mainLock.unlock();
		}
	}

	/**
	 * 设置运作状态
	 * @param runState
	 */
	public void setRunState(int runState) {
		mainLock.lock();
		try{
			this.runState = runState;
		}finally{
			mainLock.unlock();
		}
	}
	
	/**
	 * 功能描述：开始节点尝试运行
	 * 
	 * @return 
	 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
	 * @version 1.0.0
	 * @since 1.0.0
	 * create on: 2013-6-20
	 */
	public boolean tryRun(){
	    mainLock.lock();
        try{
            if(this.runState == Task.STATUS_HANG){
                this.runState = Task.STATUS_EXECUTE;
                return true;
            }
        }finally{
            mainLock.unlock();
        }
        return false;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ScriptObject getHistory() {
		return history;
	}

	public void setHistory(ScriptObject history) {
		this.history = history;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public Map<String, Runnable> getNodeRunnables() {
		return nodeRunnables;
	}

	public void setNodeRunnables(Map<String, Runnable> nodeRunnables) {
		this.nodeRunnables = nodeRunnables;
	}

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
