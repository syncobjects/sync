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
import java.util.Locale;

/**
 * 
 * @author dfroz
 *
 */
public class ApplicationConfig extends Config {
	private static final long serialVersionUID = -4106496145463699646L;
	public static final String CLASSESDIR_KEY = "application.classes";
	public static final String CONFIG_FILENAME = "application.properties";
	public static final String CONFIG_DEV_FILENAME = "application-dev.properties";
	public static final String CHARSET_KEY = "application.charset";
	public static final String DOMAINS_KEY = "application.domains";
	public static final String LIBDIR_KEY = "application.lib";
	public static final String LOCALE_KEY = "application.locale";
	public static final String SESSION_EXPIRE_KEY = "application.session.expire";
	public static final String SESSION_FACTORY_KEY = "application.session.factory";
	public static final String SESSION_IDKEY_KEY = "application.session.key";
	public static final String SESSION_POOL_SIZE_KEY = "application.session.poolsize";
	public static final String TEMPLATE_CACHE = "application.template.cache";
	public static final String TEMPLATE_VERSION = "application.template.version";
	
	private File baseDirectory;
	private File classesDirectory;
	private File libDirectory;
	private File privateDirectory;
	private File publicDirectory;
	private String charset;
	private Locale locale;
	private long sessionExpire;
	private String sessionFactory;
	private String sessionIdKey;
	private int sessionPoolSize;
	private Boolean templateCache;
	private String templateVersion;
	private File tmpDirectory;
	private File workDirectory;

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(File homeDirectory) {
		this.baseDirectory = homeDirectory;
	}
	
	public File getClassesDirectory() {
		return classesDirectory;
	}

	public void setClassesDirectory(File classesDirectory) {
		this.classesDirectory = classesDirectory;
	}

	public File getLibDirectory() {
		return libDirectory;
	}

	public void setLibDirectory(File libDirectory) {
		this.libDirectory = libDirectory;
	}

	public File getPrivateDirectory() {
		return privateDirectory;
	}

	public void setPrivateDirectory(File privateDirectory) {
		this.privateDirectory = privateDirectory;
	}

	public File getPublicDirectory() {
		return publicDirectory;
	}

	public void setPublicDirectory(File publicDirectory) {
		this.publicDirectory = publicDirectory;
	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public long getSessionExpire() {
		return sessionExpire;
	}

	public void setSessionExpire(long sessionExpire) {
		this.sessionExpire = sessionExpire;
	}

	public String getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(String sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public String getSessionIdKey() {
		return sessionIdKey;
	}

	public void setSessionIdKey(String sessionIdKey) {
		this.sessionIdKey = sessionIdKey;
	}

	public int getSessionPoolSize() {
		return sessionPoolSize;
	}

	public void setSessionPoolSize(int sessionPoolSize) {
		this.sessionPoolSize = sessionPoolSize;
	}

	public Boolean getTemplateCache() {
		return templateCache;
	}

	public void setTemplateCache(Boolean cache) {
		this.templateCache = cache;
	}

	public String getTemplateVersion() {
		return templateVersion;
	}

	public void setTemplateVersion(String templateVersion) {
		this.templateVersion = templateVersion;
	}

	public File getTmpDirectory() {
		return tmpDirectory;
	}

	public void setTmpDirectory(File tmpDirectory) {
		this.tmpDirectory = tmpDirectory;
	}

	public File getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(File workDirectory) {
		this.workDirectory = workDirectory;
	}
}