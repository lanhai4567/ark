/* ClientEventInfoDaoImpl.java @date 2012-12-20
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.etone.ark.kernel.service.IbatisDao;

public class ClientEventInfoDao extends IbatisDao{
    
    static Logger logger = Logger.getLogger(ClientEventInfoDao.class);
    
	public ClientEventInfoDao(String configFilePath) throws IOException {
        super(configFilePath);
    }

	public ClientEventInfo getByTokenAndName(String token,String name){
		if(StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(name)){
			try{
				ClientEventInfo eventInfo = new ClientEventInfo();
				eventInfo.setToken(token);
				eventInfo.setName(name);
				Object obj = sqlmap.queryForObject("getByTokenAndName",eventInfo);
				if(obj != null){
					return ClientEventInfo.class.cast(obj);
				}
			} catch (SQLException e) {
				logger.error("查找指定客户端事件错误", e);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<ClientEventInfo> findByToken(String token){
		if(StringUtils.isNotEmpty(token)){
			try{
				return sqlmap.queryForList("selectClientEventByToken",token);
			} catch (SQLException e) {
				logger.error("根据TOKEN获取客户端事件错误", e);
			}
		}
		return null;
	}
	
	
	
	public boolean saveOrUpdate(ClientEventInfo clientEventInfo) {
		try {
			if(getByTokenAndName(clientEventInfo.getToken(),clientEventInfo.getName()) != null){
				sqlmap.update("updateClientEvent",clientEventInfo);
			}else{
				sqlmap.insert("insertClientEvent",clientEventInfo);
			}
			return true;
		} catch (SQLException e) {
			logger.error("保存或修改客户端事件信息缓存错误", e);
		}
		return false;
	}
	
	public boolean deleteByTokenAndName(String token,String name){
		if(StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(name)){
			try{
				ClientEventInfo eventInfo = new ClientEventInfo();
				eventInfo.setToken(token);
				eventInfo.setName(name);
				sqlmap.delete("deleteClientEventByTokenAndName",eventInfo);
				return true;
			} catch (SQLException e) {
				logger.error("删除客户端事件信息错误", e);
			}
		}
		return false;
	}
	
	public boolean deleteByToken(String token){
		if(StringUtils.isNotEmpty(token)){
			try{
				sqlmap.delete("deleteClientEventByToken",token);
				return true;
			} catch (SQLException e) {
				logger.error("删除客户端事件信息错误", e);
			}
		}
		return false;
	}
}
