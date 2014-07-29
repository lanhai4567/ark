/* ExpressionSolver.java	@date 2012-11-13
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * <p>自定义表达式工具 </p>
 * 使用JavsScript脚本引擎
 * 
 * @version 1.1
 * @author 张浩
 * 
 */
public class ExpressionSolver {

	static Logger logger = Logger.getLogger(ExpressionSolver.class);

	/**
	 * 创建JavaScript脚本引擎
	 */
	public static ScriptEngine engine = new ScriptEngineManager()
			.getEngineByName("JavaScript");

	/**
	 * 加载脚本库 
	 * @return
	 */
	public static void initJavaScriptFile(String path){
		try {
			File file = new File(path);
			if(file.exists()){
				Reader reader  = new InputStreamReader(new FileInputStream(file),"UTF-8");
				engine.eval(reader,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			}
		} catch (UnsupportedEncodingException e) {
 			 logger.error("不支持的编码异常", e);
		} catch (FileNotFoundException e) {
			 logger.error("微内核自定义JS文件不存在", e);
		} catch (ScriptException e) {
			logger.error("加载JS脚本库错误",e);
		}
	}

	/**
	 * 是否是自定义表达式
	 * 
	 * @param expression
	 *            表达式
	 * @return 获取表达式内容获取空
	 */
	public static boolean isExpression(String expression) {
		Pattern pat = Pattern.compile("\\$\\{([^\\}]*)\\}");
		Matcher mat = pat.matcher(expression);
		return mat.find();
	}
	
	public static String getExpression(String expression) {
		Pattern pat = Pattern.compile("\\$\\{([^\\}]*)\\}");
		Matcher mat = pat.matcher(expression);
		if(mat.find()){
			return mat.group(1);
		}
		return null;
	}

	/**
	 * 脚本参数对象
	 * 
	 */
	public static class ScriptObject {

		private String name;

		private Bindings bingdings;

		public ScriptObject(String name) {
			this.name = name;
			bingdings = engine.createBindings();
		}

		public void put(String name, Object value) {
			bingdings.put(name, value);
		}

		/**
		 * <p>将一个脚本参数对象put到自身</p>
		 * 要加入的脚本参数对象会被当做一个脚本对象加入到自身中，其中脚本参数对象的name属性就是这个对象的名称
		 * 在使用时可以使用这个名称点出它所包含的属性
		 * 
		 * @param obj
		 *            要加入的脚本参数对象
		 */
		public void put(ScriptObject obj) {
			try {
				engine.eval("var " + obj.getName() + "={}", bingdings);
				for (String key : obj.getBingdings().keySet()) {
					try {
						engine.eval(obj.getName() + "." + key + "='"
								+ obj.getBingdings().get(key) + "'", bingdings);
					} catch (Exception e) {
						// 捕获不处理
					}
				}
			} catch (Exception e) {
				// 捕获不处理
			}

		}

		/**
		 * <p>将指定参数对象中的参数put到自身 </p>
		 * 
		 * @param obj 指定参数对象
		 */
		public void put(Bindings bingings) {
			for (String name : bingings.keySet()) {
				bingdings.put(name, bingings.get(name));
			}
		}

		public void put(Map<String, Object> data) {
		    if(data != null){
		        bingdings.putAll(data);
		    }
		}

		public Object get(String key) {
			return bingdings.get(key);
		}
		
		public String getString(String key){
			Object obj = bingdings.get(key);
			if(obj != null){
				return obj.toString();
			}
			return null;
		}
		
		public Bindings getBingdings() {
			return bingdings;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (String key : bingdings.keySet()) {
				sb.append("," + key + "=" + bingdings.get(key));
			}
			if (sb.length() > 0) {
				return "{" + sb.substring(1) + "}";
			}
			return "{}";
		}
	}
}
