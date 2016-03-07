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
package com.syncobjects.as.core;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Represents the interface of @Interceptor internally running at the application server.
 * 
 * @author dfroz
 * 
 */
public interface IInterceptor {
	public Method _asAfter();
	public Method _asBefore();
	public Map<String,Class<?>> _asFields();
	public Map<String,Method> _asGetters();
	public Method _asGettersApplicationContext();
	public Method _asGettersCookieContext();
	public Method _asGettersErrorContext();
	public Method _asGettersMessageContext();
	public Method _asGettersRequestContext();
	public Method _asGettersSessionContext();
	public Map<String,Method> _asSetters();
	public Method _asSettersApplicationContext();
	public Method _asSettersCookieContext();
	public Method _asSettersErrorContext();
	public Method _asSettersMessageContext();
	public Method _asSettersRequestContext();
	public Method _asSettersSessionContext();
}
