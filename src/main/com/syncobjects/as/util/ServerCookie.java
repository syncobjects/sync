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
package com.syncobjects.as.util;

/**
 * 
 * @author dfroz
 *
 */
public class ServerCookie {
	private String name;
	private String value;
	private String domain;
	private String path;
	private String comment;
	private int version=1;
	private boolean secure = false;
	/**
	 * max age defined in seconds
	 */
	private int maxAge=-1;
	
	public ServerCookie() {
		super();
	}
	public ServerCookie(String name, String value) {
		this(name, value, null, "/", 0);
	}
	public ServerCookie(String name, String value, String path) {
		this(name, value, null, path, 0);
	}
	public ServerCookie(String name, String value, String domain, String path, int maxAge) {
		this.name = name;
		this.domain = domain;
		this.path = path;
		this.value = value;
		this.maxAge = maxAge;
		this.comment = null;
		this.version = 1;
		this.secure = false;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public long getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append(name).append("=").append(value);
		if(comment != null) {
			sb.append("; Comment=").append(comment);
		}
		if(domain != null) {
			sb.append("; Domain=").append(domain);
		}
		if(path != null) {
			sb.append("; Path=").append(path);
		}
		if(maxAge >= 0) {;
			sb.append("; Max-Age=").append(maxAge);
		}
		if(secure) {
			sb.append("; Secure");
		}
		sb.append("; Version=").append(version);
		return sb.toString();
	}
}