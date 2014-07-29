/* Variable.java @date 2012-12-7
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.task.model;

/**
 * 测试业务结果参数描述
 * @version 1.1
 * @author 匡海波
 *
 */
public class Variable {

	private String code;
	private String name;
	private String value;
	private String javaType;
	private String description;
 
	public Variable() {
		super();
	}

	public Variable(String code, String name, String value, String javaType,
			  String description) {
		super();
		this.code = code;
		this.name = name;
		this.value = value;
		this.javaType = javaType;
 		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
