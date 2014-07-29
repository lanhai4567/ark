package com.etone.ark.kernel;

import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.kernel.service.ExpressionSolver;
import com.etone.ark.kernel.service.TaskStructureManager;

public class Test {

    /**
     * 功能描述：
     * 
     * @param args 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-8-9
     */
    public static void main(String[] args) {
      //初始化JS脚本库【 level - 2】
        ExpressionSolver.initJavaScriptFile("cfg/js/kernel.js");
        
        //初始化资源命令服务【 level - 3】
        ProtocolManager.initial("protocolByte.xml","protocolJson.xml");
        
        //初始化业务结构，必须初始化完动作信息后才进行，在内部需要配置的动作是否正确【 level - 3 】
        TaskStructureManager.initial("cfg/task/p2pVoiceBak.xml");
        
        System.out.println("测试完成");
    }

}
