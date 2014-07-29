package com.etone.ark.kernel;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.kernel.service.JavaScriptEngine;
import com.etone.ark.kernel.service.TaskStructureManager;
import com.etone.ark.kernel.service.TaskXmlSolver;
import com.etone.ark.kernel.task.model.TaskStructure;

public class TestTaskConfig {
	
	protected static Logger logger = Logger.getLogger(TestTaskConfig.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(JavaScriptEngine.run("cfg/js/kernel.js")){
			logger.info("JavaScript脚本加载完成！");
		}else{
			logger.error("JavaScript脚本加载失败！");
		}
		ProtocolManager.initial("protocolByte.xml","protocolJson.xml","protocolJson_android.xml");
		TaskStructure structure = TaskXmlSolver.analyzeXml(new File("cfg/task/notifyVoice.xml"));
		String error = TaskStructureManager.checkStructure(structure);
		if(StringUtils.isNotBlank(error)){
			logger.error(error);
		}else{
			logger.info("检查完成");
		}
	}

}
