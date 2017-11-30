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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.optimizer.OController;

/**
 * @author dfroz
 */
public class ControllerFactory {
	private static final Logger log = LoggerFactory.getLogger(ControllerFactory.class);
	private Application application;
	private URLPattern urls[] = new URLPattern[0];
	private Map<URLPattern, Class<?>> map = new ConcurrentHashMap<URLPattern, Class<?>>();

	public ControllerFactory(Application application) {
		this.application = application;
	}

	public boolean find(ControllerBean controllerBean, String url) throws Exception {
		if(log.isTraceEnabled())
			log.trace("finding controller to handle request: "+url);
		
		String actionName = null;
		
		URLPattern urlPattern = null;
		for(int i=0; i < urls.length; i++) {
			actionName = urls[i].action(url);
			if(actionName != null) {
				urlPattern = urls[i];
				break;
			}
		}
		if(urlPattern == null) {
			if(log.isDebugEnabled())
				log.debug(urls.length+" @Controllers in the pool, none responsible for the url: "+url);
			return false;
		}
		
		Class<?> clazz = map.get(urlPattern);
		OController controller = (OController)clazz.getDeclaredConstructor().newInstance();
		
		String action = null;
		if(controller._asActionIsDefined(actionName))
			action = actionName;
		if(action == null && controller._asActionIsDefined("main"))
			action = "main";
		if(action == null) {
			if(log.isDebugEnabled()) {
				log.debug("no @Action found on @Controller "+controller.getClass().getName()+
					" to handle request: "+url);
			}
			return false;
		}
		
		if(log.isTraceEnabled())
			log.trace("found @Controller {} -> @Action {}()", controller.getClass().getName(), action);
		
		controllerBean.setApplication(application);
		controllerBean.setController(controller);
		controllerBean.setAction(action);
		
		return true;
	}
	
	/**
	 * This method is responsible to register new controllers to the application.<br/>
	 * <br/>
	 * The controllers are sorted by URL length before to be included into the controllers[]. This allows quick search
	 * for the more specific Controller to the generic Controller.<br/>
	 * <br/>
	 * @param clazz
	 */
	public void register(Class<?> clazz) throws Exception {
		if(clazz == null)
			throw new IllegalArgumentException("class cannot be null");
		if(clazz.isAssignableFrom(OController.class))
			throw new IllegalArgumentException(clazz+" is not a valid @Controller");
		
		OController controller = (OController)clazz.getDeclaredConstructor().newInstance();
		String url = controller._asUrl();
		if(log.isTraceEnabled())
			log.trace("@Controller "+controller.getClass().getName()+" bound to URL pattern: "+url);
		
		URLPattern pattern = new URLPattern();
		pattern.compile(url);
		
		Class<?> c = map.get(pattern);
		if(c != null) {
			throw new RuntimeException("@Controller "+c.getName()+" already registered on URL pattern "+url+"; review @Controller "+clazz.getName());
		}
		
		//
		// Weight is based on the length of the pattern... A more specific @Controller may handle the request
		//		
		List<URLPattern> sorted = new LinkedList<URLPattern>();
		if(urls.length > 0) {
			for(URLPattern p: urls) {
				sorted.add(p);
			}
		}
		sorted.add(pattern);
		
		Collections.sort(sorted, new Comparator<URLPattern>() {
			public int compare(URLPattern p1, URLPattern p2) {
				String url1 = p1.toString();
				String url2 = p2.toString();
				return url1.length() < url2.length() ? 1 : (url1.length() == url2.length() ? 0 : -1);
			}
		});

		urls = new URLPattern[sorted.size()];
		for(int i=0; i < sorted.size(); i++) {
			urls[i] = sorted.get(i);
			c = map.get(urls[i]);
			if(c != null)
				map.put(urls[i], c);
		}
		map.put(pattern, clazz);
		
		if(log.isTraceEnabled())
			log.trace("@Controller {} registered", clazz.getName());
	}
}