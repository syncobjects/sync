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
package com.syncobjects.as.responder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.Cookie;
import com.syncobjects.as.api.RedirectResult;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.core.Response;
import com.syncobjects.as.core.ResponseBean;
import com.syncobjects.as.core.Session;

/**
 * 
 * @author dfroz
 *
 */
public class RedirectResponder implements Responder {
	private static final Logger log = LoggerFactory.getLogger(RedirectResponder.class);

	public void destroy() {
		// do nothing
	}
	public void init(ApplicationContext context) {
		// do nothing
	}
	
	public void respond(Response response, ResponseBean bean, Result result) throws Exception {
		if(result == null || !(result instanceof RedirectResult))
			throw new IllegalArgumentException("result is not a RedirectResult instance");
		if(response == null)
			throw new IllegalArgumentException("response is null");
		
		RedirectResult rr = (RedirectResult)result;
		switch(rr.getCode()) {
		case TEMPORARY:
			response.setCode(Response.Code.TEMPORARY_REDIRECT);
			break;
		case PERMANENT:
			response.setCode(Response.Code.PERMANENT_REDIRECT);
			break;
		}
		
		StringBuilder url = new StringBuilder();
		url.append(rr.getUrl());
		if(rr.getParameters().size() > 0) {
			int i=0;
			for(String param: rr.getParameters().keySet()) {
				if(i == 0)
					url.append("?");
				else
					url.append("&");
				url.append(param).append("=").append(rr.getParameters().get(param));
				i++;
			}
		}
		
		response.getHeaders().put("Location", url.toString());
		for(String key: rr.getHeaders().keySet()) {
			response.getHeaders().put(key, rr.getHeaders().get(key));
		}
		response.getHeaders().put("Content-Length", "0");
		
		//
		// Set cookies
		//
		for(Cookie cookie: rr.getCookies()) {
			response.getHeaders().put(Result.SET_COOKIE_HEADER, cookie.toString());
		}
		
		Session session = response.getSession();
		if(session.isRecent()) {
			if(log.isDebugEnabled())
				log.debug("setting session cookie header; response session: {}", session);
			Cookie cookie = new Cookie(session.getIdKey(), session.getId(), null, "/", 604800L, null, null);
			response.getHeaders().put("Set-Cookie", cookie.toString());
		}
		
		return;
	}
}
