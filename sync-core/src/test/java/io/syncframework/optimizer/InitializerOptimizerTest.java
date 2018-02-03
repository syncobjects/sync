package io.syncframework.optimizer;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.ApplicationContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InitializerOptimizerTest {
	private static final Logger log = LoggerFactory.getLogger(InitializerOptimizerTest.class);
	private static OInitializer initializer;
	private static ApplicationContext application;
	
	@BeforeClass
	public static void setup() throws Exception {
		Class<?> initializerClazz = ExampleInitializer.class;
		ClassOptimizer optimizer = new ClassOptimizer();
		
		byte b[] = optimizer.optimize(initializerClazz);
		
		// save class to check the byte codes
		File file = new File("OptimizedInitializer.class");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(b);
		try { fos.close(); } catch(Exception ignore) {}
		
		TestClassLoader tcl = new TestClassLoader();
		tcl.defineClass("io.syncframework.optimizer.ExampleInitializer", b);
		
		Class<?> optimizedInitializerClazz = tcl.loadClass("io.syncframework.optimizer.ExampleInitializer");
		Assert.assertTrue(optimizedInitializerClazz != null);
		Assert.assertTrue(OInitializer.class.isAssignableFrom(optimizedInitializerClazz));
		
		initializer = (OInitializer)optimizedInitializerClazz.getDeclaredConstructor().newInstance();
	}
	
	@Test
	public void t01contexts() {
		application = new ApplicationContext();
		initializer._asApplicationContext(application);
	}
	
	@Test
	public void t02init() {
		initializer._asInit();
		log.info("init called");
		Assert.assertTrue(application.get("test") != null);
	}
	
	@Test
	public void t03after() {
		initializer._asDestroy();
		log.info("destroy called");
		Assert.assertTrue(application.get("test") == null);
	}
}
