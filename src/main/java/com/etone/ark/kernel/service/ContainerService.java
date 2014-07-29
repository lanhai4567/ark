/* ContainerService.java	@date 2012-12-31
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪</p>
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>线程容器服务</p>
 * @version 1,2
 * @author 张浩
 *
 */
public class ContainerService {
	
	private static ExecutorService pool;

	private ContainerService(){}
	
	/**
	 * 初始化线程容器
	 * @param poolSize	线程个数
	 */
	public static void initial(int poolSize){
		pool = Executors.newFixedThreadPool(poolSize);
	}
	
	public static void initial(){
		pool = Executors.newCachedThreadPool();
	}
	
	/**
	 * 执行一个线程程序
	 * @param command	线程程序
	 */
	public static void execute(Runnable command){
		pool.execute(command);
	}

	/**
	 * 获取线程池
	 * @return 线程池对象
	 */
	public static ExecutorService getExecutor(){
		return pool;
	}
}
