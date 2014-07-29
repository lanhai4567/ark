package com.etone.cplusplus;


/**
 * 调用C类库，完成录音质量评分
 * @version 0.1
 * @author 张浩
 *
 */
public class Pesq {
	
	/**
	* Description:PESQ库初始化（只需初始化一次）
	*
	* @author: xb
	* @date：2012-11-5
	* @return
	*/
	public native static boolean init();

	/**
	* Description:获取录音文件时长,单位为秒
	*
	* @author: xb
	* @date：2012-11-5
	* @param infile
	* @return
	*/
	public native static int getLength(String infile); 

	/**
	* Description:把播音文件转为标准格式（转为和录音文件同一格式），并截断长度（截断为和录音文件同一长度）
	*
	* @author: xb
	* @date：2012-11-5
	* @param infile
	* @param outfile
	* @param length
	* @return
	*/
	public native static boolean changeFile(String infile, String outfile, int length);

	/**
	* Description:播音文件和录音文件作pesq评分
	*
	* @author: xb
	* @date：2012-11-5
	* @param playPath
	* @param recordPath
	* @return
	*/
	public native static double getPesq(String playPath, String recordPath);
}
