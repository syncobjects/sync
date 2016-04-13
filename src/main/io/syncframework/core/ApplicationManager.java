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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dfroz
 *
 */
public class ApplicationManager {
	private static Logger log = LoggerFactory.getLogger(ApplicationManager.class);
	private static Application applications[];
	private static Map<String, Application> domains;
	
	static {
		domains = new HashMap<String, Application>();
	}
	
	public static void register(Application application) {
		ArrayList<Application> apps = new ArrayList<Application>();
		if(applications != null) {
			for(Application a: applications) {
				apps.add(a);
			}
		}
		apps.add(application);
		
		for(String domain: application.getDomains()) {
			domains.put(domain, application);
		}
		
		applications = apps.toArray(new Application[0]);
		if(log.isTraceEnabled())
			log.trace("{} registered", application);
	}
	
	public static Application getApplication(String domain) {
		if(domain == null)
			throw new IllegalArgumentException("domain");
		
		if(applications == null) {
			if(log.isWarnEnabled())
				log.warn("no application loaded; dropping request to domain {}", domain);
			return null;
		}
		
		//
		// In case of just one application per server, then return this application to handle the request.
		// Easy and quicker method.
		//
		if(applications.length == 1) {
			return applications[0];
		}
		
		//
		// guarantees lookup
		//
		domain = domain.toLowerCase();
		
		//
		// For multiples applications under the server, then identify the correct application considering
		// the domain name
		//
		return domains.get(domain);
	}
}
