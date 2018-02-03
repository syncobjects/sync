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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Result {
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	public static final String CONTENT_LENGTH_HEADER = "Content-Length";
	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String SET_COOKIE_HEADER = "Set-Cookie";
	private final Map<String,String> headers = new ConcurrentHashMap<String,String>();
	private final Map<String,String> parameters = new ConcurrentHashMap<String,String>();
	private final List<Cookie> cookies = new LinkedList<Cookie>();
	
	public Result setCookie(Cookie cookie) {
		cookies.add(cookie);
		return this;
	}
	
	public List<Cookie> getCookies() {
		return cookies;
	}
	
	public Result setHeader(String header, String value) {
		headers.put(header, value);
		return this;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public Result setParameter(String parameter, String value) {
		parameters.put(parameter, value);
		return this;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
