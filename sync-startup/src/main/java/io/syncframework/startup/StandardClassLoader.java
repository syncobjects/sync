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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * class loader abstraction to URL classLoader.
 * @author dfroz
 */
public class StandardClassLoader extends URLClassLoader {
	private ChildURLClassLoader childClassLoader;

	/**
	 * This class allows me to call findClass on a classloader
	 */
	private static class FindClassClassLoader extends ClassLoader {
		public FindClassClassLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			return super.findClass(name);
		}
	}

	/**
	 * This class delegates (child then parent) for the findClass method for a URLClassLoader.
	 * We need this because findClass is protected in URLClassLoader
	 */
	private static class ChildURLClassLoader extends URLClassLoader {
		private FindClassClassLoader parent;

		public ChildURLClassLoader(URL[] urls, FindClassClassLoader parent) {
			super(urls, null);
			this.parent = parent;
		}

		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				// first try to use the URLClassLoader findClass
				Class<?> c = super.findLoadedClass(name);
				if(c != null)
					return c;
				return super.findClass(name);
			}
			catch( ClassNotFoundException e ) {
				// if that fails, we ask our real parent classloader to load the class (we give up)
				return parent.loadClass(name);
			}
		}
	}

	public StandardClassLoader(List<URL> classpath) {		
		this(classpath, Thread.currentThread().getContextClassLoader());
	}

	public StandardClassLoader(List<URL> classpath, ClassLoader parent) {
		//super(Thread.currentThread().getContextClassLoader());
		super(classpath.toArray(new URL[classpath.size()]));
		URL[] urls = classpath.toArray(new URL[classpath.size()]);
		childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(parent));
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			// first we try to find a class inside the child classloader
			return childClassLoader.findClass(name);
		}
		catch( ClassNotFoundException e ) {
			// didn't find it, try the parent
			return super.loadClass(name, resolve);
		}
	}

	@Override
	public URL getResource(String name) {
		URL url;
		url = childClassLoader.getResource(name);
		if(url == null)
			url = super.getResource(name);
		if (url == null) {
			url = super.findResource(name);
		}
		return url;
	}
}
