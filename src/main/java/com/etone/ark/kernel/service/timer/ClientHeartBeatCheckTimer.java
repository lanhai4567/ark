package com.etone.ark.kernel.service.timer;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.http.Client;
import com.etone.ark.kernel.service.ClientManager;

/**
 * 客户端心跳超时检测
 * 心跳检测实现方式是否可用优化？
 * @version 1.1
 * @author 张浩
 *
 */
public class ClientHeartBeatCheckTimer{

	private static final Logger logger = Logger.getLogger(ClientHeartBeatCheckTimer.class);
	
	private static Timer timer = new Timer();
	
	public static void start(){
		timer.schedule(new ClientHeartBeatCheckTask(),0, Constants.CLIENT_HEART_BEAT_TIMEOUT*1000);
	}
	
	private static class ClientHeartBeatCheckTask extends TimerTask{
		@Override
		public void run(){
			//获取所有客户端
			Collection<Client> clientList = ClientManager.findAllClient();
			//遍历所有客户端检测心跳时间
			for(Client client : clientList){
				if(DateUtils.compareSecond(client.getHeartBeatTime(), new Date()) > Constants.CLIENT_HEART_BEAT_TIMEOUT){
					//如果超时移除客户端缓存
				    ClientManager.removeClient(client.getToken());
					logger.info("心跳超时......token:"+client.getToken());
					
				}
			}
		}
	}
}
