/* TaskResource.java	@date 2013-3-15
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.communication.JsonUtils;
import com.etone.ark.communication.ParameterCode;
import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.StatusChangeService;
import com.etone.ark.communication.protocol.ResourceInfoMessage;
import com.etone.ark.communication.protocol.ResponseMessage;
import com.etone.ark.communication.protocol.TaskInfoMessage;
import com.etone.ark.communication.protocol.TaskTypeInfoMessage;
import com.etone.ark.communication.protocol.TaskTypeInfoMessage.TaskTypeInfo;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.DateUtils;
import com.etone.ark.kernel.exception.ResourceException;
import com.etone.ark.kernel.remote.RemoteCityServer;
import com.etone.ark.kernel.service.ContainerService;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.TaskManager;
import com.etone.ark.kernel.service.TaskStructureManager;
import com.etone.ark.kernel.service.TaskXmlSolver;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.task.NodeStartHandler;
import com.etone.ark.kernel.task.model.Task;
import com.etone.ark.kernel.task.model.TaskStructure;

/**
 * 测试业务HTTP资源
 * @version 0.2
 * @author 张浩
 *
 */
@Path("task")
public class TaskResource {
	
	static Logger logger = Logger.getLogger(TaskResource.class);

	/**
	 * 测试项执行响应，访问路径：/task/execute
	 * @since 0.2
	 * @param token
	 * @param code
	 * @param type
	 * @param taskParam
	 * @param resourceParamJson
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @POST
	@Path("execute")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseMessage taskExecute(@FormParam("token") String token,
							    @FormParam("timestamp") String timestamp,
								@FormParam("code") String code,
								@FormParam("type") String type,
								@FormParam("taskParam") String taskParam,
								@FormParam("resourceParam") String resourceParam){
		//参数验证，业务类型与自定义XML不能同时为空
		if(StringUtils.isEmpty(token) || StringUtils.isEmpty(code) || StringUtils.isEmpty(resourceParam) 
				|| StringUtils.isEmpty(type)){
			ResponseMessage response = new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"参数不合法");
			return response;
		}
		try{
			//解析资源数据
			ResourceInfoMessage resMsg = JsonUtils.fromJson(resourceParam, ResourceInfoMessage.class);
			if(resMsg == null || resMsg.getResource() == null || resMsg.getResource().size() <= 0){
				return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"下发测试项失败，无效的资源信息");
			}
			Map<String,Object> parameters = null;
			//解析测试项运行参数
			if(StringUtils.isNotEmpty(taskParam)){
				try{
					parameters = JsonUtils.fromJson(taskParam, Map.class);
				}catch(Exception e){
					return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"下发测试项失败，测试项运行参数JSON转换失败");
				}
			}else{
				parameters = new HashMap<String,Object>();
			}
			
			//创建测试项对象
	        Task task = new Task();
	        task.setToken(token); //设置客户端Token
	        task.setId(code); //设置测试业务ID
	        task.setType(type);
	        task.setParameters(parameters);
	        int error = TaskManager.checkTask(task);
	        if(error == 0){
	            try{
	                final NodeStartHandler handler = new NodeStartHandler(task,task.getStructure().getStartNode());
	                //检查资源
	                for(int i=0;i<resMsg.getResource().size();i++){
	                    ResourceInfoMessage.ResourceInfo info = resMsg.getResource().get(i);
	                    try{
	                    	if(info.getModuleSerialNum() == null || StringUtils.isBlank(info.getImsi())){
	                    		return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"下发测试项失败，无效的资源信息");
	                    	}
                            //计算设备序列号
                            Integer moduleSerialNum = Integer.valueOf(info.getModuleSerialNum());
                            Integer rtdSerialNum = moduleSerialNum/100;
                            Integer rtdModuleNum = moduleSerialNum%100;
                            Resource resource = null;
                            if(info.getSystemId() == null || info.getSystemId().equals(Constants.SYSTEM_ID)){
                                info.setSystemId(Constants.SYSTEM_ID);
                                resource = DeviceManager.findAndEmployResource(rtdSerialNum,rtdModuleNum,info.getSystemId(),info.getImsi(),info.getNetType(),info.getTestNum());
                            }else if(RemoteCityServer.getChannel() != null && RemoteCityServer.getChannel().isAvailable()){
                                //地市设备
                                /*
                                                            Integer sidSerialNum = null;
                            Integer slotNum = null;
                            Integer cardType = 0;
                            if(info.getSlotSerialNum() != null){
                                cardType = 1;
                                sidSerialNum = info.getSlotSerialNum()/100;
                                slotNum = info.getSlotSerialNum()%100;
                            }
                             	resource = new Resource(rtdSerialNum + 20000,rtdModuleNum,info.getImsi());
                                //resource = new Resource(rtdSerialNum,rtdModuleNum,info.getImsi());
                                resource.setCardType(cardType);
                                resource.setSystemId(info.getSystemId());
                                DeviceRtdModule rtdModule = new DeviceRtdModule();
                                rtdModule.setSystemId(info.getSystemId());
                                rtdModule.setSerialNum(rtdSerialNum);
                                rtdModule.setModuleNum(rtdModuleNum);
                                rtdModule.setChannel(RemoteCityServer.getChannel());
                                rtdModule.setStatus(Constants.RTD_MODULE_STATUS_NORMAL);
                                resource.setRemoteRtdModule(rtdModule);
                                
                                if(resource.getCardType() == Constants.CARD_REMOTE){
	                                resource.setSidSerialNum(sidSerialNum);
	                                resource.setSidSlotNum(slotNum);
	                                DeviceSidSlot sidSlot = new DeviceSidSlot();
	                                sidSlot.setSystemId(info.getSystemId());
	                                sidSlot.setSerialNum(sidSerialNum);
	                                sidSlot.setSlotNum(slotNum);
	                                sidSlot.setChannel(RemoteCityServer.getChannel());
	                                sidSlot.setStatus(Constants.SID_SLOT_STATUS_NORMAL);
	                                resource.setRemoteSidSlot(sidSlot);
                                }*/
                            }
                            if(resource != null){
                                task.getResources().put(Constants.RESOURCES[i], resource);
                            }else{
                                //资源调度失败，释放以获取的资源
                                for(Resource temp :task.getResources().values()){
                                	DeviceManager.unemployResource(temp);
                                }
                                return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,info.getModuleSerialNum() + "资源不存在，请检查设备或SystemId");
                            }
                        }catch(Exception e){
                            //资源调度失败，释放以获取的资源
                        	for(Resource temp :task.getResources().values()){
                            	DeviceManager.unemployResource(temp);
                            }
                        	if(ResourceException.class.isInstance(e)){
                        		return new ResponseMessage(ResourceException.class.cast(e).getErrorCode(),e.getMessage());
                        	}else{
                        		return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,e.getMessage());
                        	}
                        }
	                }
	                //保存测试项
	                TaskManager.saveOrUpdate(task);
	                ContainerService.execute(handler);
	                logger.info("[HTTP-SERVICE][EXECUTE-TASK]["+token+"]["+code+"]["+type+"][success]");
	                return new ResponseMessage(ResultCode.SUCCESS,"执行测试项成功");
	            }catch(Exception e){
	                logger.error("[HTTP-SERVICE][EXECUTE-TASK]["+token+"]["+code+"]["+type+"][error]",e);
	            }
	        }else if(error == 1){
	            return new ResponseMessage(ResultCode.ER_KERNEL_TASK_DUPLICATE,"测试项ID重复");
	        }else if(error == 2){
	            return new ResponseMessage(ResultCode.ER_KERNEL_TESTTYPE_NOT_EXIST,"无效的测试业务类型");
	        }

	        return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"测试项执行失败");
		}catch(Exception e){
			logger.error("[HTTP-SERVICE][EXECUTE-TASK]error", e);
			return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"微内核内部错误:"+e.getMessage());
		}
	}
	
	/**
	 * 中断测试项响应，访问路径：/task/interrupt
	 * @param token
	 * @param timestamp
	 * @param code
	 * @param range 取值范围[all：中断所有、single：中断指定业务]
	 * @return
	 */
	@Path("interrupt")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseMessage taskInterrupt(@FormParam("token") String token,
								  @FormParam("timestamp") String timestamp,
								  @FormParam("code") String code,
								  @FormParam("range") String range){
		if(StringUtils.isEmpty(range)|| (ParameterCode.RANGE_SINGLE.equals(range) && StringUtils.isEmpty(code))){
			return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"参数错误");
		}
		try {
			if(ParameterCode.RANGE_ALL.equals(range)){
			    //获取测试业务
	            Collection<Task> tasks = TaskManager.findAll();
	            if(tasks != null){
	                for(Task task : tasks){
	                    //调用中断功能
	                    TaskManager.InterruptTask(task,Constants.TASK_INTERRUPT,"测试中断");
	                }
	            }
	            return new ResponseMessage(ResultCode.SUCCESS,"中断测试项成功");
			}else if(ParameterCode.RANGE_SINGLE.equals(range)){
	            //获取测试业务
	            Task task = TaskManager.get(code);
	            if(task != null){
	                //调用中断功能
	                TaskManager.InterruptTask(task,Constants.TASK_INTERRUPT,"测试中断");
	            }else{
	                return new ResponseMessage(ResultCode.ER_KERNEL_TESTTYPE_NOT_EXIST,"测试任务 "+code+" 不存在");
	            }
		        return new ResponseMessage(ResultCode.SUCCESS,"中断测试项成功");
			}else{
				return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"range参数错误");
			}
		} catch (Exception e) {
			logger.error("中断测试业务错误",e);
			return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"内部错误");
		}
		
	}

	/**
	 * 查询正在执行的测试项，访问路径：/task/list
	 * @param token
	 * @param timestamp
	 * @param code
	 * @param range
	 * @return
	 */
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TaskInfoMessage taskList(@QueryParam("token") String token,@QueryParam("timestamp") String timestamp,@QueryParam("code") String code, @QueryParam("range") String range){
		if(StringUtils.isEmpty(range)|| (ParameterCode.RANGE_SINGLE.equals(range) && StringUtils.isEmpty(code))){
			return new TaskInfoMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"参数错误");
		}
		try {
			if(ParameterCode.RANGE_ALL.equals(range)){
			    List<TaskInfoMessage.TaskInfo> data = new ArrayList<TaskInfoMessage.TaskInfo>();
		        //查找所有测试业务
		        Collection<Task> taskList = TaskManager.findAll();
		        for(Task business : taskList){
		            TaskInfoMessage.TaskInfo info = new TaskInfoMessage.TaskInfo();
		            info.setCode(business.getId());
		            info.setType(business.getStructure().getCode());
		            //转换成对应的状态
		            info.setStatus(StatusChangeService.changeBusinessRunStatus(business.getRunState()));
		            //运行参数
		            if(business.getParameters() != null){
		                String taskParam = JsonUtils.toJson(business.getParameters());
		                info.setTaskParam(taskParam);
		            }
		            //测试业务开始时间
		            if(business.getStartDate() != null){
		                info.setBeginTime(DateUtils.convertDateToString(business.getStartDate()));
		            }
		            //测试业务结束时间
		            if(business.getEndDate() != null){
		                info.setEndTime(DateUtils.convertDateToString(business.getEndDate()));
		            }
		            //转换资源对象
		            if(business.getResources()!= null){
		                info.setResourceParam(new TaskInfoMessage.ResourceParam());
		                for(Resource resource : business.getResources().values()){
		                    TaskInfoMessage.ResourceInfo resourceInfo = new TaskInfoMessage.ResourceInfo();
		                    //计算序列号
		                    Integer serialNum = resource.getRtdSerialNum() * 100 + resource.getRtdModuleNum();
		                    //TODO 如果是本地卡，这里的定义有问题
		                    resourceInfo.setModuleSerialNum(serialNum.toString());
		                    resourceInfo.setImsi(resource.getImsi());
		                    resourceInfo.setTestNum(resource.getTestNum());
		                    //加入到报文中
		                    info.getResourceParam().getResource().add(resourceInfo);
		                }
		            };
		            //加入到报文中
		            data.add(info);
		        }
				TaskInfoMessage msg = new TaskInfoMessage(ResultCode.SUCCESS,"查询测试项列表成功");
				msg.setTask(data);
				return msg;
			}else if(ParameterCode.RANGE_SINGLE.equals(range)){
			    Task task = TaskManager.get(code);
		        if(task != null){
		            TaskInfoMessage.TaskInfo info = new TaskInfoMessage.TaskInfo();
		            info.setCode(task.getId());
		            info.setType(task.getStructure().getCode());
		            //转换成对应的状态
		            info.setStatus(StatusChangeService.changeBusinessRunStatus(task.getRunState()));
		            //运行参数
		            if(task.getParameters() != null){
		                String taskParam = JsonUtils.toJson(task.getParameters());
		                info.setTaskParam(taskParam);
		            }
		            //测试业务开始时间
		            if(task.getStartDate() != null){
		                info.setBeginTime(DateUtils.convertDateToString(task.getStartDate()));
		            }
		            //测试业务结束时间
		            if(task.getEndDate() != null){
		                info.setEndTime(DateUtils.convertDateToString(task.getEndDate()));
		            }
		            //转换资源对象
		            if(task.getResources()!= null){
		                info.setResourceParam(new TaskInfoMessage.ResourceParam());
		                for(Resource resource : task.getResources().values()){
		                    TaskInfoMessage.ResourceInfo resourceInfo = new TaskInfoMessage.ResourceInfo();
		                    //计算序列号
		                    Integer serialNum = resource.getRtdSerialNum() * 100 + resource.getRtdModuleNum();
		                    //TODO 如果是本地卡，这里的定义有问题
		                    resourceInfo.setModuleSerialNum(serialNum.toString());
		                    resourceInfo.setImsi(resource.getImsi());
		                    resourceInfo.setTestNum(resource.getTestNum());
		                    //加入到报文中
		                    info.getResourceParam().getResource().add(resourceInfo);
		                }
		            };
		            logger.debug("[HTTP-SERVICE][FIND-SINGLE-TASK]["+code+"]{status:"+info.getStatus()+"}");
		            TaskInfoMessage msg = new TaskInfoMessage(ResultCode.SUCCESS,"查询测试项成功");
	                msg.setTask(new ArrayList<TaskInfoMessage.TaskInfo>());
	                msg.getTask().add(info);
	                return msg;
		        }
			}else{
				return new TaskInfoMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"range参数错误");
			}
		} catch (Exception e) {
			logger.error("查询测试项列表错误",e);
		}
		
		return new TaskInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"查询测试业务列表失败");
	}
	
	/**
	 * 注册测试业务类型
	 * @param token
	 * @param timestamp
	 * @param code
	 * @param range
	 * @return
	 */
	@Path("type/register")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseMessage registerType(
			@FormParam("token") String token,
			@FormParam("timestamp") String timestamp,
			@FormParam("script") String xml){
		if(StringUtils.isEmpty(xml)){
			return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"参数错误");
		}
		try {
		    TaskStructure structure = TaskXmlSolver.analyzeXml(xml);
	        if(structure != null){
	            //如果已存在就报错
	            if(TaskStructureManager.get(structure.getCode()) != null){
	                return new ResponseMessage(ResultCode.ER_KERNEL_TASK_DUPLICATE,"测试业务类型已存在");
	            }
	            String error = TaskStructureManager.checkStructure(structure);
	            if(StringUtils.isNotEmpty(error)){
	                return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,error);
	            }
	            //创建文件
	            File file = new File("cfg/business/" + structure.getCode() + ".xml");
	            if(!file.exists()){file.createNewFile();}
	            FileOutputStream fos = new FileOutputStream(file);
	            OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
	            BufferedWriter output = new BufferedWriter(osw);
	            output.write(xml);
	            output.flush();
	            output.close();
	            osw.close();
	            fos.close();
	            structure.setXml(xml);//保存配置文件内容
	            TaskStructureManager.saveOrUpdate(structure);//保存缓存
	            return new ResponseMessage(ResultCode.SUCCESS,"注册测试业务类型成功");
	        }else{
	            return  new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"无效的测试业务流程脚本");
	        }
		} catch (Exception e) {
			logger.error("注册测试业务类型错误",e);
		}
		return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"注册测试业务类型失败");
	}
	
	/**
	 * 注销测试业务类型
	 * @param token
	 * @param timestamp
	 * @param code
	 * @param range
	 * @return
	 */
	@Path("type/unregister")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseMessage unregisterType(
			@FormParam("token") String token,
			@FormParam("timestamp") String timestamp,
			@FormParam("type") String type){
		if(StringUtils.isEmpty(type)){
			return new ResponseMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"参数错误");
		}
		try {
		    TaskStructure structure = TaskStructureManager.get(type);
	        if(structure == null){
	            return new ResponseMessage(ResultCode.ER_KERNEL_TASK_NOT_EXIST,"测试业务类型不存在");
	        }
	        //移除缓存
	        TaskStructureManager.delete(structure.getCode());
	        //删除文件
	        File file = new File("cfg/business/" + type + ".xml");
	        if(file.exists()){
	            file.delete();
	        }
	        return new ResponseMessage(ResultCode.SUCCESS,"注销测试业务类型成功");
		} catch (Exception e) {
			logger.error("注销测试业务类型错误",e);
		}
		return new ResponseMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"注销测试业务类型失败");
	}
	
	/**
	 * 查询测试业务类型
	 * @param token
	 * @param timestamp
	 * @param code
	 * @param range
	 * @return
	 */
	@Path("type/list")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public TaskTypeInfoMessage typeList(
			@FormParam("token") String token,
			@FormParam("timestamp") String timestamp,
			@FormParam("type") String type, 
			@FormParam("range") String range){
		if(StringUtils.isEmpty(range)|| (ParameterCode.RANGE_SINGLE.equals(range) && StringUtils.isEmpty(type))){
			return new TaskTypeInfoMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"参数错误");
		}
		try {
			if(ParameterCode.RANGE_ALL.equals(range)){
			    Collection<TaskStructure> structures = TaskStructureManager.findAll();
		        List<TaskTypeInfo> data = new ArrayList<TaskTypeInfo>();
		        for(TaskStructure structure:structures){
		            TaskTypeInfo info = new TaskTypeInfo();
		            info.setType(structure.getCode());
		            info.setScript(structure.getXml());
		            data.add(info);
		        }
				TaskTypeInfoMessage msg = new TaskTypeInfoMessage(ResultCode.SUCCESS,"查询测试业务类型列表成功");
				msg.setTaskType(data);
				return msg;
			}else if(ParameterCode.RANGE_SINGLE.equals(range)){
			    TaskStructure structure = TaskStructureManager.get(type);
		        if(structure != null){
		            TaskTypeInfo info = new TaskTypeInfo();
		            info.setType(structure.getCode());
		            info.setScript(structure.getXml());
					TaskTypeInfoMessage msg = new TaskTypeInfoMessage(ResultCode.SUCCESS,"查询测试业务类型成功");
					msg.setTaskType(new ArrayList<TaskTypeInfoMessage.TaskTypeInfo>());
					msg.getTaskType().add(info);
					return msg;
				}
			}else{
				return new TaskTypeInfoMessage(ResultCode.ER_KERNEL_PARAMETER_ERROR,"range参数错误");
			}
			
		} catch (Exception e) {
			logger.error("查询测试业务类型错误",e);
		}
		return new TaskTypeInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"查询测试业务类型失败");
	}
}
