package io.syncframework.optimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.Action;
import io.syncframework.api.ApplicationContext;
import io.syncframework.api.Interceptor;

@Interceptor
public class ExampleOptimizedInitializer implements OInitializer {
	private static final Logger log = LoggerFactory.getLogger(ExampleOptimizedInitializer.class);
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

	@Override
	public void _asDestroy() {
		destroy();
	}

	@Override
	public void _asInit() {
		init();
	}

	@Override
	public void _asApplicationContext(ApplicationContext application) {
		this.application = application;
	}
}
