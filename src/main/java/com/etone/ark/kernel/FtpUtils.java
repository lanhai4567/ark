package com.etone.ark.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FtpUtils {

	private static Log log = LogFactory.getLog(FtpUtils.class);

	private FTPClient ftpClient = null;

	private String ip = "127.0.0.1";

	private String username = "test";

	private String password = "test";

	private int port = 2121;

	private String localFileFullName = "";

	public FtpUtils(String serverIP, int port, String username, String password) {
		this.ip = serverIP;
		this.username = username;
		this.password = password;
		this.port = port;
		this.loginFtp();
	}

	public boolean loginFtp() {
		boolean flag = true;
		if (ftpClient == null) {
			int reply;
			try {
				ftpClient = new FTPClient();
				ftpClient.setControlEncoding("utf-8");
//				ftpClient.setFileType(fileType)
				ftpClient.setDefaultPort(port);
				ftpClient.connect(ip);
				ftpClient.login(username, password);
				reply = ftpClient.getReplyCode();
				ftpClient.setDataTimeout(120000);
			} catch (SocketException e) {
				flag = false;
				e.printStackTrace();
				log.error("登录ftp服务器失败,连接超时！");
			} catch (IOException e) {
				flag = false;
				e.printStackTrace();
				log.error("登录ftp服务器失败，FTP服务器无法打开！");
			}

		}
		return flag;

	}

	public boolean logoutFtp() {
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public String getUploadPath(String path) {
		File source = new File(path);
		return source.getPath().substring(localFileFullName.length()).replace('\\', '/');
	}

	/**
	 * 输出到文件
	 * 
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public boolean upload(String fileName,String parentDir) throws Exception {
		boolean flag = true;
		File source = new File(fileName);
		if (source.exists()) {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			if (source.isDirectory()) {
				if (!isDirExist(source.getPath()
						.substring(localFileFullName.length())
						.replace('\\', '/'))) {
					createDir(source.getPath()
							.substring(localFileFullName.length())
							.replace('\\', '/'));
				}
				File sourceFile[] = source.listFiles();
				for (int i = 0; i < sourceFile.length; i++) {
					if (sourceFile[i].exists()) {
						if (sourceFile[i].isDirectory()) {
							this.upload(sourceFile[i].getCanonicalPath(),parentDir);
						} else {
							// 改变目录
							this.changerToPath(sourceFile[i].getCanonicalPath());
							flag = ftpClient.storeFile((StringUtils.isNotEmpty(parentDir)?parentDir+File.separator:"") + sourceFile[i].getName(),
									new FileInputStream(sourceFile[i]));
							log.debug("文件:" + sourceFile[i].getName() + "上传"
									+ (flag == true ? "成功" : "失败"));
							// 返回根目录
							ftpClient.changeWorkingDirectory("~");
						}
					}
				}
			} else {
				try {
					FileInputStream bis = new FileInputStream(source);
					this.changerToPath(source.getCanonicalPath());
					ftpClient.changeWorkingDirectory("/");
					flag = ftpClient.storeFile((StringUtils.isNotEmpty(parentDir)?parentDir+File.separator:"") + source.getName(), bis);
					log.debug("文件:" + source.getName() + "上传"
							+ (flag == true ? "成功" : "失败"));
					ftpClient.changeWorkingDirectory("~");
					bis.close();
				} catch (Exception e) {
					e.printStackTrace();
					log.debug("本地文件上传失败！" + source.getCanonicalPath(), e);
				}
			}
		}
		return flag;
	}

	/**
	 * 获取当前的FTP路径
	 * 
	 * @param path
	 * @return
	 */
	private boolean changerToPath(String path) {
		boolean isOK = false;
		path = path.substring(localFileFullName.length()).replace('\\', '/');
		StringTokenizer s = new StringTokenizer(path, "/");
		s.countTokens();
		String pathName = "";
		while (s.hasMoreElements()) {
			pathName = (String) s.nextElement();
			try {
				isOK = ftpClient.changeWorkingDirectory(pathName);
			} catch (Exception e) {
				e = null;
				isOK = false;
			}
		}
		return isOK;
	}

	/**
	 * 创建文件夹
	 * 
	 * @param dir
	 * @param ftpClient
	 * @throws Exception
	 */
	public void createDir(String dir) throws Exception {
		StringTokenizer s = new StringTokenizer(dir, "/");
		s.countTokens();
		String pathName = "";
		while (s.hasMoreElements()) {
			pathName = (String) s.nextElement();
			try {
				ftpClient.makeDirectory(pathName);
				ftpClient.changeWorkingDirectory(pathName);
			} catch (Exception e) {
				e = null;
			}
		}
		ftpClient.changeWorkingDirectory("~");
	}

	/**
	 * 检查文件夹是否存在
	 * 
	 * @param dir
	 * @param ftpClient
	 * @return
	 */
	private boolean isDirExist(String dir) {
		boolean isDirExist = false;
		try {
			isDirExist = ftpClient.changeWorkingDirectory(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isDirExist;
	}

	public void setLocalFileFullName(String localFileFullName) {
		this.localFileFullName = localFileFullName;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		FtpUtils packer = new FtpUtils("127.0.0.1", 2121, "test", "test");
		packer.upload("F:/myself.jpg","");
		packer.logoutFtp();
		
//		System.out.println(new File("E:/ark-kernel/info.log").exists());
	}

}