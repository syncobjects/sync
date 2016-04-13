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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.optimizer.OInitializer;

/**
 * @author dfroz
 */
public class InitializerBean {
	private static final Logger log = LoggerFactory.getLogger(InitializerBean.class);
	private Application application;
	private OInitializer initializer;
	
	public InitializerBean(Application application, OInitializer initializer) {
		this.application = application;
		this.initializer = initializer;
	}
	
	public void destroy() throws Exception {
		if(log.isTraceEnabled())
			log.trace("@Initializer "+this+".destroy()");
		initializer._asDestroy();
	}
	
	public void init() throws Exception {
		if(log.isDebugEnabled())
			log.debug("@Initializer "+this+".init()");
		initializer._asApplicationContext(application.getContext());
		initializer._asInit();
	}
	
	public OInitializer getInitializer() {
		return this.initializer;
	}
	
	public String toString() {
		return this.initializer.getClass().getName();
	}
}
