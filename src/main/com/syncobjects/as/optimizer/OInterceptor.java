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

import java.util.Map;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.SessionContext;

/**
 * Represents the interface of @Interceptor internally running at the application server.
 * 
 * @author dfroz
 * 
 */
public interface OInterceptor {
	public Result _asAfter();
	public Result _asBefore();
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
}
