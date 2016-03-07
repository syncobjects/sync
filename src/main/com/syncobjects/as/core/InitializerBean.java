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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dfroz
 */
public class InitializerBean implements Wrapper {
	private static final Logger log = LoggerFactory.getLogger(InitializerBean.class);
	private Application application;
	private IInitializer initializer;
	
	public InitializerBean(Application application, IInitializer initializer) {
		this.application = application;
		this.initializer = initializer;
	}
	
	public void destroy() throws Exception {
		if(log.isTraceEnabled())
			log.trace("@Initializer "+this+".destroy()");
		
		Method method = initializer._asSettersApplicationContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context ApplicationContext to @Initializer "+this);
			method.invoke(initializer, application.getContext());
		}
		
		method = initializer._asDestroy();
		method.invoke(initializer, new Object[0]);
	}
	
	public IInitializer getInitializer() {
		return this.initializer;
	}
	
	public void init() throws Exception {
		if(log.isDebugEnabled())
			log.debug("@Initializer "+this+".init()");
		
		Method method = initializer._asSettersApplicationContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context ApplicationContext to @Initializer "+this);
			method.invoke(initializer, application.getContext());
		}
		
		method = initializer._asInit();
		try {
			method.invoke(initializer, new Object[0]);
		}
		catch(InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getCause());
		}
	}
	
	public String toString() {
		return this.initializer.getClass().getName();
	}
}
