package com.etone.ark.kernel.service;

import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import com.etone.ark.kernel.task.ActionNodeHandler;
import com.google.gson.Gson;

public class ScriptRegion {

	static Logger logger = Logger.getLogger(ScriptRegion.class);
	
	private ScriptEngine engine;
	private Bindings bingdings;
	
	public ScriptRegion(ScriptEngine engine){
		this.engine = engine;
		this.bingdings = this.engine.createBindings();
	}
	
	public void put(String name,Object value)throws ScriptException{
		if(Map.class.isInstance(value)){
			put(name,Map.class.cast(value));
		}else if(List.class.isInstance(value)){
			put(name,List.class.cast(value));
		}else if(String.class.isInstance(value)){
			put(name,value.toString());
		}else {
			evel("var "+name+"="+value);
		}
	}
	
	public void put(String name,String value) throws ScriptException{
		if(value == null){
			evel("var "+name+"=null");
		}else{
			value = value.replace("'", "\\'");
			evel("var "+name+"='"+value+"'");
		}
	}
	
	public void put(String name,Map<String,Object> map) throws ScriptException{
		evel("var "+name+"={}");
		putIn(name,map);
	}
	
	public void put(String name,List<Object> list) throws ScriptException{
		evel("var "+name+"=[]");
		putIn(name,list);
	}
	
	public void putIn(String name,String value) throws ScriptException{
		if(value == null){
			evel("var "+name+"=null");
		}else{
			value = value.replace("'", "\\'");
			evel(name+"='"+value+"'");
		}
	}
	
	public void putIn(String name,Map<String,Object> map) throws ScriptException{
		for(String key : map.keySet()){
			Object value = map.get(key);
			if(Map.class.isInstance(value)){
				evel(name+"."+key+"={}");
				putIn(name+"."+key,Map.class.cast(value));
			}else if(List.class.isInstance(value)){
				evel(name+"."+key+"=[]");
				putIn(name+"."+key,List.class.cast(value));
			}else if(String.class.isInstance(value)){
				putIn(name+"."+key,value.toString());
			}else {
				evel(name+"."+key+"="+value);
			}
		}
	}
	
	public void putIn(String name,List<Object> list) throws ScriptException{
		for(int i=0;i<list.size();i++){
			Object value = list.get(i);
			if(Map.class.isInstance(value)){
				evel(name+"["+i+"]"+"={}");
				putIn(name+"["+i+"]",Map.class.cast(value));
			}else if(List.class.isInstance(value)){
				evel(name+"["+i+"]"+"=[]");
				putIn(name+"["+i+"]",List.class.cast(value));
			}else if(String.class.isInstance(value)){
				putIn(name+"["+i+"]",value.toString());
			}else {
				evel(name+"["+i+"]="+value);
			}
		}
	}
	
	public void putJson(String name,String json) throws ScriptException{
		evel("var "+name+"="+json);
	}
	
	public Object get(String name) throws ScriptException{
		Object isObj = evel("isPlainObject("+name+")");
		if(Boolean.valueOf(isObj.toString())){
			Object value = evel("toJson("+name+")");
			logger.debug("从JavaScript脚本中获取参数："+value);
			return new Gson().fromJson(value.toString(), Map.class);
		}
		Object isArray = evel("isArray("+name+")");
		if(Boolean.valueOf(isArray.toString())){
			Object value = evel("toJson("+name+")");
			return new Gson().fromJson(value.toString(), List.class);
		}
		Object isNumber = evel("isNumber("+name+")");
		if(Boolean.valueOf(isNumber.toString())){
			Object value = evel(name);
			if(value.toString().indexOf(".")>0){
				return Double.valueOf(value.toString());
			}else{
				return Long.valueOf(value.toString());
			}
		}
		return evel(name);
	}
	
	public Object evel(String script) throws ScriptException{
		return engine.eval(script, bingdings);
	}
}
