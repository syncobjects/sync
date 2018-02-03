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
package io.syncframework.core;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.optimizer.OInitializer;

/**
 * 
 * @author dfroz
 *
 */
public class InitializerFactory {
	private static final Logger log = LoggerFactory.getLogger(InitializerFactory.class);
	private Application application;
	private List<InitializerBean> initializers = new LinkedList<InitializerBean>();
	
	public InitializerFactory(Application application) {
		this.application = application;
	}
	
	public void init() throws Exception {
		for(InitializerBean initializer: initializers) {
			if(log.isTraceEnabled())
				log.trace("calling @Initializer "+initializer+".init()");
			initializer.init();
		}
	}
	
	public void destroy() throws Exception {
		for(InitializerBean initializer: initializers) {
			if(log.isTraceEnabled())
				log.trace("calling @Initializer "+initializer+".destroy()");
			initializer.destroy();
		}
	}
	
	public void register(Class<?> clazz) throws Exception {
		if(clazz == null)
			throw new IllegalArgumentException("class cannot be null");
		if(clazz.isAssignableFrom(OInitializer.class))
			throw new IllegalArgumentException(clazz+" is not a valid @Initializer");
		
		OInitializer i = (OInitializer)clazz.getDeclaredConstructor().newInstance();
		initializers.add(new InitializerBean(application, i));
	}
}
