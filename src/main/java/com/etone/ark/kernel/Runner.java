/* Runner.java	@date 2012-11-13
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel;

import org.apache.log4j.Logger;

import com.etone.ark.communication.CommunicationServer;
import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.communication.solver.EventController;
import com.etone.ark.kernel.service.ClientManager;
import com.etone.ark.kernel.service.ContainerService;
import com.etone.ark.kernel.service.H2DataBaseServer;
import com.etone.ark.kernel.service.JavaScriptEngine;
import com.etone.ark.kernel.service.PesqServer;
import com.etone.ark.kernel.service.TaskStructureManager;
import com.etone.ark.kernel.service.timer.ClientHeartBeatCheckTimer;
import com.etone.ark.kernel.service.timer.DeviceHeartbeatCheckTimer;
/**
 * 微内核启动类
 * @version 1.1
 * @author 张浩
 *
 */
public class Runner {
	
	protected static Logger logger = Logger.getLogger(Runner.class);

	public static void main(String[] args) {
		try {
			//FIXME KHB 异常统一处理，定义服务启动级别和先后顺序
 			
 			//启动FTP服务 【 level - 0】
			//ApacheFtpServer.startServer();
			
 			//初始化 H2内存数据库，表【ClientTable，EventTable】 【 level-1 】
 			H2DataBaseServer.run();
 			
			//初始化JS脚本库【 level - 2】
			//ExpressionSolver.initJavaScriptFile("cfg/js/kernel.js");
			
			if(JavaScriptEngine.run("cfg/js/kernel.js")){
				logger.info("JavaScript脚本加载完成！");
			}else{
				logger.error("JavaScript脚本加载失败！");
			}
			
 			//初始化资源命令服务【 level - 3】
			ProtocolManager.initial("protocolByte.xml","protocolJson.xml","protocolJson_android.xml");
 			
 			//初始化业务结构，必须初始化完动作信息后才进行，在内部需要配置的动作是否正确【 level - 3 】
			TaskStructureManager.initial("cfg/task");
 			
 			//初始化内核线程池，容器【 level - 4】
			if(Constants.KERNEL_MAX_THREAD_SIZE == 0){
			    ContainerService.initial();
			}else{
			    ContainerService.initial(Constants.KERNEL_MAX_THREAD_SIZE);
			}
			
			//初始化事件控制器
			EventController.initial(ContainerService.getExecutor());
			
            //启动通讯服务
 			CommunicationServer.run(null,PropertiesUtil.getProperties());
 			
 			//开启设备心跳检测 【 level - 5 】
            DeviceHeartbeatCheckTimer.start();
 			
 			//恢复初始化前的客户端
//            ClientManager.initialBeforeRecoverClient();
            
            //启动检测客户端心跳检测定时器【 level - 5 】
            ClientHeartBeatCheckTimer.start();
            
            //初始化FTP客户端
            FTPClientUtils.initial(PropertiesUtil.getValue("ftpPath"));
            
            //初始化PESQ服务
//            PesqServer.initial();
            
			//微内核初始化-- 客户端通知 【 level - 6 】
			/*EventKernelInitial event = new EventKernelInitial();
			event.setTime(new Date());
			ClientManager.callbackEvent(event);*/
			
			logger.info("Kernel Start Running Success.....");
		} catch (Exception e) {
			logger.error("Kernel Start Running Error:" + e.getMessage(), e);
			System.exit(1);
		}
	}
	
}
