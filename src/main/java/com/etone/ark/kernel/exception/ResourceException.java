/* ResourceBindException.java @date 2013-1-17
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.exception;

/**
 * 在测试项执行前进行资源调度时，无法获取资源的各种意外情况
 * @version 1.1
 * @author 张浩
 *
 */
@SuppressWarnings("serial")
public class ResourceException extends Exception {

	/**
	 * 资源绑定错误码
	 */
	private String errorCode;
	private String msg;
	
	public ResourceException(String errorCode,String msg){
		this.errorCode = errorCode;
		this.msg = msg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		return msg;
	}
	
	
}
