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
package com.syncobjects.as.optimizer;

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

import com.syncobjects.as.core.Application;
import com.syncobjects.as.core.ApplicationClassLoader;
import com.syncobjects.as.util.FileUtils;

/**
 * This class is responsible for compilation of the classes over the ClassesDirectory, 
 * creating a optimized version under the application's WorkDirectory 
 * 
 * @author dfroz
 * 
 */
public class Optimizer {
	private static final Logger log = LoggerFactory.getLogger(Optimizer.class);
	private final List<File> classFiles = new ArrayList<File>();
	private Application application;
	
	public Optimizer(Application application) {
		this.application = application;
	}

	/**
	 * This method creates optimized classes: \@Controllers, \@Interceptors and \@Initializers.
	 * Loads \@Converters into the Factory...
	 * 
	 * The generated class files will implement OController, OInterceptor, OInitializer interfaces.
	 * No Reflection API will be utilized during Runtime. Therefore we shall expect performance improvement optimizing this classes.
	 * 
	 * Files are not loaded directly into memory to facilitate low memory foot-print; avoiding two different versions
	 * of the same class into memory.
	 * 
	 * @param classFile represents the class file
	 * @throws Exception
	 */
	private void doOptimize(ClassFile classFile) throws Exception {
		Class<?> clazz = classFile.getClazz();
		
		ClassOptimizer optimizer = new ClassOptimizer();
		byte b[] = optimizer.optimize(clazz);
		if(b == null)
			return;
		
		File file = new File(application.getConfig().getWorkDirectory(), classFile.getClassPath());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(b);
		try { fos.close(); } catch(Exception ignore) {}
		
		if(log.isTraceEnabled())
			log.trace("{} created optimized version of class {}", application, clazz.getName());
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
	public void optimize() throws Exception {
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
				doOptimize(classFile);
			}
			catch(ClassNotFoundException e) {
				log.error("failed to process class file: "+file.getAbsolutePath(), e);
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

