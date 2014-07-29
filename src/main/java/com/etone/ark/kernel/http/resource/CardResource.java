/* CardResponse.java	@date 2013-3-20
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.protocol.CardInfoMessage;
import com.etone.ark.communication.protocol.ResourceBindInfoMessage;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.service.DeviceManager;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;

/**
 * 测试卡相关请求的响应处理程序
 * @version 0.1
 * @author 张浩
 *
 */
@Path("card")
public class CardResource {

	static Logger logger = Logger.getLogger(CardResource.class);
	
	/**
	 * 查找可用测试卡，访问路径：/card/list
	 * @param token
	 * @param timestamp
	 * @return
	 */
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public CardInfoMessage queryCardList(@QueryParam("token") String token,@QueryParam("timestamp") String timestamp){
		try {
		    List<CardInfoMessage.CardInfo> data = new ArrayList<CardInfoMessage.CardInfo>();
		    List<RtdModuleDevice> rtdModuleList = DeviceManager.findRtdModuleDeviceHaveImsi();
		    if(rtdModuleList != null){
		    	for(RtdModuleDevice rtdModule : rtdModuleList){
		    		//判定是否是正常的设备
		    		if(Constants.RTD_MODULE_STATUS_NORMAL == rtdModule.getStatus()){
				    	CardInfoMessage.CardInfo cardInfo = new CardInfoMessage.CardInfo();
		                cardInfo.setDeployType(Constants.CARD_LOCAL); //卡类型：本地卡
		                cardInfo.setImsi(rtdModule.getModuleImsi());
		                cardInfo.setOwerDevice(rtdModule.getId());
		                //位置使用设备序列号
		                cardInfo.setOwerPosition(String.valueOf(DeviceManager.getDeviceID(rtdModule.getId(), rtdModule.getModuleNum())));
		                if(1 == rtdModule.getWorkStatus()){
		                    cardInfo.setWorkStatus("BUSY");
		                }else{
		                    cardInfo.setWorkStatus("IDLE");
		                }
		                data.add(cardInfo);
		    		}
		    	}
		    }
		    List<SidSlotDevice> sidSlotList = DeviceManager.findSidSlotDeviceHaveImsi();
		    if(sidSlotList != null){
		    	for(SidSlotDevice sidSlot : sidSlotList){
		    		//判定是否是正常的设备
		    		if(Constants.SID_SLOT_STATUS_NORMAL == sidSlot.getStatus()){
			    		CardInfoMessage.CardInfo cardInfo = new CardInfoMessage.CardInfo();
	                    cardInfo.setDeployType(Constants.CARD_REMOTE); //卡类型：远程卡
	                    cardInfo.setImsi(sidSlot.getSlotImsi());
	                    cardInfo.setOwerDevice(sidSlot.getId());
	                    //位置使用设备序列号
	                    cardInfo.setOwerPosition(String.valueOf(DeviceManager.getDeviceID(sidSlot.getId(), sidSlot.getSlotNum())));
	                    if(1 == sidSlot.getWorkStatus()){
	                        cardInfo.setWorkStatus("BUSY");
	                    }else{
	                        cardInfo.setWorkStatus("IDLE");
	                    }
	                    data.add(cardInfo);
		    		}
		    	}
		    }
			CardInfoMessage msg = new CardInfoMessage(ResultCode.SUCCESS,"查询可用测试卡成功");
			msg.setCard(data);
			return msg;
		} catch (Exception e) {
			logger.error("查询可用测试卡错误",e);
		}
		
		return new CardInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"查询可用测试卡失败");
	}
	
	/**
	 * 
	 * 查询绑定关系（资源对象），访问路径：/card/bind
	 * @param timestamp
	 * @return
	 */
	@GET
	@Path("bind")
	@Produces(MediaType.APPLICATION_JSON)
	public ResourceBindInfoMessage queryBindInfo(@QueryParam("timestamp") String timestamp,@QueryParam("isBusy") Boolean isBusy){
		try {
		    List<ResourceBindInfoMessage.ResourceBindInfo> data = new ArrayList<ResourceBindInfoMessage.ResourceBindInfo>();
	        //获取所有的资源绑定信息
	        List<Resource> resourceList = DeviceManager.findResourceByWorkStatus(0);
	        for(Resource resource : resourceList){
                ResourceBindInfoMessage.ResourceBindInfo rbi = new ResourceBindInfoMessage.ResourceBindInfo();
                rbi.setImsi(resource.getImsi());
                //生成模块序列号
                Integer serialNum = resource.getRtdSerialNum()*100 + resource.getRtdModuleNum();
                rbi.setModuleSerialNum(serialNum.toString());
                rbi.setCardType(resource.getCardType());
                data.add(rbi);
	        }
			ResourceBindInfoMessage msg = new ResourceBindInfoMessage(ResultCode.SUCCESS,"查询资源绑定信息成功");
			msg.setBind(data);
			return msg;
		} catch (Exception e) {
			logger.error("查询资源绑定信息错误",e);
		}
		
		return new ResourceBindInfoMessage(ResultCode.ER_KERNEL_INTERNAL_ERROR,"查询资源绑定信息失败");
	}
}
