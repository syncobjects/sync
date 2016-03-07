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

import java.util.Set;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.SessionContext;

/**
 * 
 * @author dfroz
 *
 */
public interface ResponseBean extends Wrapper {
	public ApplicationContext getApplicationContext();
	public Object getField(String name) throws Exception;
	public Set<String> getFields();
	public CookieContext getCookieContext();
	public ErrorContext getErrorContext();
	public MessageContext getMessageContext();
	public RequestContext getRequestContext();
	public SessionContext getSessionContext();
}