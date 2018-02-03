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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.optimizer.OController;
import io.syncframework.optimizer.OInitializer;
import io.syncframework.optimizer.OInterceptor;

/**
 * Responsible for load the application's classes and libraries.
 * 
 * @author dfroz
 */
public class Loader {
	private static final Logger log = LoggerFactory.getLogger(Loader.class);

	private Application application;
	
	private File workDirectory;
	private File libDirectory;
	
	private List<File> classFiles = new ArrayList<File>();
	private ClassLoader classLoader;
	private List<Class<?>> controllers = new ArrayList<Class<?>>();
	private List<Class<?>> initializers = new ArrayList<Class<?>>();
	private List<Class<?>> interceptors = new ArrayList<Class<?>>();
	
	// private ClassLoader parentClassLoader;
	private Map<URL,URL> urls = new HashMap<URL,URL>();

	public Loader(Application application) {
		this.application = application;
		this.workDirectory = application.getConfig().getWorkDirectory();
		this.libDirectory = application.getConfig().getLibDirectory();
	}

	/* private section */

	private void inspectClass(String name) throws Exception {
		Class<?> clazz = classLoader.loadClass(name);
		for(Class<?> c: clazz.getInterfaces()) {
			if(c.equals(OController.class)) {
				controllers.add(clazz);
				if(log.isTraceEnabled())
					log.trace("@Controller {} loaded", clazz.getName());
				return;
			}
			if(c.equals(OInterceptor.class)) {
				if(log.isTraceEnabled())
					log.trace("@Interceptor {} loaded", clazz.getName());
				interceptors.add(clazz);
				return;
			}
			if(c.equals(OInitializer.class)) {
				if(log.isTraceEnabled())
					log.trace("@Initializer {} loaded",clazz.getName());
				initializers.add(clazz);
				return;
			}
		}
	}

	private String normalizeClassName(String path) {
		//
		// if class path is prepended with the absolute classes path, then remove it
		//
		int p = path.indexOf(workDirectory.getAbsolutePath());
		if(p != -1)
			path = path.substring(p + workDirectory.getAbsolutePath().length() + File.separator.length());
		// stripping off ".class" from className
		String className = path;
		p = className.lastIndexOf('.');
		if(p != -1)
			className = className.substring(0, p);
		String pattern = Pattern.quote(File.separator);
		return className.replaceAll(pattern, ".");
	}

	private void scanClassesDirectory(File dir) throws Exception {
		for(File file: dir.listFiles()) {
			if(file.getName().startsWith("."))
				continue;
			if(file.isDirectory()) {
				scanClassesDirectory(file);
				continue;
			}
			if(file.getName().endsWith(".class")) {
				classFiles.add(file);
				continue;
			}
		}
	}
	
	private void scanLibDirectory(File dir) throws Exception {
		for(File file: dir.listFiles()) {
			if(file.getName().startsWith("."))
				continue;
			if(file.isDirectory()) {
				scanLibDirectory(file);
				continue;
			}
			if(file.getName().endsWith(".jar")) {
				// only adds to the URL the .jar files.
				URL url = file.toURI().toURL();
				urls.put(url, url);
				continue;
			}
		}
	}

	/* public section */

	public void clear() {
		urls.clear();
		classFiles.clear();
		controllers.clear();
		initializers.clear();
		interceptors.clear();
	}
	
	public void load() throws Exception {
		clear();

		//
		// add <domain>/classes directory to the ClassLoader's path
		//
		if(workDirectory.isDirectory()) {
			URL url = workDirectory.toURI().toURL();
			urls.put(url, url);
			scanClassesDirectory(workDirectory);
		}
		if(libDirectory.isDirectory()) {
			URL url = libDirectory.toURI().toURL();
			urls.put(url, url);
			scanLibDirectory(libDirectory);
		}
		
		if(log.isTraceEnabled())
			log.trace("ClassLoader: "+urls.keySet());
		
		//
		// create a ClassLoader from both classes and lib directory
		//
		// classLoader = new ApplicationClassLoader(urls.keySet().toArray(new URL[0]), parentClassLoader);
		classLoader = new ApplicationClassLoader(new ArrayList<URL>(urls.keySet()), application.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);

		if(!workDirectory.isDirectory()) {
			if(log.isTraceEnabled())
				log.trace("Loader: Application classes not located since classes directory does not exist");
			return;
		}

		for(File file: classFiles) {
			try {
				inspectClass(normalizeClassName(file.getAbsolutePath()));
			}
			catch(ClassNotFoundException e) {
				log.error("failed to process class file: "+file.getAbsolutePath());
				e.printStackTrace();
				throw new InvocationTargetException(e);
			}
		}
	}
	
	/* bean methods */

	public ClassLoader getClassLoader() {
		if(classLoader == null)
			throw new RuntimeException("Loader not initialized (loader())");
		return classLoader;
	}

	public List<Class<?>> getControllers() {
		if(classLoader == null)
			throw new RuntimeException("Loader not initialized (loader())");
		return controllers;
	}

	public List<Class<?>> getInitializers() {
		if(classLoader == null)
			throw new RuntimeException("Loader not initialized (loader())");
		return initializers;
	}

	public List<Class<?>> getInterceptors() {
		if(classLoader == null)
			throw new RuntimeException("Loader not initialized (loader())");
		return interceptors;
	}
	
	public void finalize() {
		if(log.isTraceEnabled())
			log.trace(application+" ApplicationLoader finalized");
	}
}
