package com.etone.ark.kernel.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.http.ClientEventInfoDao;
import com.etone.ark.kernel.http.ClientInfoDao;

/**
 * 功能描述：H2内存数据库服务
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 * 
 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
 * @version 1.0.0
 * @since 1.0.0 
 * create on: 2013-9-25 
 */
public class H2DataBaseServer {
    
    private static ClientEventInfoDao clientEventInfoDao;
    private static ClientInfoDao clientInfoDao;
   
    private static JdbcConnectionPool pool;

	/**
	 * 功能描述：初始化内存数据库，创建以下内存表：
	 *     1. temporay_device表
	 * 
	 * @throws Exception 
	 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
	 * @version 1.0.0
	 * @since 1.0.0
	 * create on: 2013-9-25
	 */
	public static void run() throws Exception{
	    Class.forName("org.h2.Driver");
	    pool= JdbcConnectionPool.create("jdbc:h2:mem:test", "sa", "");
	    pool.setMaxConnections(30);
	    //初始化表结构
	    Connection conn = pool.getConnection();
	    Statement stmt = conn.createStatement();
	    //创建temporay_device表结构
	    stmt.executeUpdate("CREATE TABLE temporay_device(id INT,systemId INT,index INT,status INT,socketChannelId INT);");
	    //创建rtd_device表结构
	    stmt.executeUpdate("CREATE TABLE rtd_device(id INT,systemId INT,status INT,socketChannelId INT,linkTime TIMESTAMP,heartBeatTime TIMESTAMP,workStatus INT" 
	    				+",serialNum INT,deviceIp VARCHAR(20),model VARCHAR(30),softVersion VARCHAR(50),vendor VARCHAR(50),moduleCount INT);");
	    //创建rtd_module_device表结构
	    stmt.executeUpdate("CREATE TABLE rtd_module_device(id INT,systemId INT,status INT,socketChannelId INT,workStatus INT" 
	    				+",moduleNum INT,moduleType VARCHAR(30),netType VARCHAR(30),moduleImsi VARCHAR(50),businessType VARCHAR(10));");
	    //创建sid_device表结构
	    stmt.executeUpdate("CREATE TABLE sid_device(id INT,systemId INT,status INT,socketChannelId INT,linkTime TIMESTAMP,heartBeatTime TIMESTAMP,workStatus INT" 
	    				+",serialNum INT,deviceIp VARCHAR(20),model VARCHAR(30),softVersion VARCHAR(50),vendor VARCHAR(50),slotCount INT);");
	    //创建sid_slot_device表结构
	    stmt.executeUpdate("CREATE TABLE sid_slot_device(id INT,systemId INT,status INT,socketChannelId INT,workStatus INT" 
	    				+",slotNum INT,slotImsi VARCHAR(50));");
	   //创建resource表结构
	    stmt.executeUpdate("CREATE TABLE resource(rtdSerialNum INT,rtdModuleNum INT,sidSerialNum INT,sidSlotNum INT,imsi VARCHAR(50)" 
	    				+",testNum VARCHAR(20),netType VARCHAR(30),workStatus INT,bindStatus INT,systemId INT,cardType INT);");
	    conn.close();
		//创建H2服务端口
		Server.createTcpServer(new String[]{"-tcpPort",Constants.H2_TCP_PORT}).start();
		Server.createWebServer(new String[]{"-webPort",Constants.H2_WEB_PORT}).start();
		clientInfoDao = new ClientInfoDao("sqlMapConfig.xml");
		clientEventInfoDao = new ClientEventInfoDao("sqlMapConfig.xml");
		InitialTable initTable = new InitialTable("sqlMapConfig.xml");
		initTable.createClientTable();
		initTable.createClientEventTable();
	}
	
	public static void close(){
	    if(pool != null){
	        pool.dispose();
	    }
	}
	
	/**
	 * 功能描述：获取H2数据库连接
	 * 
	 * @return 
	 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
	 * @version 1.0.0
	 * @since 1.0.0
	 * create on: 2013-9-25
	 */
	public static Connection getConn(){
	    if(pool != null){
	        try {
                return pool.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
	    }
	    return null;
	}
	
	public static class InitialTable extends IbatisDao{
		
		public InitialTable(String configFilePath) throws IOException {
            super(configFilePath);
        }

        /**
		 * 创建客户端表
		 */
		public void createClientTable() {
			try {
				sqlmap.update("createClientTable");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 创建客户端事件表
		 */
		public void createClientEventTable() {
			try {
				sqlmap.update("createClientEventTable");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static ClientEventInfoDao getClientEventInfoDao(){
	    return clientEventInfoDao;
	}
	
	public static ClientInfoDao getClientInfoDao(){
	    return clientInfoDao;
	}
}
