package com.etone.ark.kernel.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.etone.ark.communication.ResultCode;
import com.etone.ark.communication.socket.SocketChannel;
import com.etone.ark.kernel.Constants;
import com.etone.ark.kernel.exception.ResourceException;
import com.etone.ark.kernel.service.model.Resource;
import com.etone.ark.kernel.service.model.RtdDevice;
import com.etone.ark.kernel.service.model.RtdModuleDevice;
import com.etone.ark.kernel.service.model.SidDevice;
import com.etone.ark.kernel.service.model.SidSlotDevice;
import com.etone.ark.kernel.service.model.TemporaryDevice;

/**
 * 功能描述：设备管理，用于保存，修改，移除设备
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 * 
 * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
 * @version 1.4.0
 * @since 1.0.0 
 * create on: 2013-9-25 
 */
public class DeviceManager {
    
    //缓存SocketChannel对象
    private final static ConcurrentMap<Integer, SocketChannel> SOCKET_CHANNEL_STORE = new ConcurrentHashMap<Integer, SocketChannel>();
    
    /**
     * 功能描述：新增或修改临时设备
     * 
     * @param device
     * @return 
     *      0:失败
     *      1:成功
     *      2:参数为空 错误
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-9-25
     */
    public static int saveOrUpdateTemporayDevice(TemporaryDevice device){
        //检查数据是否为空
        if(device == null || device.getSystemId() == null || device.getId() == null 
                || device.getSocketChannelId() == null){
            return 2;
        }
        Connection conn = null;
        try{
            conn = H2DataBaseServer.getConn();
            Statement stmt = conn.createStatement();
            TemporaryDevice temp = findTemporayDevice(device.getId(),device.getSystemId(),device.getIndex());
            if(temp == null){
                stmt.executeUpdate("INSERT INTO temporay_device VALUES("+device.getId()+","+device.getSystemId()+","+device.getIndex()
                        +","+device.getStatus()+","+device.getSocketChannelId()+");");
            }else{
                String sql = "UPDATE temporay_device SET socketChannelId=" + device.getSocketChannelId()+" WHERE"
                    +" systemId="+device.getSystemId()+" AND id="+device.getId();
                if(device.getIndex() != null){
                    sql = sql + " AND index=" + device.getIndex();
                }else{
                    sql = sql + " AND index is null";
                }
                
                stmt.executeUpdate(sql);
            }
            return 1;
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try {
                if(conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
    
    /**
     * 功能描述：根据系统ID，序列号，模块号查找临时设备
     * 
     * @param device
     * @return 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-9-25
     */
    public static TemporaryDevice findTemporayDevice(Integer id,Integer systemId,Integer index){
        //检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM temporay_device t1 where t1.id ="+id+" and t1.systemId=" + systemId;
                if(index != null){
                    sql = sql + " and t1.index ="+index;
                }else{
                    sql = sql + " and t1.index is null";
                }
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                if(rs.next()) {
                    TemporaryDevice device = new TemporaryDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    obj= rs.getObject("index");
                    if(obj != null){
                        device.setIndex(Integer.valueOf(obj.toString()));
                    }
                    obj= rs.getObject("status");
                    if(obj != null){
                        device.setStatus(Integer.valueOf(obj.toString()));
                    }
                    device.setSocketChannelId(rs.getInt("socketChannelId"));
                    return device;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 查找RTD设备
     * @param id
     * @param systemId
     * @return RTD设备对象，如果未查找到则返回NULL
     */
    public static RtdDevice findRtdDevice(Integer id,Integer systemId){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM rtd_device t1 where t1.id ="+id+" and t1.systemId=" + systemId;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                if(rs.next()) {
                	RtdDevice device = new RtdDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("serialNum");
                    if(obj != null){device.setSerialNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("deviceIp");
                    if(obj != null){device.setDeviceIp(obj.toString());}
                    obj= rs.getObject("model");
                    if(obj != null){device.setModel(obj.toString());}
                    obj= rs.getObject("softVersion");
                    if(obj != null){device.setSoftVersion(obj.toString());}
                    obj= rs.getObject("vendor");
                    if(obj != null){device.setVendor(obj.toString());}
                    device.setModuleCount(rs.getInt("moduleCount"));
                    device.setLinkTime(rs.getTimestamp("linkTime"));
                    device.setHeartBeatTime(rs.getTimestamp("heartBeatTime"));
                    return device;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
   
    /**
     * 查找SID设备
     * @param id
     * @param systemId
     * @return SID设备对象，如果未查找到则返回NULL
     */
    public static SidDevice findSidDevice(Integer id,Integer systemId){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM sid_device t1 where t1.id ="+id+" and t1.systemId=" + systemId;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                if(rs.next()) {
                	SidDevice device = new SidDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("serialNum");
                    if(obj != null){device.setSerialNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("deviceIp");
                    if(obj != null){device.setDeviceIp(obj.toString());}
                    obj= rs.getObject("model");
                    if(obj != null){device.setModel(obj.toString());}
                    obj= rs.getObject("softVersion");
                    if(obj != null){device.setSoftVersion(obj.toString());}
                    obj= rs.getObject("vendor");
                    if(obj != null){device.setVendor(obj.toString());}
                    device.setSlotCount(rs.getInt("slotCount"));
                    device.setLinkTime(rs.getTimestamp("linkTime"));
                    device.setHeartBeatTime(rs.getTimestamp("heartBeatTime"));
                    return device;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 查找RtdModule设备
     * @param id
     * @param systemId
     * @return RtdModule设备对象，如果未查找到则返回NULL
     */
    public static RtdModuleDevice findRtdModuleDevice(Integer id,Integer systemId,Integer index){
    	//检查数据
        if(id != null && systemId != null && index != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM rtd_module_device t1 where t1.id ="+id+" and t1.systemId=" + systemId +" and t1.moduleNum="+index;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                if(rs.next()) {
                	RtdModuleDevice device = new RtdModuleDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("moduleNum");
                    if(obj != null){device.setModuleNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("moduleType");
                    if(obj != null){device.setModuleType(obj.toString());}
                    device.setBusinessType(rs.getString("businessType"));
                    obj= rs.getObject("netType");
                    if(obj != null){device.setNetType(obj.toString());}
                    obj= rs.getObject("moduleImsi");
                    if(obj != null){device.setModuleImsi(obj.toString());}
                    return device;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 查询RTD设备下所有模块
     * @param id
     * @param systemId
     * @return
     */
    public static List<RtdModuleDevice> findRtdModuleDevice(Integer id,Integer systemId){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
            	List<RtdModuleDevice> result = new ArrayList<RtdModuleDevice>();
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM rtd_module_device t1 where t1.id ="+id+" and t1.systemId=" + systemId;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                while(rs.next()) {
                	RtdModuleDevice device = new RtdModuleDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("moduleNum");
                    if(obj != null){device.setModuleNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("moduleType");
                    if(obj != null){device.setModuleType(obj.toString());}
                    device.setBusinessType(rs.getString("businessType"));
                    obj= rs.getObject("netType");
                    if(obj != null){device.setNetType(obj.toString());}
                    obj= rs.getObject("moduleImsi");
                    if(obj != null){device.setModuleImsi(obj.toString());}
                    result.add(device);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据状态查询RTD模块
     * @param status
     * @return
     */
    public static List<RtdModuleDevice> findRtdModuleDeviceByStatus(Integer status){
    	//检查数据
        if(status != null){
            Connection conn = null;
            try{
            	List<RtdModuleDevice> result = new ArrayList<RtdModuleDevice>();
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM rtd_module_device t1 where t1.status ="+status;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                while(rs.next()) {
                	RtdModuleDevice device = new RtdModuleDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("moduleNum");
                    if(obj != null){device.setModuleNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("moduleType");
                    if(obj != null){device.setModuleType(obj.toString());}
                    device.setBusinessType(rs.getString("businessType"));
                    obj= rs.getObject("netType");
                    if(obj != null){device.setNetType(obj.toString());}
                    obj= rs.getObject("moduleImsi");
                    if(obj != null){device.setModuleImsi(obj.toString());}
                    result.add(device);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据状态查询SID设备
     * @param status
     * @return
     */
    public static List<SidSlotDevice> findSidSlotDeviceByStatus(Integer status){
    	//检查数据
        if(status != null){
            Connection conn = null;
            try{
            	List<SidSlotDevice> result = new ArrayList<SidSlotDevice>();
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM sid_slot_device t1 where t1.status ="+status;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                while(rs.next()) {
                	SidSlotDevice device = new SidSlotDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotNum");
                    if(obj != null){device.setSlotNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotImsi");
                    if(obj != null){device.setSlotImsi(obj.toString());}
                    result.add(device);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 查找所有带IMSI的SID卡槽设备
     * @param status
     * @return
     */
    public static List<SidSlotDevice> findSidSlotDeviceHaveImsi(){
        Connection conn = null;
        try{
        	List<SidSlotDevice> result = new ArrayList<SidSlotDevice>();
            conn = H2DataBaseServer.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM sid_slot_device t1 where t1.slotImsi is not null and t1.slotImsi != ''";
            ResultSet rs = stmt.executeQuery(sql);   
            Object obj = null;
            while(rs.next()) {
            	SidSlotDevice device = new SidSlotDevice();
                device.setId(rs.getInt("id"));
                device.setSystemId(rs.getInt("systemId"));
                device.setStatus(rs.getInt("status"));
                device.setWorkStatus(rs.getInt("workStatus"));
                obj= rs.getObject("socketChannelId");
                if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                obj= rs.getObject("slotNum");
                if(obj != null){device.setSlotNum(Integer.valueOf(obj.toString()));}
                obj= rs.getObject("slotImsi");
                if(obj != null){device.setSlotImsi(obj.toString());}
                result.add(device);
            }
            return result;
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try {
                if(conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * 查找所有带IMSI的RTD模块设备
     * @return
     */
    public static List<RtdModuleDevice> findRtdModuleDeviceHaveImsi(){
        Connection conn = null;
        try{
        	List<RtdModuleDevice> result = new ArrayList<RtdModuleDevice>();
            conn = H2DataBaseServer.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM rtd_module_device t1 where t1.moduleImsi is not null and t1.moduleImsi != ''";
            ResultSet rs = stmt.executeQuery(sql);   
            Object obj = null;
            while(rs.next()) {
            	RtdModuleDevice device = new RtdModuleDevice();
                device.setId(rs.getInt("id"));
                device.setSystemId(rs.getInt("systemId"));
                device.setStatus(rs.getInt("status"));
                device.setWorkStatus(rs.getInt("workStatus"));
                obj= rs.getObject("socketChannelId");
                if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                obj= rs.getObject("moduleNum");
                if(obj != null){device.setModuleNum(Integer.valueOf(obj.toString()));}
                obj= rs.getObject("moduleType");
                if(obj != null){device.setModuleType(obj.toString());}
                device.setBusinessType(rs.getString("businessType"));
                obj= rs.getObject("netType");
                if(obj != null){device.setNetType(obj.toString());}
                obj= rs.getObject("moduleImsi");
                if(obj != null){device.setModuleImsi(obj.toString());}
                result.add(device);
            }
            return result;
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try {
                if(conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * 查询SID设备的卡槽信息
     * @param id
     * @param systemId
     * @return
     */
    public static List<SidSlotDevice> findSidSlotDevice(Integer id,Integer systemId){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
            	List<SidSlotDevice> result = new ArrayList<SidSlotDevice>();
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM sid_slot_device t1 where t1.id ="+id+" and t1.systemId=" + systemId;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                while(rs.next()) {
                	SidSlotDevice device = new SidSlotDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotNum");
                    if(obj != null){device.setSlotNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotImsi");
                    if(obj != null){device.setSlotImsi(obj.toString());}
                    result.add(device);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据IMSI查询卡槽信息
     * @param imsi
     * @return
     */
    public static SidSlotDevice findSidSlotDevice(String imsi){
    	//检查数据
        if(StringUtils.isNotBlank(imsi)){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM sid_slot_device t1 where t1.slotImsi = '"+imsi+"'";
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                if(rs.next()) {
                	SidSlotDevice device = new SidSlotDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotNum");
                    if(obj != null){device.setSlotNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotImsi");
                    if(obj != null){device.setSlotImsi(obj.toString());}
                    return device;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 查找SidSlot设备
     * @param id
     * @param systemId
     * @return SidSlot设备对象，如果未查找到则返回NULL
     */
    public static SidSlotDevice findSidSlotDevice(Integer id,Integer systemId,Integer index){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM sid_slot_device t1 where t1.id ="+id+" and t1.systemId=" + systemId +" and t1.slotNum="+index;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                if(rs.next()) {
                	SidSlotDevice device = new SidSlotDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotNum");
                    if(obj != null){device.setSlotNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("slotImsi");
                    if(obj != null){device.setSlotImsi(obj.toString());}
                    return device;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 保存RTD设备
     * @param rtd
     * @return 0:保存失败
     * 		1:第一次保存成功
     * 		2:保存成功
     * 		3:参数为空
     * 		4:已存在该设备，且设备正常
     * @version 1.3.1(2013-10-10)
     */
    public static int saveRtdDevice(RtdDevice device){
    	 //检查数据是否为空
    	if(device == null || device.getSystemId() == null || device.getId() == null){
            return 3;
        }
    	RtdDevice rtd = findRtdDevice(device.getId(),device.getSystemId());
    	if(rtd != null && Constants.RTD_STATUS_NORMAL == rtd.getStatus()){
    		//如果IP一致则替换
    		if(rtd.getDeviceIp().equals(device.getDeviceIp())){
    			SocketChannel channel = getSocketChannel(rtd.getSocketChannelId());
    			if(channel != null){channel.close();}
    			deleteRtdDevice(device.getId(),device.getSystemId());
    		}else{
    			return 4;
    		}
    	}else if(rtd != null){
    		deleteRtdDevice(device.getId(),device.getSystemId());
    	}
		 Connection conn = null;
	     try{
	         conn = H2DataBaseServer.getConn();
	         PreparedStatement stmt = conn.prepareStatement("INSERT INTO rtd_device VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
	         stmt.setInt(1, device.getId());
	         stmt.setInt(2, device.getSystemId());
	         stmt.setInt(3, device.getStatus());
	         stmt.setInt(4, device.getSocketChannelId());
	         Timestamp linkTime = new Timestamp(device.getLinkTime().getTime());
	         stmt.setTimestamp(5, linkTime);
	         Timestamp heartBeatTime = new Timestamp(device.getHeartBeatTime().getTime());
	         stmt.setTimestamp(6, heartBeatTime);
	         stmt.setInt(7, device.getWorkStatus());
	         stmt.setInt(8, device.getSerialNum());
	         stmt.setString(9, device.getDeviceIp());
	         stmt.setString(10, device.getModel());
	         stmt.setString(11, device.getSoftVersion());
	         stmt.setString(12, device.getVendor());
	         stmt.setInt(13,device.getModuleCount());
	         stmt.executeUpdate();
	         /*Statement stmt = conn.createStatement();
	         stmt.executeUpdate("INSERT INTO rtd_device VALUES("+device.getId()+","+device.getSystemId()+","+device.getStatus()+","+device.getSocketChannelId()+",'"+DateFormatUtils.format(device.getLinkTime(),"yyyy-MM-dd HH:mm:ss")+"','"+DateFormatUtils.format(device.getHeartBeatTime(),"yyyy-MM-dd HH:mm:ss")
	        		 +"',"+device.getWorkStatus()+","+device.getSerialNum()+",'"+device.getDeviceIp()+"','"+device.getModel()+"','"+device.getSoftVersion()+"','"+device.getVendor()+"',"+device.getModuleCount()+");");*/
	         if(rtd != null){return 2;}else{return 1;}
	     }catch(SQLException e){
	         e.printStackTrace();
	     }finally{
	         try {
	             if(conn != null){
	                 conn.close();
	             }
	         } catch (SQLException e) {
	             e.printStackTrace();
	         }
	     }
	     return 0;
    }
    
    /**
     * 保存RTD模块设备
     * @param device
     * @return
     */
    public static int saveRtdModuleDevice(RtdModuleDevice device){
    	 //检查数据是否为空
    	if(device == null || device.getSystemId() == null || device.getId() == null){
            return 2;
        }
		Connection conn = null;
	    try{
	         conn = H2DataBaseServer.getConn();
	         PreparedStatement stmt = conn.prepareStatement("INSERT INTO rtd_module_device VALUES(?,?,?,?,?,?,?,?,?,?)");
	         stmt.setInt(1, device.getId());
	         stmt.setInt(2, device.getSystemId());
	         stmt.setInt(3, device.getStatus());
	         stmt.setInt(4, device.getSocketChannelId());
	         stmt.setInt(5, device.getWorkStatus());
	         stmt.setInt(6, device.getModuleNum());
	         stmt.setString(7, device.getModuleType());
	         stmt.setString(8, device.getNetType());
	         stmt.setString(9, device.getModuleImsi());
	         stmt.setString(10, device.getBusinessType());
	         if(stmt.executeUpdate()>0){
	        	 return 1;
	         }
	         /*stmt.executeUpdate("INSERT INTO rtd_module_device VALUES("+device.getId()+","+device.getSystemId()+","+device.getStatus()+","+device.getSocketChannelId()
	        		 +","+device.getWorkStatus()+","+device.getModuleNum()+",'"+device.getModuleType()+"','"+device.getNetType()+"','"+device.getModuleImsi()+"','"+device.getBusinessType()+"');");*/
	    }catch(SQLException e){
	         e.printStackTrace();
	    }finally{
	        try {
	            if(conn != null){
	                 conn.close();
	            }
	        } catch (SQLException e) {
	             e.printStackTrace();
	        }
	    }
	    return 0;
    }
    
    /**
     * 修改RTD设备
     * @param device
     * @return
     */
    public static boolean updateRtdDevice(RtdDevice device){
	   	 //检查数据是否为空
	   	if(device != null && device.getSystemId() != null || device.getId()!= null){
	   		Connection conn = null;
		     try{
		         conn = H2DataBaseServer.getConn();
		         PreparedStatement stmt = conn.prepareStatement("UPDATE rtd_device SET status=?,socketChannelId=?,linkTime=?,heartBeatTime=?,serialNum=?,deviceIp=?,model=?,softVersion=?,vendor=?,moduleCount=? where id=? and systemId=?");
		         stmt.setInt(1, device.getStatus());
		         if(device.getSocketChannelId() != null){
		        	 stmt.setInt(2, device.getSocketChannelId());
		         }else{
		        	 stmt.setNull(2, Types.INTEGER);
		         }
		         Timestamp linkTime = new Timestamp(device.getHeartBeatTime().getTime());
		         stmt.setTimestamp(3,linkTime);
		         Timestamp heartBeatTime = new Timestamp(device.getHeartBeatTime().getTime());
		         stmt.setTimestamp(4,heartBeatTime);
		         stmt.setInt(5, device.getSerialNum());
		         stmt.setString(6, device.getDeviceIp());
		         stmt.setString(7, device.getModel());
		         stmt.setString(8, device.getSoftVersion());
		         stmt.setString(9, device.getVendor());
		         stmt.setInt(10, device.getModuleCount());
		         //条件
		         stmt.setInt(11, device.getId());
		         stmt.setInt(12, device.getSystemId());
		         /*stmt.executeUpdate("UPDATE rtd_device SET status = "+device.getStatus()+",socketChannelId="+device.getSocketChannelId()+",linkTime='"+DateFormatUtils.format(device.getLinkTime(),"yyyy-MM-dd HH:mm:ss")+"',heartBeatTime='"+DateFormatUtils.format(device.getHeartBeatTime(),"yyyy-MM-dd HH:mm:ss")
		        		 +"',serialNum = "+device.getSerialNum()+",deviceIp = '"+device.getDeviceIp()+"',model='"+device.getModel()+"',softVersion='"+device.getSoftVersion()
		        		 +"',vendor='"+device.getVendor()+"',moduleCount ="+device.getModuleCount()+" where id="+device.getId()+" and systemId="+device.getSystemId());*/
		         stmt.executeUpdate();
		         return true;
		     }catch(SQLException e){
		         e.printStackTrace();
		     }finally{
		         try {
		             if(conn != null){
		                 conn.close();
		             }
		         } catch (SQLException e) {
		             e.printStackTrace();
		         }
		     }
	    }
	   	return false;
   }
    
    /**
     * 修改RTD模块设备
     * @param device
     * @return
     */
    public static boolean updateRtdModuleDevice(RtdModuleDevice device){
    	 //检查数据是否为空
	   	if(device != null && device.getSystemId() != null || device.getId()!= null){
	   		Connection conn = null;
		     try{
		         conn = H2DataBaseServer.getConn();
		         PreparedStatement stmt = conn.prepareStatement("UPDATE rtd_module_device SET status=?,socketChannelId=?,moduleType=?,netType=?,moduleImsi=? where id=? and systemId=? and moduleNum=?");
		         stmt.setInt(1, device.getStatus());
		         if(device.getSocketChannelId() != null){
		        	 stmt.setInt(2, device.getSocketChannelId());
		         }else{
		        	 stmt.setNull(2, Types.INTEGER);
		         }
		         stmt.setString(3, device.getModuleType());
		         stmt.setString(4, device.getNetType());
		         stmt.setString(5, device.getModuleImsi());
		         //条件
		         stmt.setInt(6, device.getId());
		         stmt.setInt(7, device.getSystemId());
		         stmt.setInt(8, device.getModuleNum());
		         stmt.executeUpdate();
		        /* Statement stmt = conn.createStatement();
		         stmt.executeUpdate("UPDATE rtd_module_device SET status = "+device.getStatus()+",socketChannelId="+device.getSocketChannelId()
		        		 +",moduleType = '"+device.getModuleType()+"',netType='"+device.getNetType()+"',moduleImsi='"+device.getModuleImsi()+"',businessType='"+device.getBusinessType()
		        		 +"' where id="+device.getId()+" and systemId="+device.getSystemId()+" and moduleNum="+device.getModuleNum());*/
		         return true;
		     }catch(SQLException e){
		         e.printStackTrace();
		     }finally{
		         try {
		             if(conn != null){
		                 conn.close();
		             }
		         } catch (SQLException e) {
		             e.printStackTrace();
		         }
		     }
	    }
    	return false;
    }
    
    /**
     * 修改SID设备
     * @param device
     * @return
     */
    public static boolean updateSidDevice(SidDevice device){
	   	 //检查数据是否为空
	   	if(device != null && device.getSystemId() != null || device.getId()!= null){
	   		Connection conn = null;
		     try{
		         conn = H2DataBaseServer.getConn();
		         PreparedStatement stmt = conn.prepareStatement("UPDATE sid_device SET status=?,socketchannelId=?,linkTime=?,heartBeatTime=?,serialNum=?,deviceIp=?,model=?,softVersion=?,vendor=?,slotCount=? where id=? and systemId=?");
		         stmt.setInt(1, device.getStatus());
		         if(device.getSocketChannelId() != null){
		        	 stmt.setInt(2, device.getSocketChannelId());
		         }else{
		        	 stmt.setNull(2, Types.INTEGER);
		         }
		         Timestamp linkTime = new Timestamp(device.getHeartBeatTime().getTime());
		         stmt.setTimestamp(3,linkTime);
		         Timestamp heartBeatTime = new Timestamp(device.getHeartBeatTime().getTime());
		         stmt.setTimestamp(4,heartBeatTime);
		         stmt.setInt(5, device.getSerialNum());
		         stmt.setString(6, device.getDeviceIp());
		         stmt.setString(7, device.getModel());
		         stmt.setString(8, device.getSoftVersion());
		         stmt.setString(9, device.getVendor());
		         stmt.setInt(10, device.getSlotCount());
		         stmt.setInt(11, device.getId());
		         stmt.setInt(12, device.getSystemId());
		         stmt.executeUpdate();
		        /* Statement stmt = conn.createStatement();
		         stmt.executeUpdate("UPDATE sid_device SET status = "+device.getStatus()+",socketChannelId="+device.getSocketChannelId()+",linkTime='"+DateFormatUtils.format(device.getLinkTime(),"yyyy-MM-dd HH:mm:ss")+"',heartBeatTime='"+DateFormatUtils.format(device.getHeartBeatTime(),"yyyy-MM-dd HH:mm:ss")
		        		 +"',serialNum = "+device.getSerialNum()+",deviceIp = '"+device.getDeviceIp()+"',model='"+device.getModel()+"',softVersion='"+device.getSoftVersion()
		        		 +"',vendor='"+device.getVendor()+"',slotCount ="+device.getSlotCount()+" where id="+device.getId()+" and systemId="+device.getSystemId());*/
		         return true;
		     }catch(SQLException e){
		         e.printStackTrace();
		     }finally{
		         try {
		             if(conn != null){
		                 conn.close();
		             }
		         } catch (SQLException e) {
		             e.printStackTrace();
		         }
		     }
	    }
	   	return false;
   }
    
    /**
     * 修改SID卡槽设备
     * @param device
     * @return
     */
    public static boolean updateSidSlotDevice(SidSlotDevice device){
    	 //检查数据是否为空
	   	if(device != null && device.getSystemId() != null || device.getId()!= null){
	   		Connection conn = null;
		     try{
		         conn = H2DataBaseServer.getConn();
		         PreparedStatement stmt = conn.prepareStatement("UPDATE sid_slot_device SET status=?,socketChannelId=?,slotImsi=? where id=? and systemId=? and slotNum=?");
		         stmt.setInt(1, device.getStatus());
		         if(device.getSocketChannelId() != null){
		        	 stmt.setInt(2, device.getSocketChannelId());
		         }else{
		        	 stmt.setNull(2, Types.INTEGER);
		         }
		         stmt.setString(3, device.getSlotImsi());
		         stmt.setInt(4, device.getId());
		         stmt.setInt(5, device.getSystemId());
		         stmt.setInt(6, device.getSlotNum());
		         stmt.executeUpdate();
		         /*Statement stmt = conn.createStatement();
		         stmt.executeUpdate("UPDATE sid_slot_device SET status = "+device.getStatus()+",socketChannelId="+device.getSocketChannelId()
		        		 +",slotImsi = '"+device.getSlotImsi()+"' where id="+device.getId()+" and systemId="+device.getSystemId()+" and slotNum="+device.getSlotNum());*/
		         return true;
		     }catch(SQLException e){
		         e.printStackTrace();
		     }finally{
		         try {
		             if(conn != null){
		                 conn.close();
		             }
		         } catch (SQLException e) {
		             e.printStackTrace();
		         }
		     }
	    }
    	return false;
    }
    
    /**
     * 删除RTD设备，同时删除RTD下的模块
     * @param id
     * @param systemId
     */
    public static void deleteRtdDevice(Integer id,Integer systemId){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                //删除模块
                stmt.executeUpdate("DELETE FROM rtd_module_device t1 where t1.id = " + id +" and t1.systemId="+systemId);
                //删除RTD
                stmt.executeUpdate("DELETE FROM rtd_device t1 where t1.id ="+id+" and t1.systemId=" + systemId);
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 保存SID设备
     * @param rtd
     * @return 0:保存失败
     * 		1:保存成功
     * 		2:参数为空
     * 		3:已存在该设备，且设备正常
     * @version 1.3.1(2013-10-10)
     */
    public static int saveSidDevice(SidDevice device){
    	 //检查数据是否为空
    	if(device == null || device.getSystemId() == null || device.getId() == null){
            return 2;
        }
    	SidDevice sid = findSidDevice(device.getId(),device.getSystemId());
    	if(sid != null && Constants.RTD_STATUS_NORMAL == sid.getStatus()){
    		return 3;
    	}else if(sid != null){
    		//移除设备再添加
    		deleteSidDevice(device.getId(),device.getSystemId());
    	}
		 Connection conn = null;
	     try{
	         conn = H2DataBaseServer.getConn();
	         Statement stmt = conn.createStatement();
	         stmt.executeUpdate("INSERT INTO sid_device VALUES("+device.getId()+","+device.getSystemId()+","+device.getStatus()+","+device.getSocketChannelId()+",'"+DateFormatUtils.format(device.getLinkTime(),"yyyy-MM-dd HH:mm:ss")+"','"+DateFormatUtils.format(device.getHeartBeatTime(),"yyyy-MM-dd HH:mm:ss")
	        		 +"',"+device.getWorkStatus()+","+device.getSerialNum()+",'"+device.getDeviceIp()+"','"+device.getModel()+"','"+device.getSoftVersion()+"','"+device.getVendor()+"',"+device.getSlotCount()+");");
	         return 1;
	     }catch(SQLException e){
	         e.printStackTrace();
	     }finally{
	         try {
	             if(conn != null){
	                 conn.close();
	             }
	         } catch (SQLException e) {
	             e.printStackTrace();
	         }
	     }
	     return 0;
    }
    
    /**
     * 保存SID卡槽设备
     * @param device
     * @return
     */
    public static int saveSidSlotDevice(SidSlotDevice device){
    	 //检查数据是否为空
    	if(device == null || device.getSystemId() == null || device.getId() == null){
            return 2;
        }
		Connection conn = null;
	    try{
	         conn = H2DataBaseServer.getConn();
	         Statement stmt = conn.createStatement();
	         stmt.executeUpdate("INSERT INTO sid_slot_device VALUES("+device.getId()+","+device.getSystemId()+","+device.getStatus()+","+device.getSocketChannelId()
	        		 +","+device.getWorkStatus()+","+device.getSlotNum()+",'"+device.getSlotImsi()+"');");
	         return 1;
	    }catch(SQLException e){
	         e.printStackTrace();
	    }finally{
	        try {
	            if(conn != null){
	                 conn.close();
	            }
	        } catch (SQLException e) {
	             e.printStackTrace();
	        }
	    }
	    return 0;
    }
    
    /**
     * 删除SID设备，同时删除SID下的卡槽
     * @param id
     * @param systemId
     */
    public static void deleteSidDevice(Integer id,Integer systemId){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                //删除模块
                stmt.executeUpdate("DELETE FROM sid_slot_device t1 where t1.id = " + id +" and t1.systemId="+systemId);
                //删除RTD
                stmt.executeUpdate("DELETE FROM sid_device t1 where t1.id ="+id+" and t1.systemId=" + systemId);
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 查找并占有资源对象
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @param systemId
     * @param imsi
     * @param netType
     * @param testNum
     * @return
     * @throws ResourceException
     */
    public static Resource findAndEmployResource(Integer rtdSerialNum,Integer rtdModuleNum,Integer systemId,String imsi,String netType,String testNum) throws ResourceException{
    	Resource res = findResource(rtdSerialNum,rtdModuleNum,imsi);
    	if(res != null){
    		if(employResource(res)){
    			if(StringUtils.isNotBlank(netType) && !netType.equals(res.getNetType())){
    				res.setNetType(netType);
    				updateResourceNetType(rtdSerialNum,rtdModuleNum,imsi,netType); //修改网络制式
    				//发送中断
    				CommandServer.requestResourceRelease(res);
    			}
    			return findResource(rtdSerialNum,rtdModuleNum,imsi);
    		}else{
    			throw new ResourceException(ResultCode.ER_KERNEL_RESOURCE_BUSY,"资源繁忙");
    		}
    	}else{
    		//查询RTD模块
    		RtdModuleDevice rtdModule = findRtdModuleDevice(rtdSerialNum,systemId,rtdModuleNum);
    		if(rtdModule != null){
    			if(imsi.equals(rtdModule.getModuleImsi())){
    				//本地卡
    				Resource temp = findResourceByRtdModule(rtdSerialNum,rtdModuleNum);
    				if(temp != null){
    					//发送中断
        				CommandServer.requestResourceRelease(temp);
    				}
    				if(employRtdModule(rtdSerialNum,systemId,rtdModuleNum)){
    					res = new Resource();
    					res.setBindStatus(Resource.UNBIND);
    					res.setImsi(imsi);
    					res.setNetType(netType);
    					res.setRtdModuleNum(rtdModuleNum);
    					res.setRtdSerialNum(rtdSerialNum);
    					res.setSystemId(systemId);
    					res.setTestNum(testNum);
    					res.setWorkStatus(1);
    					res.setCardType(Constants.CARD_LOCAL);
    					saveResource(res);
    					return res;
    				}else{
    					 throw new ResourceException(ResultCode.ER_KERNEL_RESOURCE_BUSY,"序列号:" + rtdSerialNum +",模块号:"+rtdModuleNum+"的RTD模块资源繁忙");
    				}
    			}else{
    				//远程卡
    				SidSlotDevice sidSlot = findSidSlotDevice(imsi);
    				if(sidSlot != null){
    					Resource temp = findResourceByRtdModule(rtdSerialNum,rtdModuleNum);
    					if(temp != null){
        					//发送中断
            				CommandServer.requestResourceRelease(temp);
        				}
    					if(employRtdModule(rtdSerialNum,systemId,rtdModuleNum)){
    						temp = findResourceBySidSlot(sidSlot.getId(),sidSlot.getSlotNum());
    						if(temp != null){
            					//发送中断
                				CommandServer.requestResourceRelease(temp);
            				}
    						if(employSidSlot(sidSlot.getId(),sidSlot.getSystemId(),sidSlot.getSlotNum())){
    							res = new Resource();
    	    					res.setBindStatus(Resource.UNBIND);
    	    					res.setImsi(imsi);
    	    					res.setNetType(netType);
    	    					res.setRtdModuleNum(rtdModuleNum);
    	    					res.setRtdSerialNum(rtdSerialNum);
    	    					res.setSidSerialNum(sidSlot.getId());
    	    					res.setSidSlotNum(sidSlot.getSlotNum());
    	    					res.setSystemId(systemId);
    	    					res.setTestNum(testNum);
    	    					res.setWorkStatus(1);
    	    					res.setCardType(Constants.CARD_REMOTE);
    	    					saveResource(res);
    	    					return res;
    						}else{
    							unemployRtdModule(rtdSerialNum,systemId,rtdModuleNum);
    							throw new ResourceException(ResultCode.ER_KERNEL_RESOURCE_BUSY,"IMSI:"+imsi+"的SID卡槽资源繁忙");
    						}
    					}else{
    						throw new ResourceException(ResultCode.ER_KERNEL_RESOURCE_BUSY,"序列号:" + rtdSerialNum +",模块号:"+rtdModuleNum+"的RTD模块资源繁忙");
    					}
    				}else{
    					throw new ResourceException(ResultCode.ER_KERNEL_RESOURCE_BUSY,"没有IMSI:"+imsi+"的设备");
    				}
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * 占用资源对象
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @param imsi
     * @return
     */
    public static boolean employResource(Resource res){
    	//检查数据
        if(res != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE resource SET workStatus = 1 where bindStatus != " + Resource.INTERRUPT + " and workStatus = 0 and rtdSerialNum ="+res.getRtdSerialNum()+" and rtdModuleNum=" + res.getRtdModuleNum() +" and imsi = '"+res.getImsi()+"'";
                int i = stmt.executeUpdate(sql);  
                if(i > 0){
                	if(employRtdModule(res.getRtdSerialNum(),res.getSystemId(),res.getRtdModuleNum())){
	                	if(res.getSidSerialNum() != null && res.getSidSlotNum() != null){
	                		if(employSidSlot(res.getSidSerialNum(),res.getSystemId(),res.getSidSlotNum())){
	                			res.setWorkStatus(1);
	                			return true;
	                		}
	                	}else{
	                		res.setWorkStatus(1);
	                		return true;
	                	}
	                	unemployRtdModule(res.getRtdSerialNum(),res.getSystemId(),res.getRtdModuleNum());
                	}
                	sql = "UPDATE resource SET workStatus = 0 where rtdSerialNum ="+res.getRtdSerialNum()+" and rtdModuleNum=" + res.getRtdModuleNum() +" and imsi = '"+res.getImsi()+"'";
                	stmt.executeUpdate(sql);
                	res.setWorkStatus(0);
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    	return false;
    }
    
    /**
     * 释放占用的资源
     * @param res
     */
    public static void unemployResource(Resource res){
    	//检查数据
        if(res != null){
            Connection conn = null;
            try{
            	unemployRtdModule(res.getRtdSerialNum(),res.getSystemId(),res.getRtdModuleNum());
            	unemploySidSlot(res.getSidSerialNum(),res.getSystemId(),res.getSidSlotNum());
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE resource SET workStatus = 0 where rtdSerialNum ="+res.getRtdSerialNum()+" and rtdModuleNum=" + res.getRtdModuleNum() +" and imsi = '"+res.getImsi()+"'";
                stmt.executeUpdate(sql);
                res.setWorkStatus(0);
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 是否可以重置资源
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @param imsi
     * @return
     */
    public static boolean isCanReleaseResource(Integer rtdSerialNum,Integer rtdModuleNum,String imsi){
    	//检查数据
        if(rtdSerialNum != null && rtdModuleNum != null && StringUtils.isNotBlank(imsi)){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE resource SET bindStatus = "+Resource.INTERRUPT+" where bindStatus = " + Resource.BINDING + " and rtdSerialNum ="+rtdSerialNum+" and rtdModuleNum=" + rtdModuleNum +" and imsi = '"+imsi+"'";
                int i = stmt.executeUpdate(sql);  
                if(i > 0){return true;}
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    	return false;
    }
    
    /**
     * 占用RTD模块设备
     * @param id
     * @param systemId
     * @param moduleNum
     * @return
     */
    public static boolean employRtdModule(Integer id,Integer systemId,Integer moduleNum){
    	//检查数据
        if(id != null && systemId != null && moduleNum != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE rtd_module_device SET workStatus=1 where workStatus=0 and id="+id+" and systemId=" + systemId +" and moduleNum="+moduleNum;
                int i = stmt.executeUpdate(sql);  
                if(i > 0){return true;}
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    	return false;
    }
    
    /**
     * 释放占有的RTD模块
     * @param id
     * @param systemId
     * @param moduleNum
     */
    public static void unemployRtdModule(Integer id,Integer systemId,Integer moduleNum){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE rtd_module_device SET workStatus = 0 where id="+id+" and systemId=" + systemId +" and moduleNum="+moduleNum;
                stmt.executeUpdate(sql);  
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 占用SID卡槽设备
     * @param id
     * @param systemId
     * @param slotNum
     * @return
     */
    public static boolean employSidSlot(Integer id,Integer systemId,Integer slotNum){
    	//检查数据
        if(id != null && systemId != null && slotNum != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE sid_slot_device SET workStatus=1 where workStatus=0 and id="+id+" and systemId=" + systemId +" and slotNum="+slotNum;
                int i = stmt.executeUpdate(sql);  
                if(i > 0){return true;}
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    	return false;
    }
    
    /**
     * 释放占有的SID卡槽
     * @param id
     * @param systemId
     * @param slotNum
     */
    public static void unemploySidSlot(Integer id,Integer systemId,Integer slotNum){
    	//检查数据
        if(id != null && systemId != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE sid_slot_device SET workStatus=0 where id="+id+" and systemId=" + systemId +" and slotNum="+slotNum;
                stmt.executeUpdate(sql);  
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
     
    /**
     * 查找资源对象
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @param imsi
     * @return
     */
    public static Resource findResource(Integer rtdSerialNum,Integer rtdModuleNum,String imsi){
    	//检查数据
        if(rtdSerialNum != null && rtdModuleNum != null && StringUtils.isNotBlank(imsi)){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM resource t1 where t1.rtdSerialNum ="+rtdSerialNum+" and t1.rtdModuleNum=" + rtdModuleNum +" and t1.imsi = '"+imsi+"'";
                ResultSet rs = stmt.executeQuery(sql); 
                if(rs.next()) {
                	Resource res = new Resource();
                	res.setRtdSerialNum(rs.getInt("rtdSerialNum"));
                	res.setBindStatus(rs.getInt("bindStatus"));
                	res.setImsi(rs.getNString("imsi"));
                	res.setNetType(rs.getNString("netType"));
                	res.setRtdModuleNum(rs.getInt("rtdModuleNum"));
                	Object obj = rs.getObject("sidSerialNum");
                	if(obj != null){res.setSidSerialNum(Integer.valueOf(obj.toString()));}
                	obj = rs.getObject("sidSlotNum");
                	if(obj != null){res.setSidSlotNum(Integer.valueOf(obj.toString()));}
                	res.setWorkStatus(rs.getInt("workStatus"));
                	res.setTestNum(rs.getNString("testNum"));
                	res.setSystemId(rs.getInt("systemId"));
                	res.setCardType(rs.getInt("cardType"));
                    return res;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据工作状态查找资源对象
     * @param workStatus
     * @return
     */
    public static List<Resource> findResourceByWorkStatus(Integer workStatus){
    	//检查数据
        if(workStatus != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM resource t1 where t1.bindStatus = "+Resource.BINDING+" and t1.workStatus ="+workStatus;
                ResultSet rs = stmt.executeQuery(sql);
                List<Resource> result = new ArrayList<Resource>();
                if(rs.next()) {
                	Resource res = new Resource();
                	res.setRtdSerialNum(rs.getInt("rtdSerialNum"));
                	res.setBindStatus(rs.getInt("bindStatus"));
                	res.setImsi(rs.getNString("imsi"));
                	res.setNetType(rs.getNString("netType"));
                	res.setRtdModuleNum(rs.getInt("rtdModuleNum"));
                	Object obj = rs.getObject("sidSerialNum");
                	if(obj != null){res.setSidSerialNum(Integer.valueOf(obj.toString()));}
                	obj = rs.getObject("sidSlotNum");
                	if(obj != null){res.setSidSlotNum(Integer.valueOf(obj.toString()));}
                	res.setWorkStatus(rs.getInt("workStatus"));
                	res.setTestNum(rs.getNString("testNum"));
                	res.setSystemId(rs.getInt("systemId"));
                	res.setCardType(rs.getInt("cardType"));
                	result.add(res);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据RTD模块信息查询资源对象
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @return
     */
    public static Resource findResourceByRtdModule(Integer rtdSerialNum,Integer rtdModuleNum){
    	//检查数据
        if(rtdSerialNum != null && rtdModuleNum != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM resource t1 where t1.rtdSerialNum ="+rtdSerialNum+" and t1.rtdModuleNum=" + rtdModuleNum;
                ResultSet rs = stmt.executeQuery(sql);   
                if(rs.next()) {
                	Resource res = new Resource();
                	res.setRtdSerialNum(rs.getInt("rtdSerialNum"));
                	res.setBindStatus(rs.getInt("bindStatus"));
                	res.setImsi(rs.getNString("imsi"));
                	res.setNetType(rs.getNString("netType"));
                	res.setRtdModuleNum(rs.getInt("rtdModuleNum"));
                	Object obj = rs.getObject("sidSerialNum");
                	if(obj != null){res.setSidSerialNum(Integer.valueOf(obj.toString()));}
                	obj = rs.getObject("sidSlotNum");
                	if(obj != null){res.setSidSlotNum(Integer.valueOf(obj.toString()));}
                	res.setWorkStatus(rs.getInt("workStatus"));
                	res.setTestNum(rs.getNString("testNum"));
                	res.setSystemId(rs.getInt("systemId"));
                	res.setCardType(rs.getInt("cardType"));
                    return res;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
  
    /**
     * 根据SID卡槽信息查询资源对象
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @return
     */
    public static Resource findResourceBySidSlot(Integer sidSerialNum,Integer sidSlotNum){
    	//检查数据
        if(sidSerialNum != null && sidSlotNum != null){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM resource t1 where t1.sidSerialNum ="+sidSerialNum+" and t1.sidSlotNum=" + sidSlotNum;
                ResultSet rs = stmt.executeQuery(sql);   
                if(rs.next()) {
                	Resource res = new Resource();
                	res.setRtdSerialNum(rs.getInt("rtdSerialNum"));
                	res.setBindStatus(rs.getInt("bindStatus"));
                	res.setImsi(rs.getNString("imsi"));
                	res.setNetType(rs.getNString("netType"));
                	res.setRtdModuleNum(rs.getInt("rtdModuleNum"));
                	Object obj = rs.getObject("sidSerialNum");
                	if(obj != null){res.setSidSerialNum(Integer.valueOf(obj.toString()));}
                	obj = rs.getObject("sidSlotNum");
                	if(obj != null){res.setSidSlotNum(Integer.valueOf(obj.toString()));}
                	res.setWorkStatus(rs.getInt("workStatus"));
                	res.setTestNum(rs.getNString("testNum"));
                	res.setSystemId(rs.getInt("systemId"));
                	res.setCardType(rs.getInt("cardType"));
                    return res;
                }
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 保存资源对象
     * @param resource
     */
    public static int saveResource(Resource resource){
    	 //检查数据是否为空
    	if(resource == null || resource.getRtdSerialNum() == null || resource.getRtdModuleNum() == null ||StringUtils.isBlank(resource.getImsi())){
            return 2;
        }
    	Resource res = findResource(resource.getRtdSerialNum(),resource.getRtdModuleNum(),resource.getImsi());
    	if(res != null){
    		return 3;
    	}
		 Connection conn = null;
	     try{
	         conn = H2DataBaseServer.getConn();
	         Statement stmt = conn.createStatement();
	         stmt.executeUpdate("INSERT INTO resource VALUES("+resource.getRtdSerialNum()+","+resource.getRtdModuleNum()+","+resource.getSidSerialNum()+","+resource.getSidSlotNum()
	        		 +",'"+resource.getImsi()+"','"+resource.getTestNum()+"','"+resource.getNetType()+"',"+resource.getWorkStatus()+","+resource.getBindStatus()
	        		 +","+resource.getSystemId()+","+resource.getCardType()+");");
	         return 1;
	     }catch(SQLException e){
	         e.printStackTrace();
	     }finally{
	         try {
	             if(conn != null){
	                 conn.close();
	             }
	         } catch (SQLException e) {
	             e.printStackTrace();
	         }
	     }
	     return 0;
    }
    
    /**
     * 修改资源的网络制式
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @param imsi
     * @param bindStatus
     */
    public static void updateResourceNetType(Integer rtdSerialNum,Integer rtdModuleNum,String imsi,String netType){
    	//检查数据
        if(rtdSerialNum != null && rtdModuleNum != null && StringUtils.isNotBlank(imsi)){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE resource SET netType = '"+netType+"' where rtdSerialNum ="+rtdSerialNum+" and rtdModuleNum=" + rtdModuleNum +" and imsi = '"+imsi+"'";
                stmt.executeUpdate(sql);  
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 修改资源绑定关系
     * @param rtdSerialNum
     * @param rtdModuleNum
     * @param imsi
     * @param bindStatus
     */
    public static void updateResourceBindStatus(Integer rtdSerialNum,Integer rtdModuleNum,String imsi,Integer bindStatus){
    	//检查数据
        if(rtdSerialNum != null && rtdModuleNum != null && StringUtils.isNotBlank(imsi)){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "UPDATE resource SET bindStatus = "+bindStatus+" where rtdSerialNum ="+rtdSerialNum+" and rtdModuleNum=" + rtdModuleNum +" and imsi = '"+imsi+"'";
                stmt.executeUpdate(sql);  
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void removeResource(Integer rtdSerialNum,Integer rtdModuleNum,String imsi){
    	//检查数据
        if(rtdSerialNum != null && rtdModuleNum != null && StringUtils.isNotBlank(imsi)){
            Connection conn = null;
            try{
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "DELETE FROM resource where workStatus = 0 and bindStatus = "+Resource.UNBIND+" and rtdSerialNum ="+rtdSerialNum+" and rtdModuleNum=" + rtdModuleNum +" and imsi = '"+imsi+"'";
                stmt.executeUpdate(sql);  
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 查询所有RTD设备
     * @return
     */
    public static List<RtdDevice> findRtdDevice(){
    	 Connection conn = null;
         try{
         	List<RtdDevice> result = new ArrayList<RtdDevice>();
             conn = H2DataBaseServer.getConn();
             Statement stmt = conn.createStatement();
             String sql = "SELECT * FROM rtd_device t1";
             ResultSet rs = stmt.executeQuery(sql);   
             Object obj = null;
             while(rs.next()) {
             	RtdDevice device = new RtdDevice();
                 device.setId(rs.getInt("id"));
                 device.setSystemId(rs.getInt("systemId"));
                 device.setStatus(rs.getInt("status"));
                 device.setWorkStatus(rs.getInt("workStatus"));
                 obj= rs.getObject("socketChannelId");
                 if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                 obj= rs.getObject("serialNum");
                 if(obj != null){device.setSerialNum(Integer.valueOf(obj.toString()));}
                 obj= rs.getObject("deviceIp");
                 if(obj != null){device.setDeviceIp(obj.toString());}
                 obj= rs.getObject("model");
                 if(obj != null){device.setModel(obj.toString());}
                 obj= rs.getObject("softVersion");
                 if(obj != null){device.setSoftVersion(obj.toString());}
                 obj= rs.getObject("vendor");
                 if(obj != null){device.setVendor(obj.toString());}
                 device.setModuleCount(rs.getInt("moduleCount"));
                 device.setLinkTime(rs.getTimestamp("linkTime"));
                 device.setHeartBeatTime(rs.getTimestamp("heartBeatTime"));
                 result.add(device);
             }
             return result;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try {
                 if(conn != null){
                     conn.close();
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return null;
    }
    
    /**
     * 查询所有SID设备
     * @return
     */
    public static List<SidDevice> findSidDevice(){
    	//检查数据
        Connection conn = null;
        try{
        	List<SidDevice> result = new ArrayList<SidDevice>();
            conn = H2DataBaseServer.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM sid_device t1";
            ResultSet rs = stmt.executeQuery(sql);   
            Object obj = null;
            while(rs.next()) {
            	SidDevice device = new SidDevice();
                device.setId(rs.getInt("id"));
                device.setSystemId(rs.getInt("systemId"));
                device.setStatus(rs.getInt("status"));
                device.setWorkStatus(rs.getInt("workStatus"));
                obj= rs.getObject("socketChannelId");
                if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                obj= rs.getObject("serialNum");
                if(obj != null){device.setSerialNum(Integer.valueOf(obj.toString()));}
                obj= rs.getObject("deviceIp");
                if(obj != null){device.setDeviceIp(obj.toString());}
                obj= rs.getObject("model");
                if(obj != null){device.setModel(obj.toString());}
                obj= rs.getObject("softVersion");
                if(obj != null){device.setSoftVersion(obj.toString());}
                obj= rs.getObject("vendor");
                if(obj != null){device.setVendor(obj.toString());}
                device.setSlotCount(rs.getInt("slotCount"));
                device.setLinkTime(rs.getTimestamp("linkTime"));
                device.setHeartBeatTime(rs.getTimestamp("heartBeatTime"));
                result.add(device);
            }
            return result;
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try {
                if(conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * 根据通讯通道ID查询RTD设备
     * @param socketChannelId
     * @return
     */
    public static List<RtdDevice> findRtdDeviceBySocketChannelId(Integer socketChannelId){
    	//检查数据
        if(socketChannelId != null){
            Connection conn = null;
            try{
            	List<RtdDevice> result = new ArrayList<RtdDevice>();
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM rtd_device t1 where t1.socketChannelId ="+socketChannelId;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                while(rs.next()) {
                	RtdDevice device = new RtdDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("serialNum");
                    if(obj != null){device.setSerialNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("deviceIp");
                    if(obj != null){device.setDeviceIp(obj.toString());}
                    obj= rs.getObject("model");
                    if(obj != null){device.setModel(obj.toString());}
                    obj= rs.getObject("softVersion");
                    if(obj != null){device.setSoftVersion(obj.toString());}
                    obj= rs.getObject("vendor");
                    if(obj != null){device.setVendor(obj.toString());}
                    device.setModuleCount(rs.getInt("moduleCount"));
                    device.setLinkTime(rs.getTimestamp("linkTime"));
                    device.setHeartBeatTime(rs.getTimestamp("heartBeatTime"));
                    result.add(device);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据通讯通道ID查询SID设备
     * @param socketChannelId
     * @return
     */
    public static List<SidDevice> findSidDeviceBySocketChannelId(Integer socketChannelId){
    	//检查数据
        if(socketChannelId != null){
            Connection conn = null;
            try{
            	List<SidDevice> result = new ArrayList<SidDevice>();
                conn = H2DataBaseServer.getConn();
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM sid_device t1 where t1.socketChannelId ="+socketChannelId;
                ResultSet rs = stmt.executeQuery(sql);   
                Object obj = null;
                while(rs.next()) {
                	SidDevice device = new SidDevice();
                    device.setId(rs.getInt("id"));
                    device.setSystemId(rs.getInt("systemId"));
                    device.setStatus(rs.getInt("status"));
                    device.setWorkStatus(rs.getInt("workStatus"));
                    obj= rs.getObject("socketChannelId");
                    if(obj != null){device.setSocketChannelId(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("serialNum");
                    if(obj != null){device.setSerialNum(Integer.valueOf(obj.toString()));}
                    obj= rs.getObject("deviceIp");
                    if(obj != null){device.setDeviceIp(obj.toString());}
                    obj= rs.getObject("model");
                    if(obj != null){device.setModel(obj.toString());}
                    obj= rs.getObject("softVersion");
                    if(obj != null){device.setSoftVersion(obj.toString());}
                    obj= rs.getObject("vendor");
                    if(obj != null){device.setVendor(obj.toString());}
                    device.setSlotCount(rs.getInt("slotCount"));
                    device.setLinkTime(rs.getTimestamp("linkTime"));
                    device.setHeartBeatTime(rs.getTimestamp("heartBeatTime"));
                    result.add(device);
                }
                return result;
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                try {
                    if(conn != null){
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 功能描述：保存Socket通讯通道
     * 
     * @param channel
     * @return 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-9-25
     */
     public static boolean saveSocketChannel(SocketChannel channel){
        if(channel != null){
            SOCKET_CHANNEL_STORE.put(channel.getId(), channel);
            return true;
        }
        return false;
    }
    
    /**
     * 功能描述：移除Socket通讯通道
     * 
     * @param id 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-9-25
     */
    public static void removeSocketChannel(Integer id){
        if(id != null){
             SOCKET_CHANNEL_STORE.remove(id);
             Connection conn = null;
             try{
                 conn = H2DataBaseServer.getConn();
                 Statement stmt = conn.createStatement();
                 String sql = "DELETE FROM temporay_device WHERE socketChannelId=" + id;
                 stmt.executeUpdate(sql);
             }catch(SQLException e){
                 e.printStackTrace();
             }finally{
                 try {
                     if(conn != null){
                         conn.close();
                     }
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
        }
    }
   
    /**
     * 功能描述：根据ID获取Socket通讯通道
     * 
     * @param id
     * @return 
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0
     * create on: 2013-9-25
     */
    public static SocketChannel getSocketChannel(Integer id){
        if(id != null){
            return SOCKET_CHANNEL_STORE.get(id);
        }
        return null;
    }
    
    /**
     * 功能描述：获取设备序列号,公式:设备序列化*100+模块或卡槽号
     * 
     * @param serialNum
     *            RTD设备序列号或SID设备序列号
     * @param num
     *            模块号或卡槽号,如果没有则填0
     * @return
     * @author <a href="mailto:zhanghao@gzexport.com">张浩 </a>
     * @version 1.0.0
     * @since 1.0.0 create on: 2013-6-5
     */
    public static int getDeviceID(int serialNum, int num) {
        return serialNum * 100 + num;
    }

    public static String buildAddress(int systemId, int serialId, Integer index) {
        if (index != null) {
            return systemId + ":" + serialId + ":" + index;
        } else {
            return systemId + ":" + serialId + ":";
        }
    }
}
