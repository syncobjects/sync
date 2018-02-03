/*
 * Copyright (c) 2016-2017. SyncObjects Ltda.
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
 * Use CookieContext to retrieve information from the request...
 * @author dfroz
 */
public class Cookie {
	private String name;
	private String value;
	private String domain;
	private String path;
	private Long maxAge;
	private Boolean secure;
	private Boolean httpOnly;
	
	public Cookie() {
		this(null,null,null,null,null,null,null);
	}
	public Cookie(String name, String value) {
		this(name, value, null, null, null, null, null);
	}
	public Cookie(String name, String value, String domain, String path, Long maxAge, Boolean secure, Boolean httpOnly) {
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.path = path;
		this.maxAge = maxAge;
		this.secure = secure;
		this.httpOnly = httpOnly;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Long getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(Long maxAge) {
		this.maxAge = maxAge;
	}
	public Boolean getSecure() {
		return secure;
	}
	public void setSecure(Boolean secure) {
		this.secure = secure;
	}
	public Boolean getHttpOnly() {
		return httpOnly;
	}
	public void setHttpOnly(Boolean httpOnly) {
		this.httpOnly = httpOnly;
	}
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(name).append('=').append(value);
	    if (domain != null) {
	        buf.append("; Domain=")
	           .append(domain);
	    }
	    if (path != null) {
	        buf.append("; Path=")
	           .append(path);
	    }
	    if (maxAge != null && maxAge >= 0) {
	        buf.append("; Max-Age=").append(maxAge);
	    }
	    if (secure != null && secure) {
	        buf.append("; Secure");
	    }
	    if (httpOnly != null && httpOnly) {
	        buf.append("; HttpOnly");
	    }
	    return buf.toString();
	}
}
