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
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dfroz
 *
 */
public class SessionFactoryDefaultImpl implements SessionFactory {
	private static final Logger log = LoggerFactory.getLogger(SessionFactoryDefaultImpl.class);
	private ApplicationConfig config;
	private static final StringBuffer buffer = new StringBuffer();
	private static final Map<String,Session> sessions = new ConcurrentHashMap<String, Session>();
	private boolean running;
	private Thread sanitizer;
	
	public SessionFactoryDefaultImpl() {
		running = true;
		sanitizer = null;
	}

	private synchronized Session create() {
		String id = null;
		while(true) {
			buffer.setLength(0);
			for(int i=0; i < 2; i++) {
				long l = (long)((System.nanoTime() & 0xffff) << 40) |
						((System.nanoTime() & 0xffff) << 32) |
						((System.nanoTime() & 0xffff) << 16) ^ 
						((System.nanoTime() & 0xffff) << 8) |
						((System.nanoTime() & 0xffff));
				buffer.append(Long.toHexString(l));
			}
			id = buffer.toString();
			if(!sessions.containsKey(id))
				break;
			if(log.isTraceEnabled())
				log.trace("id ["+id+"] already exists, generating new id");
		} //!while(true)

		Session session = new Session();
		session.setId(id);
		session.setIdKey(config.getSessionIdKey());
		sessions.put(id, session);
		return session;
	}

	/**
	 * Locates the existing session related to the client. In case that none is found, generate new one.
	 */
	public Session find(Request request) {		
		String id = request.getCookieContext().get(config.getSessionIdKey());
		if(id == null) {
			// try request parameter
			List<String> values = request.getParameters().get(config.getSessionIdKey());
			if(values != null && values.size() == 1)
				id = values.get(0);
		}
		Session session = null;
		if(id != null) {
			session = sessions.get(id);
		}
		// session may be null if the existing the session has expired or ID is invalid
		if(session == null) {
			// create new session
			session = create();
			session.setRecent(true);
			if(log.isTraceEnabled())
				log.trace("session created: {}", session);
		}
		else {
			// session returned from the sessions are no longer recent
			session.setRecent(false);
			if(log.isTraceEnabled())
				log.trace("session identified: {}", session);
		}
		session.setAccessTime(System.currentTimeMillis());
		return session;
	}

	public void start(ApplicationConfig config) {
		if(config == null)
			throw new IllegalArgumentException("config");
		this.config = config;
		running = true;
		sanitizer = new Thread(new SessionSanitizer(), "session-sanitizer");
		sanitizer.setDaemon(true);
		sanitizer.start();
	}

	public void stop() {
		running = false;
		sanitizer = null;
	}

	private class SessionSanitizer implements Runnable {
		private long timeout = 1000L;

		public void run() {
			while(running) {
				try { Thread.sleep(timeout); } catch (InterruptedException ignore) {}
				for(String id: sessions.keySet()) {
					Session session = sessions.get(id);
					long now = System.currentTimeMillis();
					long elapsed = now - session.getAccessTime();
					// if(log.isTraceEnabled())
					//	log.trace("checking session "+session+",  "+elapsed+" >= "+config.getSessionExpire());
					if(elapsed >= config.getSessionExpire()) {
						// session has expired
						if(log.isTraceEnabled())
							log.trace("{} has expired", session);
						sessions.remove(id);
					}
				}
			}
			if(log.isTraceEnabled()) {
				log.trace("session Sanitizer thread has stopped");
			}
		}
	}
	
	public String toString() {
		return "default";
	}
}
