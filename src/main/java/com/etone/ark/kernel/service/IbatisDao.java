/* IbatisDao.java	@date 2013-4-27
 * @JDK 1.6
 * @encoding UTF-8
 * <p>版权所有：宜通世纪</p>
 * <p>未经本公司许可，不得以任何方式复制或使用本程序任何部分 </p>
 */
package com.etone.ark.kernel.service;

import java.io.IOException;
import java.io.Reader;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 * 使用Ibatis框架的抽象类
 * @version 1.2
 * @author 张浩
 *
 */
public abstract class IbatisDao {

	protected static SqlMapClient sqlmap;
	
	public IbatisDao(String configFilePath) throws IOException{
		Reader reader = Resources.getResourceAsReader(configFilePath);
		sqlmap = SqlMapClientBuilder.buildSqlMapClient(reader);
	}
}
