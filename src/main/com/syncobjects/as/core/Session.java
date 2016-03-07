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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.SessionContext;

/**
 * 
 * @author dfroz
 *
 */
public class Session implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(Session.class);
	private static final long serialVersionUID = 937117557602804062L;
	
	/**
	 * time when the last access was done (in milliseconds)
	 */
	private long accessTime; // in milliseconds
	/**
	 * time when session was created (in milliseconds)
	 */
	private long creationTime;
	private ErrorContext errorContext = new ErrorContext();
	private String id;
	private String idKey;
	/**
	 * identifies if recent created/reused session
	 */
	private boolean recent;
	private SessionContext sessionContext = new SessionContext();
	
	// application.properties
	// session.expirationtime = 10
	
	public Session() {
		this(null, null);
	}
	public Session(String id, String idKey) {
		this.accessTime = this.creationTime = System.currentTimeMillis();
		this.id = id;
		this.idKey = idKey;
		this.recent = true;
	}
	public long getAccessTime() {
		return accessTime;
	}
	public void setAccessTime(long accessTime) {
		this.accessTime = accessTime;
	}
	public long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	public ErrorContext getErrorContext() {
		return errorContext;
	}
	public void setErrorContext(ErrorContext errorContext) {
		this.errorContext = errorContext;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIdKey() {
		return idKey;
	}
	public void setIdKey(String idKey) {
		this.idKey = idKey;
	}
	public boolean isRecent() {
		return recent;
	}
	public void setRecent(boolean recent) {
		this.recent = recent;
	}
	public SessionContext getSessionContext() {
		return sessionContext;
	}
	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}
	public String toString() {
		return "Session-"+id;
	}
	public void recycle() {
		accessTime = creationTime = System.currentTimeMillis();
		id = null;
		errorContext.clear();
		recent = true;
		sessionContext.clear();
	}
	public void finalize() {
		if(log.isTraceEnabled()) {
			log.trace("session finalized");
		}
	}
}