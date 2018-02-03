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
package io.syncframework.optimizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.Controller;
import io.syncframework.api.Initializer;
import io.syncframework.api.Interceptor;

/**
 * @author dfroz
 */
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
				log.trace("@Interceptor {} identified", clazz.getName());
			
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
				log.trace("@Initializer {} identified", clazz.getName());
			
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
