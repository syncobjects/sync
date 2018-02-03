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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.core.Application;
import io.syncframework.core.ApplicationClassLoader;
import io.syncframework.util.FileUtils;

/**
 * This class is responsible for compilation of the classes over the ClassesDirectory, 
 * creating a optimized version under the application's WorkDirectory 
 * 
 * @author dfroz
 * 
 */
public class Optimizer {
	private static final Logger log = LoggerFactory.getLogger(Optimizer.class);
	private final List<ClassFile> classFiles = new LinkedList<ClassFile>();
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
		ClassOptimizer optimizer = new ClassOptimizer();
		byte b[] = optimizer.optimize(classFile.getClazz());
		if(b == null)
			return;
		
		File file = new File(application.getConfig().getWorkDirectory(), classFile.getRelativePath());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(b);
		try { fos.close(); } catch(Exception ignore) {}
		
		if(log.isTraceEnabled())
			log.trace("{} created optimized version of class {}", application, classFile.getClazz().getName());
	}

	private void scanClassesDirectory(File file) throws Exception {
		for(File f: file.listFiles()) {
			if(f.getName().equals(".") || f.getName().equals(".."))
				continue;
			if(f.isDirectory()) {
				scanClassesDirectory(f);
				continue;
			}
			
			String relativePath = FileUtils.getRelativePath(f, application.getConfig().getClassesDirectory());
			if(f.getName().endsWith(".class")) {
				String clazzName = relativePath;
				int p = clazzName.indexOf(".class");
				if(p != -1) {
					clazzName = clazzName.substring(0, p);
				}
				String pattern = Pattern.quote(File.separator);
				clazzName = clazzName.replaceAll(pattern, ".");
				
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				Class<?> clazz = cl.loadClass(clazzName);
				classFiles.add(new ClassFile(clazz, clazzName, relativePath));
			}
			
			// copy file to the work directory
			File workFile = new File(application.getConfig().getWorkDirectory(), relativePath);
			FileUtils.copyTo(f, workFile);
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
		
		// recreating the WorkDirectory
		FileUtils.delete(application.getConfig().getWorkDirectory());
		
		//
		// first step is to create the ClassLoader from the WorkDirectory
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
		
		ApplicationClassLoader classLoader = new ApplicationClassLoader(urls, application.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
		
		if(log.isTraceEnabled())
			log.trace("{} optimizing classes with ClassLoader: [{}]", application, classLoader);
		
		// scan classes from the classes/
		scanClassesDirectory(application.getConfig().getClassesDirectory());
		
		for(ClassFile file: classFiles) {
			try {
				doOptimize(file);
			}
			catch(ClassNotFoundException e) {
				log.error("failed to process class file: {} ", file, e);
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
	private String clazzName;
	private String relativePath;
	
	public ClassFile(Class<?> clazz, String clazzName, String relativePath) {
		this.clazz = clazz;
		this.clazzName = clazzName;
		this.relativePath = relativePath;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getClazzName() {
		return clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
}
