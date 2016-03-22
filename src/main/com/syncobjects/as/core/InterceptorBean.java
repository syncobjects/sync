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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.SessionContext;
import com.syncobjects.as.converter.Converter;
import com.syncobjects.as.i18n.MessageContextImpl;

/**
 * 
 * @author dfroz
 *
 */
public class InterceptorBean implements ResponseBean {
	private static Logger log = LoggerFactory.getLogger(InterceptorBean.class);
	private Application application;
	private CookieContext cookieContext;
	private ErrorContext errorContext;
	private IInterceptor interceptor;
	private MessageContext messageContext;
	private RequestContext requestContext;
	private SessionContext sessionContext;

	public InterceptorBean() {
		super();
	}

	public InterceptorBean(Application application, IInterceptor interceptor) {
		this.application = application;
		this.interceptor = interceptor;
	}

	public Result after(Request request, Response response) {
		if(request == null)
			throw new IllegalArgumentException("invalid request argument");
		if(response == null)
			throw new IllegalArgumentException("invalid response argument");

		if(log.isDebugEnabled())
			log.debug("@Interceptor "+this+".after()");

		Session session = request.getSession();

		errorContext = request.getSession().getErrorContext();
		requestContext = request.getRequestContext();
		sessionContext = request.getSession().getSessionContext();
		messageContext = new MessageContextImpl(application.getMessageFactory(), application.getContext(), session.getSessionContext());

		Method method = null;

		/*
		 *  INVOKE SPECIALS
		 */
		method = interceptor._asSettersApplicationContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, application.getContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersCookieContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, request.getCookieContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersErrorContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, session.getErrorContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersMessageContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, messageContext); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersRequestContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, request.getRequestContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersSessionContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, session.getSessionContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		/*
		 *  INVOKE SETTERS
		 */

		Set<String> keys = request.getParameters().keySet();
		if(keys != null) {
			for(String name: keys) {
				List<String> values = request.getParameters().get(name);

				if(values.size() == 0) {
					continue;
				}

				if(log.isTraceEnabled())
					log.trace("binding ... @Parameter "+this+"."+name);

				Class<?> type = interceptor._asFields().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Interceptor "+this+
								"; requested url: "+request.getUri()+"");
					}
					continue;
				}

				Converter<?> converter = application.getConverterFactory().getConverter(type);
				if(converter == null) {
					String errmsg = "Failed to convert @Parameter "+name+" to @Interceptor "+this+"."+type.getName();
					log.error(errmsg);
					log.error("Converter not found for type: \""+type.getName()+"\"");
					throw new RuntimeException(errmsg);
				}
				
				if(log.isTraceEnabled())
					log.trace(converter+".convert({})", values);

				try {
					Object argument = converter.convert(values.toArray(new String[0]));
					method = interceptor._asSetters().get(name);
					method.invoke(interceptor, argument);
				}
				catch(Exception e) { throw new InterceptorBeanException(e, this); }
			}
		}

		if(log.isTraceEnabled())
			log.trace("invoking @Action "+this+".after()");
		Result result = null;
		try { result = (Result)interceptor._asAfter().invoke(interceptor, new Object[0]); }
		catch(Exception e) { throw new InterceptorBeanException(e, this); }

		return result;
	}

	public Result before(Request request, Response response) {
		if(request == null)
			throw new IllegalArgumentException("invalid request argument");
		if(response == null)
			throw new IllegalArgumentException("invalid response argument");

		if(log.isDebugEnabled())
			log.debug("@Interceptor "+this+".before()");

		Session session = request.getSession();
		errorContext = request.getSession().getErrorContext();
		requestContext = request.getRequestContext();
		sessionContext = request.getSession().getSessionContext();
		messageContext = new MessageContextImpl(application.getMessageFactory(), application.getContext(), session.getSessionContext());

		Method method = null;

		/*
		 *  INVOKE SPECIALS
		 */
		method = interceptor._asSettersApplicationContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, application.getContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersCookieContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, request.getCookieContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersErrorContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, session.getErrorContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersMessageContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, messageContext); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersRequestContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, request.getRequestContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		method = interceptor._asSettersSessionContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(interceptor, session.getSessionContext()); }
			catch(Exception e) { throw new InterceptorBeanException(e, this); }
		}

		/*
		 *  INVOKE SETTERS
		 */

		Set<String> keys = request.getParameters().keySet();
		if(keys != null) {
			for(String name: keys) {
				List<String> values = request.getParameters().get(name);

				if(values.size() == 0) {
					continue;
				}

				if(log.isTraceEnabled())
					log.trace("binding ... "+this+" with parameter: "+name);

				Class<?> type = interceptor._asFields().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Interceptor "+this+
								"; requested url: "+request.getUri()+"");
					}
					continue;
				}

				Converter<?> converter = application.getConverterFactory().getConverter(type);
				if(converter == null) {
					String errmsg = "Failed to convert @Parameter "+name+" to @Interceptor "+this+"."+type.getName();
					log.error(errmsg);
					log.error("Converter not found for type: \""+type.getName()+"\"");
					throw new RuntimeException(errmsg);
				}

				if(log.isTraceEnabled())
					log.trace(converter+".convert({})", values.get(0));

				try {
					Object argument = converter.convert(values.toArray(new String[0]));
					method = interceptor._asSetters().get(name);
					method.invoke(interceptor, argument);
				}
				catch(Exception e) { throw new InterceptorBeanException(e, this); }
			}
		}

		if(log.isTraceEnabled())
			log.trace("invoking @Action "+this+".before()");

		Result result = null;
		
		try { result = (Result)interceptor._asBefore().invoke(interceptor, new Object[0]); }
		catch(Exception e) { throw new InterceptorBeanException(e, this); }
		
		return result;
	}

	public IInterceptor getInterceptor() {
		return interceptor;
	}

	public Object getField(String name) throws Exception {
		if(name == null)
			throw new IllegalArgumentException("name is null");
		Method method = interceptor._asGetters().get(name);
		if(method == null) {
			throw new RuntimeException("@Interceptor "+this+" has no field: "+name);
		}
		return method.invoke(interceptor, new Object[0]);
	}

	public Set<String> getFields() {
		return interceptor._asFields().keySet();
	}

	public ApplicationContext getApplicationContext() {
		return application.getContext();
	}

	public CookieContext getCookieContext() {
		return cookieContext;
	}

	public void setCookieContext(CookieContext cookieContext) {
		this.cookieContext = cookieContext;
	}

	public ErrorContext getErrorContext() {
		return errorContext;
	}

	public void setErrorContext(ErrorContext errorContext) {
		this.errorContext = errorContext;
	}

	public MessageContext getMessageContext() {
		return messageContext;
	}

	public void setMessageContext(MessageContext messageContext) {
		this.messageContext = messageContext;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public String toString() {
		return interceptor.getClass().getName();
	}
}