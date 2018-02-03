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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.syncframework.optimizer.OInitializer;
import io.syncframework.optimizer.OInterceptor;

/**
 * @author dfroz
 */
public class InterceptorFactory {
	private Application application;
	private Map<Class<?>,InterceptorBean> map = new ConcurrentHashMap<Class<?>,InterceptorBean>();

	public InterceptorFactory(Application application) {
		this.application = application;
	}

	public InterceptorBean[] find(Class<?> classes[]) {
		if(classes == null)
			return null;
		
		int total = 0;		
		for(int i=0; i < classes.length; i++) {
			// if(classes[i].equals(Object.class))
			//	continue;
			if(map.containsKey(classes[i]))
				total++;
		}
		if(total == 0) {
			return null;
		}
		
		int j=0;
		InterceptorBean interceptors[] = new InterceptorBean[total];
		for(int i=0; i < classes.length; i++) {
			// if(classes[i].equals(Object.class))
			//	continue;
			InterceptorBean interceptor = map.get(classes[i]);
			interceptors[j++] = interceptor;
		}
		return interceptors;
	}

	public void register(Class<?> clazz) throws Exception {
		if(clazz == null)
			throw new IllegalArgumentException("class cannot be null");
		if(clazz.isAssignableFrom(OInitializer.class))
			throw new IllegalArgumentException(clazz+" is not a valid @Initializer");

		OInterceptor i = (OInterceptor)clazz.getDeclaredConstructor().newInstance();
		map.put(clazz, new InterceptorBean(application, i));
	}
}
