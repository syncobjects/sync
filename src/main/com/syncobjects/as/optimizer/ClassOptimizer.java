package com.syncobjects.as.optimizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.Controller;
import com.syncobjects.as.api.Initializer;
import com.syncobjects.as.api.Interceptor;

public class ClassOptimizer {
	private static Logger log = LoggerFactory.getLogger(ClassOptimizer.class);
	
	public byte[] optimize(Class<?> clazz) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		if(clazz.isAnnotationPresent(Controller.class)) {
			if(log.isTraceEnabled())
				log.trace("@Controller {} identified", clazz.getName());
				
			OControllerReflector reflector = new OControllerReflector(clazz);
			reflector.reflect();
			
			ClassReader cr = new ClassReader(cl.getResourceAsStream(Type.getInternalName(clazz)+".class"));
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			OControllerClassVisitor cv = new OControllerClassVisitor(cw, reflector);
			cr.accept(cv, 0);
			return cw.toByteArray();
		}
		
		if(clazz.isAnnotationPresent(Interceptor.class)) {
			if(log.isTraceEnabled())
				log.info("@Interceptor {} identified", clazz.getName());
			
			OInterceptorReflector reflector = new OInterceptorReflector(clazz);
			reflector.reflect();
			
			ClassReader cr = new ClassReader(cl.getResourceAsStream(Type.getInternalName(clazz)+".class"));
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			OInterceptorClassVisitor cv = new OInterceptorClassVisitor(cw, reflector);
			cr.accept(cv, 0);
			return cw.toByteArray();
		}
		
		if(clazz.isAnnotationPresent(Initializer.class)) {
			if(log.isTraceEnabled())
				log.info("@Initializer {} identified", clazz.getName());
			
			OInitializerReflector reflector = new OInitializerReflector(clazz);
			reflector.reflect();
			
			ClassReader cr = new ClassReader(cl.getResourceAsStream(Type.getInternalName(clazz)+".class"));
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			OInitializerClassVisitor cv = new OInitializerClassVisitor(cw, reflector);
			cr.accept(cv, 0);
			return cw.toByteArray();
		}
		
		if(log.isTraceEnabled())
			log.trace("@Class {} is not a special class to be optimized", clazz.getName());
		
		return null;
	}
}
