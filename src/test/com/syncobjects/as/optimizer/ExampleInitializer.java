package com.syncobjects.as.optimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.Action;
import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.Initializer;

@Initializer
public class ExampleInitializer {
	private static final Logger log = LoggerFactory.getLogger(ExampleInitializer.class);
	private ApplicationContext application;
	
	@Action
	public void init() {
		application.put("test", "test");
		log.info("Application Initialized");
	}
	
	@Action
	public void destroy() {
		application.clear();
	}

	public ApplicationContext getApplication() {
		return application;
	}

	public void setApplication(ApplicationContext application) {
		this.application = application;
	}
}
