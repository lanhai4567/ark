package com.etone.ark.kernel;

import java.io.File;

import com.etone.ark.kernel.service.PesqServer;

public class TestPesqServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PesqServer.initial();
		File voiceFile = new File("./temp/_Resource/test.wav");
		File recordFile = new File("./temp/_Resource/test.wav");
		System.out.println(voiceFile.getParent());
		System.out.println(voiceFile.getName());
		System.out.println(recordFile.getPath());
		if(voiceFile.exists() && recordFile.exists()){
			System.out.println(PesqServer.pesq(voiceFile.getParent(),voiceFile.getName(),recordFile.getPath()));
		}else{
			System.out.println("无法找到");
		}
	}

}
