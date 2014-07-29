package com.etone.ark.kernel.service;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.ProtocolManager;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.task.model.Node;
import com.etone.ark.kernel.task.model.TaskStructure;
import com.etone.ark.kernel.task.model.Transition;

public class TaskStructureManager {
    
    static Logger logger = Logger.getLogger(TaskStructureManager.class);

    private final static ConcurrentMap<String,TaskStructure> STORE = new ConcurrentHashMap<String,TaskStructure>();
    
    /**
     * 初始化业务管理
     * @param path  测试业务配置路径
     */
    public static void initial(String path){
        File file = new File(path);
        if(file.exists()){
            //如果是文件，那么就解析文件
            if(file.isFile()){
                if(file.getName().indexOf(".xml")>0){
                    TaskStructure structure = TaskXmlSolver.analyzeXml(file);
                    if(structure != null){
                        //检测业务结构是否正确
                        String error = checkStructure(structure);
                        if(error == null){
                            //读取文件
                            structure.setXml(readFile(file));
                            saveOrUpdate(structure);
                        }else{
                            logger.error(error);
                        }
                    }
                }
            }else{
                for(int i=0;i<file.listFiles().length;i++){
                    File temp = file.listFiles()[i];
                    initial(temp.getPath());
                }
            }
        }
    }
    
    /**
     * 读取文件
     * @param file
     * @return
     */
    public static String readFile(File file){
        StringBuffer xml = new StringBuffer();
        try {
            FileReader reader = new FileReader(file);
            char[] tempchars = new char[30];
            int charread = 0;
            // 读入多个字符到字符数组中，charread为一次读取字符数
            while ((charread = reader.read(tempchars)) != -1) {
                // 同样屏蔽掉\r不显示
                if ((charread == tempchars.length)
                        && (tempchars[tempchars.length - 1] != '\r')) {
                    xml.append(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        if (tempchars[i] == '\r') {
                            continue;
                        } else {
                            xml.append(tempchars[i]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml.toString();
    }
    
    /**
     * 检测业务结构是否正确
     * @param structure 业务结构
     * @return  错误信息或NULL
     */
    public static String checkStructure(TaskStructure structure){
        if(structure != null){
            //检测开始节点
            if(structure.getStartNode() == null){
                return structure.getCode() + "业务结构，开始节点不存在";
            }else{
                //检测连接线
                if(structure.getStartNode().getTransitions() == null 
                        || structure.getStartNode().getTransitions().size() <= 0){
                    return structure.getCode() + "业务结构，开始节点无连接线";
                }else{
                    for(Transition line :structure.getStartNode().getTransitions()){
                        if(StringUtils.isNotEmpty(line.getToNodeId())){
                            if(!"end".equals(line.getToNodeId()) && structure.getNodeGroup().get(line.getToNodeId()) == null){
                                return structure.getCode() + "业务结构，开始节点存在连接线指向错误";
                            }
                        }else{
                            return structure.getCode() + "业务结构，开始节点存在无效连接线";
                        }
                    }
                }
            }
            //检测节点组
            for(Node node : structure.getNodeGroup().values()){
                if(!node.getId().matches("^[a-zA-Z0-9_]+$")){
                    return structure.getCode() + "业务结构，"+node.getId() +"节点的ID命名不合法！";

                }
                if(Constants.NODE_TYPE_ACTION.equals(node.getType())){
                    //检测测试动作是否正确
                    Object actionType = node.getParameter().get("actionType");
                    if(actionType != null){
                        if(ProtocolManager.getProtocol(structure.getVersion(),actionType.toString()) == null){
                            return structure.getCode() + "业务结构，"+node.getId() +"节点的测试动作错误";
                        }
                    }else{
                        return structure.getCode() + "业务结构，"+node.getId() +"节点的测试动作错误";
                    }
                }else if(Constants.NODE_TYPE_FORK.equals(node.getType())){
                    //检查JOIN节点
                    Object join = node.getParameter().get("join");
                    if(join != null && StringUtils.isNotEmpty(join.toString())){
                        if(!structure.getEndNode().getId().equals(join) && structure.getNodeGroup().get(join) == null){
                            return structure.getCode() + "业务结构，"+node.getId() +"节点JOIN属性指向错误";
                        }
                    }else{
                        return structure.getCode() + "业务结构，"+node.getId() +"节点无JOIN属性";
                    }
                }
                
                //检测连接线
                if(node.getTransitions() == null || node.getTransitions().size() <= 0){
                    return structure.getCode() + "业务结构，"+node.getId() +"节点无连接线";
                }else{
                    for(Transition line :node.getTransitions()){
                        if(StringUtils.isNotEmpty(line.getToNodeId())){
                            if(!structure.getEndNode().getId().equals(line.getToNodeId()) && structure.getNodeGroup().get(line.getToNodeId()) == null){
                                return structure.getCode() + "业务结构，"+node.getId() +"节点存在连接线指向错误";
                            }
                        }else{
                            return structure.getCode() + "业务结构，"+node.getId() +"节点存在无效连接线";
                        }
                    }
                }
            }
            //检测结束节点
            if(structure.getEndNode() == null){
                return structure.getCode() + "业务结构，结束节点不存在";
            }
        }else{
            return "业务结构为空";
        }
        return null;
    }
    
    public static boolean saveOrUpdate(TaskStructure structure) {
        try{
            STORE.put(structure.getCode(),structure);
            return true;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            return false;
        }
    }

    public static TaskStructure get(String code) {
        if(code != null){
            return STORE.get(code);
        }
        return null;
    }

    public static boolean delete(String code) {
        try{
            STORE.remove(code);
            return true;
        }catch(Exception ex){
            logger.error(ex.getMessage());
            return false;
        }
    }

    public static Collection<TaskStructure> findAll() {
        return STORE.values();
    }
}
