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
package io.syncframework.i18n;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.MessageContext;
import io.syncframework.api.SessionContext;

/**
 * 
 * @author dfroz
 *
 */
public class MessageContextImpl implements MessageContext {
	private static final Logger log = LoggerFactory.getLogger(MessageContextImpl.class);
	private MessageFactory messages;
	private ApplicationContext application;
	private SessionContext session;
	
	public MessageContextImpl(MessageFactory messages, ApplicationContext application, SessionContext session) {
		this.application = application;
		this.messages = messages;
		this.session = session;
	}
	
	public String get(String key) {
		return get(null, key, new Object[0]);
	}

	public String get(String key, Object ... args) {
		return get(null, key, args);
	}

	public String get(Locale locale, String key) {
		return get(locale, key, new Object[0]);
	}

	public String get(Locale locale, String key, Object... args) {
		if(locale == null)
			locale = (Locale)session.get(SessionContext.LOCALE);
		if(locale == null)
			locale = (Locale)application.get(ApplicationContext.LOCALE);
		if(locale == null)
			locale = Locale.getDefault();
		if(log.isTraceEnabled()) {
			log.trace("get(locale: {}, key: {}, args: {})", locale, key, args);
		}
		return messages.get(locale, key, args);
	}
}
