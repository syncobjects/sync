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
package io.syncframework.startup;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Properties;

import io.syncframework.Globals;


/**
 * Bootstrap loader for the application server. This class is in charge of setting up the base directory and load the Server implementation
 * according to the default or predefined server (-Dsync.server=<class>)
 * 
 * @author dfroz
 */
public class Bootstrap {
	private static final String SERVER_DEFAULT_CHARSET_KEY = "default.charset";
	private static final String SERVER_PROPERTIES_FILE = "server.properties";
	private static final String defaultServerFactoryClassName = "io.syncframework.netty.ServerFactory";
	private static final String serverInterfaceName = "io.syncframework.core.Server";
	private static String serverFactoryClassName = null;
	private static String defaultFileEncoding = "UTF-8";
	
	static {
		String s = System.getProperty(Globals.SYNC_SERVER);
		if(s == null)
			s = defaultServerFactoryClassName;
		serverFactoryClassName = s;
		
		String basedir = System.getProperty(Globals.SYNC_BASE);
		if(basedir == null) {
			String userdir = System.getProperty("user.dir");
			basedir = userdir;
		}
		
		File dir = new File(basedir);
		if(!dir.isDirectory())
			throw new RuntimeException(dir.getAbsolutePath()+" is not a valid directory");
		
		System.setProperty(Globals.SYNC_BASE, dir.getAbsolutePath());
	}

	public void start() throws Exception {
		String basedir = System.getProperty(Globals.SYNC_BASE);
		
		System.out.println("Starting SYNC|Framework");
		//
		// setting @Server charset
		//
		String fileEncoding = defaultFileEncoding;
		File spf = new File(basedir, SERVER_PROPERTIES_FILE);
		if(!spf.isFile() || !spf.canRead()) {
			throw new RuntimeException("Failed to locate/open the "+spf.getAbsolutePath()+" file");
		}
		FileInputStream fis = new FileInputStream(spf);
		Properties properties = new Properties();
		properties.load(fis);
		if(properties.getProperty(SERVER_DEFAULT_CHARSET_KEY) != null)
			fileEncoding = properties.getProperty(SERVER_DEFAULT_CHARSET_KEY).trim();
		System.setProperty("file.encoding", fileEncoding);
		// tricky code to make the JVM to make the Charset.defaultCharset() to take the new 
		// System.getProperty("file.encoding") value
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null, null);
		
		// lib folder
		File libraryDirectory = new File(basedir, "lib");
		if(!libraryDirectory.exists())
			throw new RuntimeException(libraryDirectory.getAbsolutePath()+" is not a valid directory");
		
		ClassLoader parent = this.getClass().getClassLoader();
		
		ClassLoader commonClassLoader = ClassLoaderFactory.createClassLoader(new File[] { libraryDirectory }, parent);		
		ClassLoader serverClassLoader = ClassLoaderFactory.createClassLoader(new File[0], commonClassLoader);
		
		Thread.currentThread().setContextClassLoader(serverClassLoader);
		
		Method method = null;
		Class<?> serverFactoryClass = Class.forName(serverFactoryClassName, false, serverClassLoader);
		method = serverFactoryClass.getMethod("getServer", new Class<?>[0]);
		Object serverObject = method.invoke(null, new Object[0]);
		
		Class<?> serverClass = Class.forName(serverInterfaceName, false, serverClassLoader);
		method = serverClass.getDeclaredMethod("init", new Class<?>[0]);
		method.invoke(serverObject, new Object[0]);
	}

	public static void main(String args[]) {
		Bootstrap daemon = new Bootstrap();
		try {
			daemon.start();
		}
		catch(Exception e) {
			System.err.println("failed to start SAS");
			e.printStackTrace();
		}
	}
}