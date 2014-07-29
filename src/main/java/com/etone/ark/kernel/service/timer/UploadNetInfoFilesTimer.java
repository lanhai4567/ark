package com.etone.ark.kernel.service.timer;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.kernel.FtpUtils;
import com.etone.ark.kernel.PropertiesUtil;

public class UploadNetInfoFilesTimer{

	private static final Logger logger = Logger.getLogger(UploadNetInfoFilesTask.class);
	private static final DateFormat format = new SimpleDateFormat("yyyyMMdd");
	
	private static Timer timer = new Timer();
	
	public static void start(){
		timer.schedule(new UploadNetInfoFilesTask(),0,10000);
	}
	
	private static class UploadNetInfoFilesTask extends TimerTask{
		@Override
		public void run() {
			File file = new File("cellinfo");
			if(file.isDirectory()){
				File[] files = file.listFiles(new FileFilter(){
	
					@Override
					public boolean accept(File pathname) {
						if(pathname.getName().endsWith(".txt")){
							return true;
						}
						return false;
					}
					
				});
				
				String rtdFtp = PropertiesUtil.getValue("RTDFtpService");
				Matcher matcher = Pattern.compile("FTP://(\\w+):(\\w+)@(\\d+.\\d+.\\d+.\\d+)((:(\\d+))?)").matcher(rtdFtp);
				
				if(matcher.matches()){
					FtpUtils ftpUtils = new FtpUtils(matcher.group(3),
							StringUtils.isEmpty(matcher.group(6))?21:Integer.parseInt(matcher.group(6)),matcher.group(1),matcher.group(2));
					
					for(File f : files){
						try {
							String parentDir = format.format(new Date());
 							ftpUtils.createDir("cellinfo/"+parentDir);
							boolean flag = ftpUtils.upload(f.getAbsolutePath(),"cellinfo"+File.separator+parentDir);
							if(flag){
								f.delete();
								logger.info("上传文件"+f.getName()+"成功!");
							}else{
								logger.info("上传文件"+f.getName()+"失败!");
							}
						} catch (Exception e) {
							logger.error("上传文件"+f.getName()+"失败!");
							e.printStackTrace();
						}
					}
					ftpUtils.logoutFtp();
				}else{
					logger.error("配置文件配置的，FTP服务器格式错误!");
				}
			}
		}
	}
}
