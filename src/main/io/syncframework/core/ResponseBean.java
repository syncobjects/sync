/*
 * Copyright 2012-2017 SyncObjects Ltda.
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

import java.util.Set;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.MessageContext;
import io.syncframework.api.RequestContext;
import io.syncframework.api.SessionContext;

/**
 * @author dfroz
 */
public interface ResponseBean {
	public ApplicationContext getApplicationContext();
	public String getContentType();
	public Object getParameter(String name) throws Exception;
	public Set<String> getParametersName();
	public CookieContext getCookieContext();
	public ErrorContext getErrorContext();
	public MessageContext getMessageContext();
	public RequestContext getRequestContext();
	public SessionContext getSessionContext();
}