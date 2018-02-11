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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.RequestContext;

/**
 * 
 * @author dfroz
 *
 */
public class SessionFactorySecureImpl implements SessionFactory {
	private static final Logger log = LoggerFactory.getLogger(SessionFactorySecureImpl.class);
	private static final Session bogus = new SessionVoid();
	private ApplicationConfig config;
	private final StringBuffer buffer = new StringBuffer();
	private final Map<String,Session> sessions = new ConcurrentHashMap<String, Session>();
	private final Map<String,Integer> remoteAddressCounter = new HashMap<String,Integer>();
	private boolean running;
	private Thread sanitizer;
	
	public SessionFactorySecureImpl() {
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
		//
		// session may be null under the scenarios:
		// 1. first user request
		// 2. session has expired
		// 3. ID is fake / invalid...
		//
		// All of them new session must be created...
		//
		if(session == null) {
			String remoteAddress = (String)request.getRequestContext().get(RequestContext.REMOTE_ADDRESS);
			if(remoteAddress == null)
				throw new RuntimeException("remote address is null");
			//
			// protect from memory exhaustion DDoS
			//
			Integer i = remoteAddressCounter.get(remoteAddress);
			if(i == null)
				i = 0;
			i++;
			
			if(i < 100) {
				// create new session
				session = create();
				session.setRemoteAddress(remoteAddress);
				session.setRecent(true);
				remoteAddressCounter.put(remoteAddress, i);
				if(log.isTraceEnabled())
					log.trace("session created: {}", session);
			}
			else {
				session = bogus;
				session.setRecent(false);
				// since sending bogus session, then no need to increment the sessions per address
				remoteAddressCounter.put(remoteAddress, --i);
				if(log.isWarnEnabled())
					log.warn("session max counter exceeded the value {} per remote address {}; assigned bogus session", i, remoteAddress);
			}
			session.setAccessTime(System.currentTimeMillis());
			return session;
		}
		
		// session returned from the sessions are no longer recent
		
		//
		// check for SSID spoofed requests...
		// session requests must come from the same IP in order to be valid...
		//
		String remoteAddress = (String)request.getRequestContext().get(RequestContext.REMOTE_ADDRESS);
		if(remoteAddress == null)
			throw new RuntimeException("remote address is null");
		if(!remoteAddress.equals(session.getRemoteAddress()))
			throw new RuntimeException("session hijacking detected!");
		
		// update the session access time
		session.setRecent(false);
		session.setAccessTime(System.currentTimeMillis());
		if(log.isTraceEnabled())
			log.trace("session identified: {}", session);
		
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
						
						// check for bogus session
						Integer i = remoteAddressCounter.get(session.getRemoteAddress());
						if(i != null) {
							remoteAddressCounter.put(session.getRemoteAddress(), --i);
							if(i <= 0) {
								if(log.isTraceEnabled())
									log.trace("removed address counter for remote address {}", session.getRemoteAddress());
								remoteAddressCounter.remove(session.getRemoteAddress());
							}
						}
					}
				}
			}
			if(log.isTraceEnabled()) {
				log.trace("session Sanitizer thread has stopped");
			}
		}
	}
	
	public String toString() {
		return "secure";
	}
}


