/*
 * Copyright 2016 SyncObjects Ltda.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.syncobjects.as.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.Action;
import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.Controller;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.Interceptor;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.Parameter;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.SessionContext;
import com.syncobjects.as.util.StringUtils;
import com.syncobjects.asm.Type;

/**
 * ControllerReflector helps with the reflection tool of the @Controller annotated classes.
 * In case of the failure with the declaration, it throws an exception reporting the error.
 *
 * @author dfroz
 */
public class ControllerReflector implements Reflector {
	private static final Logger log = LoggerFactory.getLogger(ControllerReflector.class);
	private Map<String,Method> actions = new HashMap<String,Method>();

	private Class<?> clazz;
	private String clazzInternalName;
	private String clazzDescriptor;
	private Map<String,Class<?>> fields = new HashMap<String,Class<?>>();
	private Map<String,Method> getters = new HashMap<String,Method>();
	private Method gettersApplicationContext;
	private Method gettersCookieContext;
	private Method gettersErrorContext;
	private Method gettersMessageContext;
	private Method gettersRequestContext;
	private Method gettersSessionContext;
	private Map<String,Method> setters = new HashMap<String,Method>();
	private Method settersApplicationContext;
	private Method settersCookieContext;
	private Method settersErrorContext;
	private Method settersMessageContext;
	private Method settersRequestContext;
	private Method settersSessionContext;
	private String url;

	public ControllerReflector(Class<?> clazz) {
		this.clazz = clazz;
		this.clazzInternalName = Type.getInternalName(clazz);
		this.clazzDescriptor = Type.getDescriptor(clazz);
	}

	/**
	 * reflect clazz
	 */
	public void reflect() throws ReflectorException {
		/*
		 * Reflecting Controller Annotation
		 */
		Annotation annotation = clazz.getAnnotation(Controller.class);
		if(annotation == null)
			throw new ReflectorException("@Controller annotation not defined for class "+clazz.getName());
		Controller controllerAnnotation = (Controller)annotation;
		this.url = controllerAnnotation.url();

		/*
		 * Reflecting Specials
		 */
		for(Field field: clazz.getDeclaredFields()) {
			if(field.isAnnotationPresent(Parameter.class))
				continue;

			Class<?> type = field.getType();
			Method getterMethod = null;
			Method setterMethod = null;

			String getterMethodName = "get"+StringUtils.capitalize(field.getName());
			try {
				getterMethod = clazz.getDeclaredMethod(getterMethodName, new Class<?>[0]);
			}
			catch(NoSuchMethodException ignore) {
				if(type.equals(ApplicationContext.class) ||
						type.equals(CookieContext.class) ||
						type.equals(ErrorContext.class) ||
						type.equals(MessageContext.class) ||
						type.equals(RequestContext.class) ||
						type.equals(SessionContext.class))
					throw new ReflectorException(clazz.getName()+"."+getterMethodName+"() not defined");
			}

			String setterMethodName = "set"+StringUtils.capitalize(field.getName());
			try {
				setterMethod = clazz.getDeclaredMethod(setterMethodName, new Class<?>[]{ type });
			}
			catch(NoSuchMethodException ignore) {
				if(type.equals(ApplicationContext.class) ||
						type.equals(CookieContext.class) ||
						type.equals(ErrorContext.class) ||
						type.equals(MessageContext.class) ||
						type.equals(RequestContext.class) ||
						type.equals(SessionContext.class))
					throw new ReflectorException(clazz.getName()+"."+setterMethodName+"("+type.getName()+" "+field.getName()+") not defined");
			}

			boolean contextType = false;

			if(type.equals(ApplicationContext.class)) {
				gettersApplicationContext = getterMethod;
				settersApplicationContext = setterMethod;
				contextType = true;
			}
			else if(type.equals(CookieContext.class)) {
				gettersCookieContext = getterMethod;
				settersCookieContext = setterMethod;
				contextType = true;
			}
			else if(type.equals(ErrorContext.class)) {
				gettersErrorContext = getterMethod;
				settersErrorContext = setterMethod;
				contextType = true;
			}
			else if(type.equals(MessageContext.class)) {
				gettersMessageContext = getterMethod;
				settersMessageContext = setterMethod;
				contextType = true;
			}
			else if(type.equals(RequestContext.class)) {
				gettersRequestContext = getterMethod;
				settersRequestContext = setterMethod;
				contextType = true;
			}
			else if(type.equals(SessionContext.class)) {
				gettersSessionContext = getterMethod;
				settersSessionContext = setterMethod;
				contextType = true;
			}

			if(contextType) {
				if(log.isTraceEnabled())
					log.trace("@Context "+clazz.getName()+"."+field.getName()+" loaded");
			}
		}

		/*
		 * Reflecting Parameters
		 * 
		 * Very important to notice that we are not only reflecting the Fields 
		 * but also the Getters and Setters for each field in the same operation
		 * in case that one getter or setter is NOT found, throw an Exception.
		 */
		for(Field field: clazz.getDeclaredFields()) {
			if(!field.isAnnotationPresent(Parameter.class))
				continue;

			Class<?> type = field.getType();
			if(type.isPrimitive()) {
				throw new ReflectorException("@Parameter "+clazz.getName()+"."+field.getName()+" cannot be defined as primitive");
			}

			fields.put(field.getName(), type);

			String getterMethodName = "get"+StringUtils.capitalize(field.getName());
			String setterMethodName = "set"+StringUtils.capitalize(field.getName());

			try {
				Method method = clazz.getDeclaredMethod(getterMethodName, new Class<?>[0]);
				getters.put(field.getName(), method);
			}
			catch(NoSuchMethodException ignore) {
				throw new ReflectorException(clazz.getName()+"."+getterMethodName+"() not defined");
			}
			try {
				Method method = clazz.getMethod(setterMethodName, new Class<?>[]{ type });
				setters.put(field.getName(), method);
			}
			catch(NoSuchMethodException ignore) {
				throw new ReflectorException(clazz.getName()+"."+setterMethodName+"("+type.getName()+" "+field.getName()+") not defined");
			}

			if(log.isTraceEnabled())
				log.trace("@Parameter "+clazz.getName()+"."+field.getName()+" loaded");
		}

		/*
		 * Reflecting ACTIONS
		 */
		for(Method method: clazz.getDeclaredMethods()) {
			if(!method.isAnnotationPresent(Action.class))
				continue;
			
			// check whether intercepted by @Interceptor classes
			Action a = method.getAnnotation(Action.class);
			Class<?> interceptors[] = a.interceptedBy();
			for(int i=0; i < interceptors.length; i++) {
				if(interceptors[i] == java.lang.Object.class) {
					// sometimes @Action() uses java.lang.Object as interceptedBy annotation
					continue;
				}
				Annotation ai = interceptors[i].getAnnotation(Interceptor.class);
				if(ai == null) {
					throw new ReflectorException(
							"@Action "+clazz.getName()+"."+method.getName()+"() intercepted by "+interceptors[i]+
							", which is not an @Interceptor.");
				}
			}
			
			if(method.getReturnType() != Result.class || method.getParameterTypes().length != 0) {
				throw new ReflectorException("@Action "+clazz.getName()+"."+method.getName()+"() not returning Result object");
			}
			actions.put(method.getName(), method);
			if(log.isTraceEnabled()) {
				log.trace("@Action "+clazz.getName()+"."+method.getName()+"() loaded");
			}
		}
	}

	public Map<String, Method> getActions() {
		return actions;
	}
	public void setActions(Map<String, Method> actions) {
		this.actions = actions;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public String getClazzInternalName() {
		return clazzInternalName;
	}
	public void setClazzInternalName(String clazzInternalName) {
		this.clazzInternalName = clazzInternalName;
	}
	public String getClazzDescriptor() {
		return clazzDescriptor;
	}
	public void setClazzDescriptor(String clazzDescriptor) {
		this.clazzDescriptor = clazzDescriptor;
	}
	public Map<String, Class<?>> getFields() {
		return fields;
	}
	public void setFields(Map<String, Class<?>> fields) {
		this.fields = fields;
	}
	public Map<String, Method> getGetters() {
		return getters;
	}
	public void setGetters(Map<String, Method> getters) {
		this.getters = getters;
	}
	public Method getGettersApplicationContext() {
		return gettersApplicationContext;
	}
	public void setGettersApplicationContext(Method gettersApplicationContext) {
		this.gettersApplicationContext = gettersApplicationContext;
	}
	public Method getGettersCookieContext() {
		return gettersCookieContext;
	}
	public void setGettersCookieContext(Method gettersCookieContext) {
		this.gettersCookieContext = gettersCookieContext;
	}
	public Method getGettersErrorContext() {
		return gettersErrorContext;
	}
	public void setGettersErrorContext(Method gettersErrorContext) {
		this.gettersErrorContext = gettersErrorContext;
	}
	public Method getGettersMessageContext() {
		return gettersMessageContext;
	}
	public void setGettersMessageContext(Method gettersMessageContext) {
		this.gettersMessageContext = gettersMessageContext;
	}
	public Method getGettersRequestContext() {
		return gettersRequestContext;
	}
	public void setGettersRequestContext(Method gettersRequestContext) {
		this.gettersRequestContext = gettersRequestContext;
	}
	public Method getGettersSessionContext() {
		return gettersSessionContext;
	}
	public void setGettersSessionContext(Method gettersSessionContext) {
		this.gettersSessionContext = gettersSessionContext;
	}
	public Map<String, Method> getSetters() {
		return setters;
	}
	public void setSetters(Map<String, Method> setters) {
		this.setters = setters;
	}
	public Method getSettersApplicationContext() {
		return settersApplicationContext;
	}
	public void setSettersApplicationContext(Method settersApplicationContext) {
		this.settersApplicationContext = settersApplicationContext;
	}
	public Method getSettersCookieContext() {
		return settersCookieContext;
	}
	public void setSettersCookieContext(Method settersCookieContext) {
		this.settersCookieContext = settersCookieContext;
	}
	public Method getSettersErrorContext() {
		return settersErrorContext;
	}
	public void setSettersErrorContext(Method settersErrorContext) {
		this.settersErrorContext = settersErrorContext;
	}
	public Method getSettersMessageContext() {
		return settersMessageContext;
	}
	public void setSettersMessageContext(Method settersMessageContext) {
		this.settersMessageContext = settersMessageContext;
	}
	public Method getSettersRequestContext() {
		return settersRequestContext;
	}
	public void setSettersRequestContext(Method settersRequestContext) {
		this.settersRequestContext = settersRequestContext;
	}
	public Method getSettersSessionContext() {
		return settersSessionContext;
	}
	public void setSettersSessionContext(Method settersSessionContext) {
		this.settersSessionContext = settersSessionContext;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
