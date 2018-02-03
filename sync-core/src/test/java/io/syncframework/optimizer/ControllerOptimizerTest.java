package io.syncframework.optimizer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.Converter;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.MessageContextMock;
import io.syncframework.api.RenderResult;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.SessionContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ControllerOptimizerTest {
	private static final Logger log = LoggerFactory.getLogger(ControllerOptimizerTest.class);
	private static OController controller;
	
	@BeforeClass
	public static void setup() throws Exception {
		Class<?> controllerClazz = ExampleController.class;
		ClassOptimizer optimizer = new ClassOptimizer();
		
		byte b[] = optimizer.optimize(controllerClazz);
		
		// save class to check the byte codes
		File file = new File("OptimizedController.class");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(b);
		try { fos.close(); } catch(Exception ignore) {}
		
		TestClassLoader tcl = new TestClassLoader();
		tcl.defineClass("io.syncframework.optimizer.ExampleController", b);
		
		Class<?> optimizedControllerClazz = tcl.loadClass("io.syncframework.optimizer.ExampleController");
		Assert.assertTrue(optimizedControllerClazz != null);
		Assert.assertTrue(OController.class.isAssignableFrom(optimizedControllerClazz));
		
		controller = (OController)optimizedControllerClazz.getDeclaredConstructor().newInstance();
	}
	
	@Test
	public void t01url() throws Exception {
		log.info("url {}", controller._asUrl());
		Assert.assertTrue(controller._asUrl().equals("/*"));
	}
	
	@Test
	public void t02contexts() {
		controller._asApplicationContext(new ApplicationContext());
		controller._asErrorContext(new ErrorContext());
		controller._asMessageContext(new MessageContextMock());
		controller._asRequestContext(new RequestContext());
		controller._asSessionContext(new SessionContext());
	}
	
	@Test
	public void t03parameters() {
		for(String name: controller._asParameters().keySet()) {
			log.info("@Parameter {} {}", controller._asParameters().get(name).getName(), name);
		}
		controller._asParameter("name", "Daniel Froz");
		String name = (String)controller._asParameter("name");
		Assert.assertTrue(name != null);
		Assert.assertTrue(name.equals("Daniel Froz"));
	}
	
	@Test
	public void t04converter() throws Exception {
		// checking converter
		Class<?> converterClazz = (Class<?>)controller._asParameterConverter("date");
		
		Converter<?> converter = (Converter<?>)converterClazz.getDeclaredConstructor().newInstance();
		
		log.info("converter: {}", converter.getClass().getName());
		
		Object value = converter.convert(new String[] { "01/01/2016" });
		controller._asParameter("date", value);
		Date date = (Date)controller._asParameter("date");
		Assert.assertTrue(date != null);
		log.info("date: {}", date);
	}
	
	@Test
	public void t05actions() {
		Assert.assertTrue(controller._asActionIsDefined("main") == true);
		Assert.assertTrue(controller._asActionIsDefined("upload") == true);
		
		Result result = controller._asAction("main");
		Assert.assertTrue(result != null);
		Assert.assertTrue(result instanceof RenderResult);
		RenderResult render = (RenderResult)result;
		Assert.assertTrue(render.getTemplate().equals("/main.ftl"));
	}
	
	@Test
	public void t06actionsInvalidCall() {
		Assert.assertTrue(controller._asActionIsDefined("unknown") == false);
		
		try {
			controller._asAction("unknown");
			Assert.assertTrue(false);
		}
		catch(Exception e) {
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void t07interceptors() {
		Class<?> interceptors[] = controller._asActionInterceptors("upload");
		Assert.assertTrue(interceptors != null);
		Assert.assertTrue(interceptors.length == 2);
		Assert.assertTrue(interceptors[0] == ExampleInterceptor.class);
		Assert.assertTrue(interceptors[1] == DummyInterceptor.class);
	}
	
	@Test
	public void t08interceptorsInvalidCall() {
		Class<?> interceptors[] = controller._asActionInterceptors("unknown");
		Assert.assertTrue(interceptors == null);
	}
}

class TestClassLoader extends ClassLoader {
	public Class<?> defineClass(String name, byte[] b) {
		return super.defineClass(name, b, 0, b.length);
	}
}
