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

import java.util.Map;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.MessageContext;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.SessionContext;

/**
 * Optimized Controller Interface
 * 
 * This interfaces defines the \@Controller methods after the optimization is performed. No Java Reflection
 * is used at runtime. In the initialization time SAS will optimize \@Controller classes and save them to
 * \@Application work directory.
 * 
 * @author dfroz
 */
public interface OController {
	/**
	 * \@Controller URL pattern
	 */
	public String _asUrl();
	/**
	 * @return Map with \@Parameters name as key and Class<?> as value
	 */
	public Map<String, Class<?>> _asParameters();
	/**
	 * \@Parameter getter representation.
	 * @return object.
	 */
	public Object _asParameter(String name);
	/**
	 * \@Parameter setter representation.
	 */
	public void _asParameter(String name, Object value);
	/**
	 * @return \@Converter class defined in the \@Parameter annotation. Returns null if there is no converter
	 */
	public Class<?> _asParameterConverter(String name);
	/**
	 * \@Context setters.
	 */
	public void _asApplicationContext(ApplicationContext application);
	public void _asCookieContext(CookieContext cookies);
	public void _asErrorContext(ErrorContext errors);
	public void _asMessageContext(MessageContext messages);
	public void _asRequestContext(RequestContext request);
	public void _asSessionContext(SessionContext session);
	/**
	 * execute \@Action specified by the name
	 */
	public Result _asAction(String name);
	/**
	 * Utilized to identify if the action exists (declared)
	 */
	public boolean _asActionIsDefined(String name);
	/**
	 * Utilized to return the interceptors
	 */
	public Class<?>[] _asActionInterceptors(String name);
	/**
	 * Shortcut to Content-Type header... 
	 */
	public String _asActionType(String name);
}
