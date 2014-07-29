/* PropertiesUtil.java	@date 2005-12-22
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A simple base implementation of Properties file
 * @serial 0.1
 * @version 0.1
 * @author Eagles
 */
public final class PropertiesUtil {
	protected static Logger logger = Logger.getLogger(PropertiesUtil.class);

	private static final String[] FILENAME = {"cfg/config.properties"};
	private static Properties prop = null;

	public static synchronized Properties getProperties() {
 		if (prop == null) {
			prop = new Properties();
			try {
				for(String file : FILENAME){
					prop.load(new FileInputStream(file));
				}
				return prop;
			} catch (IOException ex) {
				logger.error("init an instance from the Properties error:"+ ex.getMessage());
				return null;
			}
		}
		return prop;
	}

	public static String getValue(String param) {
		String result = getProperties().getProperty(param);
		return result == null ? "" : result;
	}
}
