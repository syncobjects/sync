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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.Initializer;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.Parameter;
import com.syncobjects.as.util.StringUtils;
import com.syncobjects.asm.Type;

/**
 * 
 * @author dfroz
 *
 */
public class InitializerReflector implements Reflector {
	private static Logger log = LoggerFactory.getLogger(InitializerReflector.class);
	private Class<?> clazz;
	private String clazzDescriptor;
	private String clazzInternalName;
	private Method destroy;
	private Method gettersApplicationContext;
	private Method gettersErrorContext;
	private Method gettersMessageContext;
	private Method init;
	private Method settersApplicationContext;
	private Method settersErrorContext;
	private Method settersMessageContext;

	public InitializerReflector(Class<?> clazz) {
		this.clazz = clazz;
		this.clazzInternalName = Type.getInternalName(clazz);
		this.clazzDescriptor = Type.getDescriptor(clazz);
	}

	public void reflect() throws ReflectorException {
		if(!clazz.isAnnotationPresent(Initializer.class))
			throw new ReflectorException(clazz+" is not an Initializer");

		/*
		 * Reflecting Specials
		 */
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
						type.equals(ErrorContext.class) ||
						type.equals(MessageContext.class))
					throw new ReflectorException(clazz.getName()+"."+getterMethodName+"() not defined");
			}

			String setterMethodName = "set"+StringUtils.capitalize(field.getName());
			try {
				setterMethod = clazz.getDeclaredMethod(setterMethodName, new Class<?>[]{ type });
			}
			catch(NoSuchMethodException ignore) {
				if(type.equals(ApplicationContext.class) ||
						type.equals(ErrorContext.class) ||
						type.equals(MessageContext.class))
					throw new ReflectorException(clazz.getName()+"."+setterMethodName+"("+type.getName()+" "+field.getName()+") not defined");
			}

			boolean contextType = false;

			if(type.equals(ApplicationContext.class)) {
				gettersApplicationContext = getterMethod;
				settersApplicationContext = setterMethod;
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

			String getterMethodName = "get"+StringUtils.capitalize(field.getName());
			try {
				Method method = clazz.getDeclaredMethod(getterMethodName, new Class<?>[0]);
				if(type.equals(ApplicationContext.class))
					gettersApplicationContext = method;
				else if(type.equals(ErrorContext.class))
					gettersErrorContext = method;
				else if(type.equals(MessageContext.class))
					gettersMessageContext = method;
			}
			catch(NoSuchMethodException ignore) {
				throw new ReflectorException(clazz.getName()+"."+getterMethodName+"() not defined");
			}

			String setterMethodName = "set"+StringUtils.capitalize(field.getName());
			try {
				Method method = clazz.getDeclaredMethod(setterMethodName, new Class<?>[]{ type });
				if(type.equals(ApplicationContext.class)) {
					settersApplicationContext = method;
				}
				else if(type.equals(ErrorContext.class)) {
					settersErrorContext = method;
				}
				else if(type.equals(MessageContext.class)) {
					settersMessageContext = method;
				}
			}
			catch(NoSuchMethodException ignore) {
				throw new ReflectorException(clazz.getName()+"."+setterMethodName+"("+type.getName()+" "+field.getName()+") not defined");
			}

			if(log.isTraceEnabled()) {
				log.trace("@Parameter "+clazz.getName()+"."+field.getName()+" loaded");
			}
		}

		/*
		 * Init & Destroy methods
		 */
		try {
			init = clazz.getDeclaredMethod("init", new Class<?>[0]);
			if(log.isTraceEnabled()) {
				log.debug("@Action "+clazz.getName()+"."+init.getName()+"() loaded");
			}
		}
		catch(NoSuchMethodException ignore) {
			throw new ReflectorException("@Initializer "+clazz.getName()+".init() not defined");
		}

		try {
			destroy = clazz.getDeclaredMethod("destroy", new Class<?>[0]);
			if(log.isTraceEnabled()) {
				log.trace("@Action "+clazz.getName()+"."+destroy.getName()+"() loaded");
			}
		}
		catch(NoSuchMethodException ignore) {
			throw new ReflectorException("@Initializer "+clazz.getName()+".destroy() not defined");
		}
	}


	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public String getClazzDescriptor() {
		return clazzDescriptor;
	}
	public void setClazzDescriptor(String clazzDescriptor) {
		this.clazzDescriptor = clazzDescriptor;
	}
	public String getClazzInternalName() {
		return clazzInternalName;
	}
	public void setClazzInternalName(String clazzInternalName) {
		this.clazzInternalName = clazzInternalName;
	}

	public Method getDestroy() {
		return destroy;
	}

	public void setDestroy(Method destroy) {
		this.destroy = destroy;
	}

	public Method getGettersApplicationContext() {
		return gettersApplicationContext;
	}
	public void setGettersApplicationContext(Method gettersApplicationContext) {
		this.gettersApplicationContext = gettersApplicationContext;
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
	public void setGettersMessages(Method gettersMessageContext) {
		this.gettersMessageContext = gettersMessageContext;
	}
	public Method getInit() {
		return init;
	}
	public void setInit(Method init) {
		this.init = init;
	}
	public Method getSettersApplicationContext() {
		return settersApplicationContext;
	}
	public void setSettersApplicationContext(Method settersApplicationContext) {
		this.settersApplicationContext = settersApplicationContext;
	}
	public Method getSettersErrorContext() {
		return settersErrorContext;
	}
	public void setSettersErrors(Method settersErrorContext) {
		this.settersErrorContext = settersErrorContext;
	}
	public Method getSettersMessageContext() {
		return settersMessageContext;
	}
	public void setSettersMessages(Method settersMessageContext) {
		this.settersMessageContext = settersMessageContext;
	}
}
