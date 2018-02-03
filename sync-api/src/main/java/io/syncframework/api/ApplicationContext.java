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

/**
 * <p>This class is the Context map created during application's initialization time.
 * You shall utilize it to add your application's resource objects among others.</p>
 * <p>
 * This ApplicationContext interface also defines some of the key Properties utilized along your application.<br/>
 * Those are:<br/>
 * <ul>
 * <li>HOME - String of application's absolute path home directory.</li>
 * <li>LOCALE - Application's default locale. See Server configuration for more detailed information.</li>
 * <li>PROPERTIES - ApplicationConfig map. It contains all keys and values declared on the application.properties file.</li>
 * </ul>
 * </p>
 * 
 * @author dfroz
 */
public class ApplicationContext extends Context<String,Object> {
	private static final long serialVersionUID = 78591761595270365L;
	public static final String HOME = "com.syncobjects.as.api.ApplicationContext.HOME";
	public static final String LOCALE = "com.syncobjects.as.api.ApplicationContext.LOCALE";
	public static final String PRIVATE_FOLDER = "com.syncobjects.as.api.ApplicationContext.PRIVATE_FOLDER";
	public static final String PROPERTIES = "com.syncobjects.as.api.ApplicationContext.PROPERTIES";
	public static final String PUBLIC_FOLDER = "com.syncobjects.as.api.ApplicationContext.PUBLIC_FOLDER";
}
