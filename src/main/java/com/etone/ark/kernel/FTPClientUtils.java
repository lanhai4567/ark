package com.etone.ark.kernel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPClientUtils {

	private final static Logger Log = LoggerFactory.getLogger(FTPClientUtils.class);
	
	private static String ip;
	private static Integer port = 21;
	private static String userName;
	private static String userPwd;
	
	private static ThreadLocal<FTPClientAgent> client = new ThreadLocal<FTPClientAgent>();
	
	/**
	 * @param ftpPath	格式:FTP://用户名:密码@IP:端口号
	 */
	public static void initial(String ftpPath){
		Pattern pat = Pattern.compile("^FTP://([^:]+):([^:]+)@([^:]+):?(\\d*)$");
		Matcher mat = pat.matcher(ftpPath);
		if(mat.find()){
			ip = mat.group(3);
			userName = mat.group(1);
			userPwd = mat.group(2);
			if(StringUtils.isNotBlank(mat.group(4))){
				port = Integer.valueOf(mat.group(4));
			}
			Log.info("FTP客户端初始化成功! 初始化路径："+ftpPath);
		}else{
			Log.error("FTP客户端初始化失败! 初始化路径："+ftpPath);
		}
	}
	
	public static FTPClientAgent get(){
		FTPClientAgent clientAgent = client.get();
		if(clientAgent == null){
			clientAgent = new FTPClientAgent(ip,port,userName,userPwd);
		}
		if(!clientAgent.getClient().isConnected()){
			clientAgent.login();
		}
		return clientAgent;
	}
	
	public static void disconnect(){
		FTPClientAgent clientAgent = client.get();
		if(clientAgent != null){
			clientAgent.disconnect();
			client.remove();
		}
	}
	
	public static class FTPClientAgent{
		
		private String ip;
		private Integer port = 21;
		private String userName;
		private String userPwd;
		
		private FTPClient client;
		
		public FTPClientAgent(String ip,Integer port,String userName,String userPwd){
			this.ip = ip;
			this.port = port;
			this.userName = userName;
			this.userPwd = userPwd;
			this.client = new FTPClient();
		}
		
		public FTPClientAgent(String ftpPath){
			Pattern pat = Pattern.compile("^FTP://([^:]+):([^:]+)@([^:]+):?(\\d*)$");
			Matcher mat = pat.matcher(ftpPath);
			if(mat.find()){
				this.ip = mat.group(3);
				this.userName = mat.group(1);
				this.userPwd = mat.group(2);
				if(StringUtils.isNotBlank(mat.group(4))){
					this.port = Integer.valueOf(mat.group(4));
				}
			}
			this.client = new FTPClient();
		}
		
		public boolean login(){
			try{
				client.connect(this.ip,this.port);
				if(client.isConnected()){
					if(client.login(this.userName,this.userPwd)){
						client.setKeepAlive(true);
						Log.info("FTP客户端链接成功!");
					}else{
						Log.error("FTP客户端账号登陆失败");
						client.disconnect();
					}
				}else{
					Log.error("FTP客户端无法建立Socket链接");
				}
				return true;
			}catch(Exception e){
				Log.error("FTP客户端链接失败",e);
				disconnect();
			}
			return false;
		}
		
		public void disconnect(){
			try {
				client.disconnect();
			} catch (IOException e) {
				Log.error("FTP客户端断开连接错误", e);
			}
		}
		
		public File downFile(String targetFile,String localPath){
			File result = null;
			FileOutputStream stream = null;
			targetFile.replace(File.separator, "/");
			int spot = targetFile.lastIndexOf("/");
			String fileName = targetFile;
			if(spot > 0){
				fileName = targetFile.substring(spot + 1);
			}
			try{
				result = new File(localPath + fileName);
				result.getParentFile().mkdirs();
				stream = new FileOutputStream(result);
				if(this.client.retrieveFile(targetFile, stream)){
					stream.close();
					Log.debug("FTP客户端下载文件成功,下载文件："+targetFile);
				}else{
					Log.error("FTP客户端下载文件失败,下载文件："+targetFile);
					stream.close();
					//删除空文件
					if(result != null && result.exists()){
						result.delete();
						result = null;
					}
				}
			}catch(Exception e){
				Log.error("FTP客户端下载文件失败",e);
				if(stream != null){
					try {
						stream.close();
					} catch (IOException ioEx) {}
				}
				if(result != null && result.exists()){
					result.delete();
				}
				result = null;
			}
			return result;
		}
		
		public File waitDownFile(String targetFile,String localPath,Long waitTime){
			File result = null;
			FileOutputStream stream = null;
			targetFile.replace(File.separator, "/");
			int spot = targetFile.lastIndexOf("/");
			String fileName = targetFile;
			if(spot > 0){
				fileName = targetFile.substring(spot + 1);
			}
			try{
				Long size = waitGetFileSize(targetFile,waitTime);
				if(size != null && size > 0){
					result = new File(localPath + fileName);
					result.getParentFile().mkdirs();
					stream = new FileOutputStream(result);
					if(this.client.retrieveFile(targetFile, stream)){
						stream.close();
						Log.debug("FTP客户端下载文件成功,下载文件："+targetFile);
					}else{
						Log.error("FTP客户端下载文件失败,下载文件："+targetFile);
						stream.close();
						//删除空文件
						if(result != null && result.exists()){
							result.delete();
							result = null;
						}
					}
				}else{
					Log.error("FTP客户端未找到远端下载文件："+targetFile);
				}
			}catch(Exception e){
				Log.error("FTP客户端下载文件失败",e);
				if(stream != null){
					try {
						stream.close();
					} catch (IOException ioEx) {}
				}
				if(result != null && result.exists()){
					result.delete();
				}
				result = null;
			}
			return result;
		}
		
		public Long waitGetFileSize(String targetFile,Long waitTime){
			Long result = null;
			if(StringUtils.isNotBlank(targetFile)){
				if(waitTime == null){waitTime = 0L;}
				try{
					Integer replyCode = null;
					String replyString = null;
					do{
						waitTime =  waitTime - 300;
						replyCode = this.client.sendCommand("size",targetFile);
						replyString = this.client.getReplyString();
						if(replyCode == 213){
							Matcher mat = Pattern.compile("^213 (\\d*)$").matcher(replyString);
							if(mat.find()){
								result = Long.valueOf(mat.group(1));
							}
						}else if(replyCode == 550){
							Thread.sleep(300L);
						}
					}while(result == null && waitTime > 0);
				}catch(Exception e){
					Log.error("FTP客户端获取远端文件大小失败",e);
					result = null;
				}
			}
			return result;
		}
		
		public FTPClient getClient(){
			return this.client;
		}
		
		public String getIp() {
			return ip;
		}

		public Integer getPort() {
			return port;
		}

		public String getUserName() {
			return userName;
		}

		public String getUserPwd() {
			return userPwd;
		}

		@Override
		public boolean equals(Object obj) {
			if(FTPClientAgent.class.isInstance(obj)){
				FTPClientAgent temp = (FTPClientAgent)obj;
				if(this.ip.equals(temp.getIp())){
					if(this.port == temp.getPort()){
						if(this.userName.equals(temp.getUserName())){
							return true;
						}
					}
				}
			}
			return false;
		}
		
		
	}
}
