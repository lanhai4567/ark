package com.etone.ark.kernel.service;


import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.Logger;

import com.etone.ark.kernel.Constants;

public class ApacheFtpServer {
	
	protected static Logger logger = Logger.getLogger(ApacheFtpServer.class);

	// FTP服务器的池
	private static ConcurrentHashMap<String, FtpServer> ftpServerPool = new ConcurrentHashMap<String, FtpServer>();

	static{
		initFtpServerConfig();
	}
	
	private static void initFtpServerConfig(){
		// FTP server 配置
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		// 设置监听端口 
		factory.setPort(Constants.FTP_SERVER_PORT); // 默认端口：2121
		// 默认存在一个default的监听端口，这里采用这种方式替换默认的监听端口
		serverFactory.addListener("default", factory.createListener());
		// 用户管理
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
		userManagerFactory.setFile(new File("cfg/ftpUser.properties"));
		serverFactory.setUserManager(userManagerFactory.createUserManager());
		// 默认FTP根路径创建
		NativeFileSystemFactory fileSystem = new NativeFileSystemFactory();
		fileSystem.setCaseInsensitive(true);
		fileSystem.setCreateHome(true);
		serverFactory.setFileSystem(fileSystem);

		// 创建服务
		FtpServer server = serverFactory.createServer();
		ftpServerPool.put("defaultftpserver", server);
 	}

	/**
	 * 启动FTP服务器
	 * @param name FTP服务名称【默认值：“defaultftpserver”】
	 */
	public static void startServer(){
		startServer(null);
	}
	  
	
	/**
	 * 启动FTP服务器
	 * @param name FTP服务名称【默认值：“defaultftpserver”】
	 */
	public static void startServer(String name) {
		if (null == name || "".equals(name)) {
			name = "defaultftpserver";
		}
		FtpServer ftpServer = ftpServerPool.get(name);
		try {
			ftpServer.start();
			logger.info("ftpserver start success...");
		} catch (FtpException e) {
			logger.error("ftpserver start  error", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 停止FTP服务器
	 * @param name FTP服务名称【默认值：“defaultftpserver”】
	 */
	public static void stop(String name) {
		FtpServer ftpServer = ftpServerPool.get(name);
		ftpServer.stop();
	}

	/**
	 *  重启FTP服务器
	 * @param name FTP服务名称【默认值：“defaultftpserver”】
	 */
	public static void resume(String name) {
		FtpServer ftpServer = ftpServerPool.get(name);
		ftpServer.resume();
	}

	/**
	 * 暂停FTP服务器
	 * @param name FTP服务名称【默认值：“defaultftpserver”】
	 */
	public  static void suspend(String name) {
		FtpServer ftpServer = ftpServerPool.get(name);
		ftpServer.suspend();
	}


	public static ConcurrentHashMap<String, FtpServer> getFtpServerPool() {
		return ftpServerPool;
	}
}
