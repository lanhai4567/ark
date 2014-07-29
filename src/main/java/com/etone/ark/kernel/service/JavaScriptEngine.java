package com.etone.ark.kernel.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptEngine {

	private static ScriptEngine engine;
	
	/**
	 * 运行JavaScript脚本
	 * @param jsPath JS脚本文件路径数组
	 */
	public static boolean run(String... jsPath){
		try{
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
			engine.eval("var class2type = {}",engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			engine.eval("var core_toString = class2type.toString;",engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			engine.eval("var core_hasOwn = class2type.hasOwnProperty;",engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			String fun = null;
			//获取数据类型
			fun = "function type( obj ) {"
					+"if ( obj == null ) {"
					+"return String( obj );"
					+"}"
					+"return typeof obj === 'object' || typeof obj === 'function' ?"
					+"class2type[ core_toString.call(obj) ] || 'object' :"
					+"typeof obj;"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			//判定是否是Window对象
			fun = "function isWindow( obj ) {"
					+"return obj != null && obj == obj.window;"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			//判定是否是{}或new Object对象
			fun = "function isPlainObject( obj ) {"
					+"var key;"
					+"if ( !obj || type(obj) !== 'object' || obj.nodeType || isWindow( obj ) ) {"
					+"	return false;"
					+"}"
					+"try {"
					+"	if ( obj.constructor &&"
					+"		!core_hasOwn.call(obj, 'constructor') &&"
					+"		!core_hasOwn.call(obj.constructor.prototype, 'isPrototypeOf') ) {"
					+"		return false;"
					+"	}"
					+"} catch ( e ) {"
					+"	return false;"
					+"}"
					+"for ( key in obj ) {}"
					+"return key === undefined || core_hasOwn.call( obj, key );"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			//判定是否是数组
			fun = "var isArray = Array.isArray || function( obj ) {"
					+"return type(obj) === 'object' && !isPlainObject(obj);"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			//判定是否是数字
			fun = "function isNumeric( obj ) {"
					+"return !isNaN( parseFloat(obj) ) && isFinite( obj );"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			//判定是否是数字类型
			fun = "function isNumber( obj ) {"
					+"return type(obj) === 'number';"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			//转换为JSON字符串
			fun = "function toJson(obj){"
					+"if(isArray(obj)){"
					+"	var result = '';"
					+"	for(var i=0;i<obj.length;i++){"
					+"		result = result + ','+toJson(obj[i]);"
					+"	}"
					+"	if(result == ''){return '[]';}else{"
					+"	return '['+result.substring(1)+']';}"
					+"}else if(isPlainObject(obj)){"
					+"	var result = '';"
					+"	for(key in obj){"
					+"		result = result + ',\"'+key+'\":'+toJson(obj[key]);"
					+"	}"
					+"	if(result == ''){return '{}';}else{"
					+"	return '{'+result.substring(1)+'}';}"
					+"}else if(isNumber(obj)){"
					+"	return obj;"
					+"}else{"
					+"	return '\"'+obj+'\"';"
					+"}"
					+"}";
			engine.eval(fun,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
			if(jsPath != null && jsPath.length > 0){
				for(int i=0;i<jsPath.length;i++){
					File file = new File(jsPath[i]);
					if(file.exists()){
						Reader reader  = new InputStreamReader(new FileInputStream(file),"UTF-8");
						engine.eval(reader,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
					}else{
						throw new FileNotFoundException(jsPath[i] + " not found");
					}
				}
			}
			return true;
		} catch (UnsupportedEncodingException e) {
 			 e.printStackTrace();
		} catch (FileNotFoundException e) {
			 e.printStackTrace();
		} catch (ScriptException e) {
			 e.printStackTrace();
		}catch(Exception e){
			 e.printStackTrace();
		}
		return false;
	}
	
	public static ScriptRegion getScriptRegion(){
		return new ScriptRegion(engine);
	}
	
	public static ScriptEngine getEngine(){
		return engine;
	}
}
