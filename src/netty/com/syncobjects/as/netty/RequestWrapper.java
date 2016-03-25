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
package com.syncobjects.as.netty;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.FileUpload;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.core.Request;
import com.syncobjects.as.core.Session;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

/**
 * 
 * @author dfroz
 *
 */
public class RequestWrapper implements Request {
	private static final Logger log = LoggerFactory.getLogger(RequestWrapper.class);
	private final CookieContext cookieContext = new CookieContext();
	private final Map<String, List<String>> headers = new HashMap<String, List<String>>();
	private final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
	private final Map<String, FileUpload> files = new HashMap<String, FileUpload>();
	private HttpRequest request;
	private final RequestContext requestContext = new RequestContext();
	private Session session;

	@Override
	public CookieContext getCookieContext() {
		return cookieContext;
	}

	@Override
	public Map<String, FileUpload> getFiles() {
		return files;
	}

	@Override
	public List<String> getHeader(String name) {
		if(headers == null)
			return null;
		return headers.get(name);
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	@Override
	public Map<String, List<String>> getParameters() {
		return parameters;
	}

	@Override
	public RequestContext getRequestContext() {
		return requestContext;
	}
	
	public void setRequest(HttpRequest request) {
		this.request = request;
		this.session = null;
		
		// setting headers...
		//
		for (Entry<String, String> entry : request.headers()) {
			String name = entry.getKey().toString();
			List<String> values = headers.get(name);
			if(values == null) {
				values = new LinkedList<String>();
			}
			values.add(entry.getValue().toString());
			headers.put(name, values);
        }
		
		// setting cookies...
		//
		Set<Cookie> cookies;
        String value = this.request.headers().getAsString(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
        	ServerCookieDecoder decoder = ServerCookieDecoder.STRICT;
            cookies = decoder.decode(value);
        }
        if(log.isTraceEnabled())
        	log.trace("cookies: {}", cookies);
        
        for (Cookie cookie : cookies) {
            cookieContext.put(cookie.name(), cookie.value());
        }
        
        // parameters from the URL
        //
        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Entry<String, List<String>> attr: uriAttributes.entrySet()) {
        	parameters.put(attr.getKey(), attr.getValue());
        }
	}

	@Override
	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public String getUri() {
		return request.uri();
	}
	
	@Override
	public void recycle() {
		cookieContext.clear();
		files.clear();
		headers.clear();
		parameters.clear();
		requestContext.clear();
	}
}
