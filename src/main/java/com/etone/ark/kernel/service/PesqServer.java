package com.etone.ark.kernel.service;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.etone.cplusplus.Pesq;

public class PesqServer {

	static Logger logger = Logger.getLogger(PesqServer.class);
	
	private static boolean isAvailable = false;

	public static void initial(){
		String osName = System.getProperty("os.name");  
		boolean isWindow = osName.matches("^(?i)Windows.*$");
		try{
			if (isWindow) {// Window 系统  
				logger.debug("加载Window版本的PESQ类库");
				System.loadLibrary("libpesq");
			}else {
				logger.debug("加载Linux版本的PESQ类库");
				System.loadLibrary("pesqLib");
			}
		}catch(UnsatisfiedLinkError e){
			logger.error("加载失败，"+e.getMessage());
			//获取java.library.path路径
			String path = System.getProperty("java.library.path");
			logger.debug("获取java.library.path路径："+path);
			try {
				if(isWindow){
					Matcher mat = Pattern.compile("^([^;]+);.*$").matcher(path);
					if(mat.find()){
						if(mat.group(1).matches("^.*"+File.separator+"$")){
							path = mat.group(1);
						}else{
							path = mat.group(1)+File.separator;
						}
					}
					String pesqPath=new File("lib/libpesq.dll").getAbsolutePath();
					logger.debug("从"+pesqPath+"拷贝Window版本的PESQ类库文件到："+path);
					FileUtils.copyFile(new File("lib/libpesq.dll"), new File(path+"libpesq.dll"));
				}else{
					Matcher mat = Pattern.compile("^([^:]+):.*$").matcher(path);
					if(mat.find()){
						if(mat.group(1).matches("^.*"+File.separator+"$")){
							path = mat.group(1);
						}else{
							path = mat.group(1)+File.separator;
						}
					}
					File file = new File("lib/libpesqLib.so");
					File destDir = new File(path);
					FileUtils.copyFileToDirectory(file, destDir);
					logger.debug("拷贝Linux版本的PESQ类库文件到："+path);
				}
			} catch (IOException IOE) {
				logger.error("加载PESQ类库失败",e);
			}
			//第二次加载
			if (isWindow) {// Window 系统  
				System.loadLibrary("libpesq");
				logger.debug("第二次加载Window版本的PESQ类库");
			}else {
				System.loadLibrary("pesqLib");
				logger.debug("第二次加载Linux版本的PESQ类库");
			}
		}
		//初始化PESQ类库
		if((isAvailable = Pesq.init())){
			logger.info("PESQ服务初始化成功！");
		}else{
			logger.info("PESQ服务初始化失败！");
		}
	}
	
	public static Double pesq(String voiceFilePath,String voiceFileName,String recordFilePath){
		if(isAvailable){
	    	//获取录音文件长度
	    	Integer length = Pesq.getLength(recordFilePath);
	    	if(length != null && length>0){
	    		//转换和截取语音文件
	    		if(Pesq.changeFile(voiceFilePath+File.separator+voiceFileName, voiceFilePath+File.separator+"temp_"+voiceFileName, length)){
	    			return Pesq.getPesq(voiceFilePath+File.separator+"temp_"+voiceFileName, recordFilePath);
	    		}
	    	}
		}
    	return null;
    }
}
