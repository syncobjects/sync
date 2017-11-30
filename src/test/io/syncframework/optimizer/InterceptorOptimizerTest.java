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
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.MessageContextMock;
import io.syncframework.api.RenderResult;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.SessionContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InterceptorOptimizerTest {
	private static final Logger log = LoggerFactory.getLogger(InterceptorOptimizerTest.class);
	private static OInterceptor interceptor;
	
	@BeforeClass
	public static void setup() throws Exception {
		Class<?> interceptorClazz = ExampleInterceptor.class;
		ClassOptimizer optimizer = new ClassOptimizer();
		
		byte b[] = optimizer.optimize(interceptorClazz);
		
		// save class to check the byte codes
		File file = new File("OptimizedInterceptor.class");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(b);
		try { fos.close(); } catch(Exception ignore) {}
		
		TestClassLoader tcl = new TestClassLoader();
		tcl.defineClass("io.syncframework.optimizer.ExampleInterceptor", b);
		
		Class<?> optimizedInterceptorClazz = tcl.loadClass("io.syncframework.optimizer.ExampleInterceptor");
		Assert.assertTrue(optimizedInterceptorClazz != null);
		Assert.assertTrue(OInterceptor.class.isAssignableFrom(optimizedInterceptorClazz));
		
		interceptor = (OInterceptor)optimizedInterceptorClazz.getDeclaredConstructor().newInstance();
	}
	
	@Test
	public void t01contexts() {
		interceptor._asApplicationContext(new ApplicationContext());
		interceptor._asCookieContext(new CookieContext());
		interceptor._asErrorContext(new ErrorContext());
		interceptor._asRequestContext(new RequestContext());
		interceptor._asMessageContext(new MessageContextMock());
		interceptor._asSessionContext(new SessionContext());
	}
	
	@Test
	public void t02parameters() {
		for(String name: interceptor._asParameters().keySet()) {
			log.info("@Parameter {} {}", interceptor._asParameters().get(name).getName(), name);
		}
		interceptor._asParameter("name", "Daniel Froz");
		String name = (String)interceptor._asParameter("name");
		Assert.assertTrue(name != null);
		Assert.assertTrue(name.equals("Daniel Froz"));
	}
	
	@Test
	public void t03converter() throws Exception {
		// checking converter
		Class<?> converterClazz = (Class<?>)interceptor._asParameterConverter("date");
		
		Converter<?> converter = (Converter<?>)converterClazz.getDeclaredConstructor().newInstance();
		
		log.info("converter: {}", converter.getClass().getName());
		
		Object value = converter.convert(new String[] { "01/01/2016" });
		interceptor._asParameter("date", value);
		Date date = (Date)interceptor._asParameter("date");
		Assert.assertTrue(date != null);
		log.info("date: {}", date);
	}
	
	@Test
	public void t04before() {
		interceptor._asParameter("name", "Daniel Froz");
		Result result = interceptor._asBefore();
		Assert.assertTrue(result != null);
		Assert.assertTrue(result instanceof RenderResult);
		RenderResult render = (RenderResult)result;
		Assert.assertTrue(render.getTemplate().equals("/interceptor.ftl"));
	}
	
	@Test
	public void t05after() {
		Result result = interceptor._asAfter();
		Assert.assertTrue(result == null);
	}
}
