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
package com.syncobjects.as.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.Controller;
import com.syncobjects.as.api.Initializer;
import com.syncobjects.as.api.Interceptor;
import com.syncobjects.as.util.FileUtils;
import com.syncobjects.asm.ClassReader;
import com.syncobjects.asm.ClassWriter;

/**
 * This class is responsible for compilation of the classes over the ClassesDirectory, creating a fast-access classes under the 
 * application's WorkDirectory 
 * 
 * @author dfroz
 * 
 */
public class Enhancer {
	private static final Logger log = LoggerFactory.getLogger(Enhancer.class);
	private final List<File> classFiles = new ArrayList<File>();
	private Application application;
	
	public Enhancer(Application application) {
		this.application = application;
	}

	/**
	 * This method works to enhance the classes creating alternative versions of the Controllers, Interceptors and Initializers.
	 * Also load the Converters into the Factory...
	 * 
	 * The generated class files will extend the IController, IInterceptor, IInitializer interfaces for a matter of speed execution
	 * We shall expect better performance during runtime.
	 * 
	 * Files are not loaded directly into memory to facilitate to troubleshoot through bytecode readability.
	 * 
	 * @param classFile
	 * @throws Exception
	 */
	private void doEnhance(ClassFile classFile) throws Exception {
		Class<?> clazz = classFile.getClazz();
		ClassLoader cl = classFile.getClassLoader();
		
		if(clazz.isAnnotationPresent(Controller.class)) {
			if(log.isTraceEnabled())
				log.trace("identified "+clazz.getName()+" as @Controller");

			ControllerReflector _cr = new ControllerReflector(clazz);
			_cr.reflect();
			
			ClassReader cr = new ClassReader(cl.getResourceAsStream(_cr.getClazzInternalName()+".class"));
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ControllerClassAdapter cca = new ControllerClassAdapter(cw, _cr);
			cr.accept(cca, 0);
			byte b[] = cw.toByteArray();

			File file = new File(application.getConfig().getWorkDirectory(), classFile.getClassPath());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(b);
			try { fos.close(); } catch(Exception ignore) {}
			if(log.isTraceEnabled())
				log.trace("created enhanced @Controller "+classFile.getClassName());
			return;
		}
		if(clazz.isAnnotationPresent(Interceptor.class)) {
			if(log.isTraceEnabled())
				log.trace("identified "+clazz.getName()+" as @Interceptor");

			InterceptorReflector _ir = new InterceptorReflector(clazz);
			_ir.reflect();

			ClassReader cr = new ClassReader(cl.getResourceAsStream(_ir.getClazzInternalName()+".class"));
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			InterceptorClassAdapter ica = new InterceptorClassAdapter(cw, _ir);
			cr.accept(ica, 0);
			byte b[] = cw.toByteArray();

			File file = new File(application.getConfig().getWorkDirectory(), classFile.getClassPath());
			
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(b);
			try { fos.close(); } catch(Exception ignore) {}
			
			if(log.isTraceEnabled())
				log.trace("created enhanced @Interceptor "+classFile.getClassName());
			
			return;
		}
		if(clazz.isAnnotationPresent(Initializer.class)) {
			if(log.isTraceEnabled())
				log.trace("identified "+clazz.getName()+" as @Initializer");

			InitializerReflector _ir = new InitializerReflector(clazz);
			_ir.reflect();

			ClassReader cr = new ClassReader(cl.getResourceAsStream(_ir.getClazzInternalName()+".class"));
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			InitializerClassAdapter ica = new InitializerClassAdapter(cw, _ir);
			cr.accept(ica, 0);
			byte b[] = cw.toByteArray();

			File file = new File(application.getConfig().getWorkDirectory(), classFile.getClassPath());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(b);
			try { fos.close(); } catch(Exception ignore) {}
			
			if(log.isTraceEnabled())
				log.trace("created enhanced @Initializer "+classFile.getClassName());
			return;
		}
		if(application.getConverterFactory().isConverter(clazz)) {
			if(log.isTraceEnabled())
				log.trace("identified "+clazz.getName()+" as @Converter");
			application.getConverterFactory().register(clazz);
			return;
		}
	}

	private void scanClassesDirectory(File dir) throws IOException {
		for(File file: dir.listFiles()) {
			if(file.getName().equals(".") || file.getName().equals(".."))
				continue;
			if(file.isDirectory()) {
				scanClassesDirectory(file);
				continue;
			}
			
			// copy file to the work directory
			File workFile = new File(application.getConfig().getWorkDirectory(),
					FileUtils.getRelativePath(file, application.getConfig().getClassesDirectory()));
			FileUtils.copyTo(file, workFile);
			
			if(file.getName().endsWith(".class")) {
				// class files will be enhanced and automatically copied over the work directory.
				classFiles.add(file);
			}
		}
	}

	/**
	 * Generates new optimized class files under application's WorkDirectory. 
	 * Those will ultimately be utilized for class load.
	 * 
	 * @param application
	 * @throws Exception
	 */
	public void enhance() throws Exception {
		//
		// first step is to create the ClassLoader for the ClassesDirectory
		//
		ArrayList<URL> urls = new ArrayList<URL>();
		try {
			urls.add(application.getConfig().getClassesDirectory().toURI().toURL());
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		
		File jars[] = application.getConfig().getLibDirectory().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.endsWith(".jar"))
					return true;
				return false;
			}
		});
		for(File jar: jars) {
			try {
				urls.add(jar.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(log.isTraceEnabled())
			log.trace("{} utilizing the following to paths to enhance classes: [{}]", application, urls);
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(urls);
		
		// ClassLoader classLoader = application.getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
				
		// recreating the files but WorkDirectory but only those not recreated by enhanced.
		scanClassesDirectory(application.getConfig().getClassesDirectory());
		
		if(log.isTraceEnabled())
			log.trace("class identified [{}]", classFiles);
		
		for(File file: classFiles) {
			ClassFile classFile = new ClassFile(classLoader, file, application.getConfig().getClassesDirectory());
			try {
				doEnhance(classFile);
			}
			catch(ClassNotFoundException e) {
				log.error("failed to process class file: "+file.getAbsolutePath());
				e.printStackTrace();
				throw new InvocationTargetException(e);
			}
		}
		
		Thread.currentThread().setContextClassLoader(classLoader.getParent());
		
		classLoader = null;
		
		// this shall destroy the ClassesDirectory based ClassLoader
		System.gc();
	}
	
	@Override
	public void finalize() {
		if(log.isTraceEnabled())
			log.trace("finalize()");
	}
}

class ClassFile {
	private Class<?> clazz;
	private ClassLoader classLoader;
	private String classPath;
	private String className;

	public ClassFile(ClassLoader classLoader, File file, File basedir) {
		this.classLoader = classLoader;

		//
		// generating class path
		//
		this.classPath = file.getAbsolutePath();
		int p = classPath.indexOf(basedir.getAbsolutePath());
		if(p != -1) {
			classPath = classPath.substring(p + basedir.getAbsolutePath().length() + File.separator.length());
		}
		// 
		// generating class name
		//
		className = classPath;
		p = className.indexOf(".class");
		if(p != -1)
			className = className.substring(0, p);
		String pattern = Pattern.quote(File.separator);
		className = className.replaceAll(pattern, ".");
		try {
			this.clazz = classLoader.loadClass(className);
		}
		catch(ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getClassName() {
		return className;
	}

	public String getClassPath() {
		return classPath;
	}
	
	public String toString() {
		return classPath;
	}
}

