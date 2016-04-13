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
package io.syncframework.api;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * ApplicationContextMock facilitates your Test driven development. Mocks ApplicationContext loading the application.properties
 * file and making available to your Unit Tests. Nothing really special, just a useful shortcut code.
 * 
 * @author dfroz
 */
public class ApplicationContextMock extends ApplicationContext {
	private static final long serialVersionUID = 226733308235387980L;
	public ApplicationContextMock() {
		this(new File("."));
	}
	public ApplicationContextMock(String homedir) {
		this(new File(homedir));
	}
	public ApplicationContextMock(File homedir) {
		put(ApplicationContext.HOME, homedir.getAbsolutePath());
		put(ApplicationContext.LOCALE, Locale.getDefault());
		
		File propertiesFile = new File(homedir.getAbsolutePath()+ File.separator + "application.properties");
		if(!propertiesFile.isFile())
			throw new IllegalArgumentException("application.properties not found: "+propertiesFile.getAbsolutePath());
		
		try {
			FileInputStream is = new FileInputStream(propertiesFile);
			Properties props = new Properties();
			props.load(is);
			put(ApplicationContext.PROPERTIES, props);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
