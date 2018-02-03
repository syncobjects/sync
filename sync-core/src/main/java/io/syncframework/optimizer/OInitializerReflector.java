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

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.Initializer;
import io.syncframework.api.MessageContext;
import io.syncframework.api.RequestContext;
import io.syncframework.api.SessionContext;

/**
 * @author dfroz
 */
public class OInitializerReflector implements Reflector {
	private static Logger log = LoggerFactory.getLogger(OInitializerReflector.class);
	private Class<?> clazz;
	private String clazzDescriptor;
	private String clazzInternalName;
	private Method init;
	private Method destroy;
	private String applicationContext;
	private String errorContext;
	private String messageContext;

	public OInitializerReflector(Class<?> clazz) {
		this.clazz = clazz;
		this.clazzInternalName = Type.getInternalName(clazz);
		this.clazzDescriptor = Type.getDescriptor(clazz);
	}

	public void reflect() throws ReflectorException {
		if(clazz == null)
			throw new IllegalArgumentException("clazz is null");
		
		/*
		 * Reflecting @Initializer Annotation
		 */
		Annotation annotation = clazz.getAnnotation(Initializer.class);
		if(annotation == null)
			throw new ReflectorException("@Initializer annotation not defined for class "+clazz.getName());

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
			else if(type.equals(ErrorContext.class)) {
				errorContext = field.getName();
			}
			else if(type.equals(MessageContext.class)) {
				messageContext = field.getName();
			}

			if(log.isTraceEnabled())
				log.trace("@Context "+clazz.getName()+"."+field.getName()+" loaded");
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
	public Method getInit() {
		return init;
	}
	public String getApplicationContext() {
		return applicationContext;
	}
	public String getErrorContext() {
		return errorContext;
	}
	public String getMessageContext() {
		return messageContext;
	}
}
