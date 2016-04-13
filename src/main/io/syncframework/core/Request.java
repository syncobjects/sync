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

import java.util.List;
import java.util.Map;

import io.syncframework.api.CookieContext;
import io.syncframework.api.FileUpload;
import io.syncframework.api.RequestContext;

/**
 * 
 * @author dfroz
 *
 */
public interface Request extends Recyclable {
	public CookieContext getCookieContext();
	public Map<String, FileUpload> getFiles();
	public List<String> getHeader(String name);
	public Map<String, List<String>> getHeaders();
	public Map<String, List<String>> getParameters();
	public RequestContext getRequestContext();
	public Session getSession();
	public void setSession(Session session);
	public String getUri();
}