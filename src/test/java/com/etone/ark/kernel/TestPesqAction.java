package com.etone.ark.kernel;

import java.io.File;
import com.etone.ark.kernel.service.PesqServer;

public class TestPesqAction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FTPClientUtils.initial("FTP://root:etone@192.168.8.245:21");
		PesqServer.initial();
		String voiceFilePath = "_Resource/d1.wav";
    	String recordFilePath = "_Resource/d2.wav";
    	//下载语音文件
    	File voiceFile = FTPClientUtils.get().downFile(voiceFilePath,"./temp/");
    	if(voiceFile != null){
    		//下载录音文件
    		File recordFile = FTPClientUtils.get().waitDownFile(recordFilePath,"./temp/",60*1000L);
    		if(recordFile != null){
    			System.out.println(PesqServer.pesq(voiceFile.getParent(),voiceFile.getName(),recordFile.getPath()));
    		}
    	}

	}

}
