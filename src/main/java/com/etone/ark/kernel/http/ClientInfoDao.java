/* ClientInfoDaoImpl.java @date 2012-12-20
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.http;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.service.H2DataBaseServer;
import com.etone.ark.kernel.service.IbatisDao;

public class ClientInfoDao extends IbatisDao{
	
    static Logger logger = Logger.getLogger(ClientInfoDao.class);
    
	public ClientInfoDao(String configFilePath) throws IOException {
        super(configFilePath);
    }

	public boolean saveOrUpdate(ClientInfo clientInfo) {
		try {
			if(get(clientInfo.getToken()) != null){
				sqlmap.update("updateClient",clientInfo);
			}else{
				sqlmap.update("insertClient",clientInfo);
			}
			return true;
		} catch (SQLException e) {
			logger.error("保存或修改客户端信息缓存错误", e);
		}
		return false;
	}

	public boolean delete(String token) {
		try {
			sqlmap.delete("deleteClientByToken",token);
			H2DataBaseServer.getClientEventInfoDao().deleteByToken(token);
			return true;
		} catch (SQLException e) {
			logger.error("删除客户端信息缓存错误", e);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<ClientInfo> findAll() {
		try {
			return sqlmap.queryForList("selectAllClient");
		} catch (SQLException e) {
			logger.error("获取所有客户端信息缓存错误", e);
		}
		return null;
	}

	public ClientInfo get(String token) {
		try {
			Object obj = sqlmap.queryForObject("getClientByToken",token);
			if(obj != null){
				return ClientInfo.class.cast(obj);
			}
		} catch (SQLException e) {
			logger.error("获取客户端信息缓存错误", e);
		}
		return null;
	}
}
