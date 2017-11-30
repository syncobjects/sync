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
package io.syncframework.optimizer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.Action;
import io.syncframework.api.ApplicationContext;
import io.syncframework.api.Converter;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.Interceptor;
import io.syncframework.api.MessageContext;
import io.syncframework.api.Parameter;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.SessionContext;
import io.syncframework.util.StringUtils;

/**
 * InterceptorReflector helps with the reflection tool of the @Interceptor annotated classes.
 * In case of the failure with the declaration, it throws an exception reporting the error.
 * 
 * @author dfroz
 */
public class OInterceptorReflector implements Reflector {
	private static final Logger log = LoggerFactory.getLogger(OInterceptorReflector.class);
	private Method after;
	private String afterType;
	private Method before;
	private String beforeType;
	private Class<?> clazz;
	private String clazzInternalName;
	private String clazzDescriptor;
	private Map<String,Class<?>> parameters = new HashMap<String,Class<?>>();
	private Map<String,Method> getters = new HashMap<String,Method>();
	private Map<String,Method> setters = new HashMap<String,Method>();
	private Map<String,Class<?>> converters = new LinkedHashMap<String,Class<?>>();
	private String applicationContext;
	private String cookieContext;
	private String errorContext;
	private String messageContext;
	private String requestContext;
	private String sessionContext;

	public OInterceptorReflector(Class<?> clazz) {
		this.clazz = clazz;
		this.clazzInternalName = Type.getInternalName(clazz);
		this.clazzDescriptor = Type.getDescriptor(clazz);
	}

	/**
	 * reflect clazz
	 */
	public void reflect() throws ReflectorException {
		if(clazz == null)
			throw new IllegalArgumentException("clazz is null");
		
		/*
		 * Reflecting @Interceptor Annotation
		 */
		Annotation annotation = clazz.getAnnotation(Interceptor.class);
		if(annotation == null)
			throw new ReflectorException("@Interceptor annotation not defined for class "+clazz.getName());

		/*
		 * Reflecting @Parameter Contexts
		 */
		for(Field field: clazz.getDeclaredFields()) {
			Class<?> type = field.getType();
			if(!type.equals(ApplicationContext.class) &&
					!type.equals(CookieContext.class) &&
					!type.equals(ErrorContext.class) &&
					!type.equals(MessageContext.class) &&
					!type.equals(RequestContext.class) &&
					!type.equals(SessionContext.class)) {
				// check for Context parameters only
				continue;
			}

			if(type.equals(ApplicationContext.class)) {
				applicationContext = field.getName();
			}
			else if(type.equals(CookieContext.class)) {
				cookieContext = field.getName();
			}
			else if(type.equals(ErrorContext.class)) {
				errorContext = field.getName();
			}
			else if(type.equals(MessageContext.class)) {
				messageContext = field.getName();
			}
			else if(type.equals(RequestContext.class)) {
				requestContext = field.getName();
			}
			else if(type.equals(SessionContext.class)) {
				sessionContext = field.getName();
			}

			if(log.isTraceEnabled())
				log.trace("@Context "+clazz.getName()+"."+field.getName()+" loaded");
		}

		/*
		 * Reflecting Parameters
		 * 
		 * Very important to notice that we are not only reflecting the Fields 
		 * but also the Getters and Setters for each field in the same operation
		 * in case that none is found, throw an Exception.
		 * 
		 * In case that the @Parameter utilizes converter (@Parameter(convert=Converter.class)), then also include
		 * it under the converters.
		 */
		for(Field field: clazz.getDeclaredFields()) {
			if(!field.isAnnotationPresent(Parameter.class))
				continue;

			Class<?> type = field.getType();
			if(type.isPrimitive()) {
				throw new ReflectorException("@Parameter "+clazz.getName()+"."+field.getName()+" cannot be defined as primitive");
			}
			
			// check for Contexts... we already treated the contexts
			if(type.equals(ApplicationContext.class) ||
					type.equals(CookieContext.class) ||
					type.equals(ErrorContext.class) ||
					type.equals(MessageContext.class) ||
					type.equals(RequestContext.class) ||
					type.equals(SessionContext.class)) {
				// check for @Parameters only but no Contexts
				continue;
			}
			
			//
			// check for the converter
			//
			Parameter parameter = (Parameter)field.getAnnotation(Parameter.class);
			Class<?> converter = parameter.converter();
			if(converter != Object.class) {
				// check if Class implements Converter
				if(!Converter.class.isAssignableFrom(converter)) {
					throw new ReflectorException(converter.getName()+" is a non qualified @Converter");
				}
				converters.put(field.getName(), converter);
			}

			parameters.put(field.getName(), type);

			String getterMethodName = "get"+StringUtils.capitalize(field.getName());
			String setterMethodName = "set"+StringUtils.capitalize(field.getName());

			try {
				Method method = clazz.getDeclaredMethod(getterMethodName, new Class<?>[0]);
				getters.put(field.getName(), method);
			}
			catch(NoSuchMethodException ignore) {
				throw new ReflectorException("@Parameter "+clazz.getName()+"."+getterMethodName+"() not defined");
			}
			try {
				Method method = clazz.getMethod(setterMethodName, new Class<?>[]{ type });
				setters.put(field.getName(), method);
			}
			catch(NoSuchMethodException ignore) {
				throw new ReflectorException("@Parameter "+clazz.getName()+"."+setterMethodName+"("+type.getName()+" "+field.getName()+") not defined");
			}

			if(log.isTraceEnabled()) {
				log.trace("@Parameter "+clazz.getName()+"."+field.getName()+" loaded");
			}
		}


		/*
		 * After method
		 */
		try {
			Method method = clazz.getDeclaredMethod("after", new Class<?>[0]);
			if(method.getReturnType() != Result.class || method.getParameterTypes().length != 0) {
				throw new ReflectorException("@Action "+clazz.getName()+"."+method.getName()+"() not returning Result object");
			}
			
			afterType = "text/html";
			if(method.isAnnotationPresent(Action.class)) {
				Action a = method.getAnnotation(Action.class);
				afterType = a.type();
			}
			
			after = method;
			if(log.isTraceEnabled()) {
				log.trace("@Action "+clazz.getName()+"."+after.getName()+"() loaded");
			}
		}
		catch(NoSuchMethodException ignore) {
			throw new ReflectorException("@Interceptor "+clazz.getName()+".after() not defined");
		}
		
		/*
		 * Before method
		 */
		try {
			Method method = clazz.getDeclaredMethod("before", new Class<?>[0]);
			if(method.getReturnType() != Result.class || method.getParameterTypes().length != 0) {
				throw new ReflectorException("@Action "+clazz.getName()+"."+method.getName()+"() not returning Result object");
			}
			
			beforeType = "text/html";
			if(method.isAnnotationPresent(Action.class)) {
				Action a = method.getAnnotation(Action.class);
				beforeType = a.type();
			}
			
			before = method;
			if(log.isTraceEnabled()) {
				log.trace("@Action "+clazz.getName()+"."+before.getName()+"() loaded");
			}
		}
		catch(NoSuchMethodException ignore) {
			throw new ReflectorException("@Interceptor "+clazz.getName()+".before() not defined");
		}
	}
	
	public Method getAfter() {
		return after;
	}
	public String getAfterType() {
		return afterType;
	}
	public Method getBefore() {
		return before;
	}
	public String getBeforeType() {
		return beforeType;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public String getClazzInternalName() {
		return clazzInternalName;
	}
	public String getClazzDescriptor() {
		return clazzDescriptor;
	}
	public Map<String, Class<?>> getConverters() {
		return converters;
	}
	public Map<String, Class<?>> getParameters() {
		return parameters;
	}
	public Map<String, Method> getGetters() {
		return getters;
	}
	public Map<String, Method> getSetters() {
		return setters;
	}
	public String getApplicationContext() {
		return applicationContext;
	}
	public String getCookieContext() {
		return cookieContext;
	}
	public String getErrorContext() {
		return errorContext;
	}
	public String getMessageContext() {
		return messageContext;
	}
	public String getRequestContext() {
		return requestContext;
	}
	public String getSessionContext() {
		return sessionContext;
	}
}
