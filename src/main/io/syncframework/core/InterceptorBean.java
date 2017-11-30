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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.Converter;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.FileUpload;
import io.syncframework.api.MessageContext;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.SessionContext;
import io.syncframework.i18n.MessageContextImpl;
import io.syncframework.optimizer.OInterceptor;

/**
 * 
 * @author dfroz
 *
 */
public class InterceptorBean implements ResponseBean {
	private static Logger log = LoggerFactory.getLogger(InterceptorBean.class);
	private Application application;
	private String contentType;
	private OInterceptor interceptor;
	private CookieContext cookieContext;
	private ErrorContext errorContext;
	private MessageContext messageContext;
	private RequestContext requestContext;
	private SessionContext sessionContext;

	public InterceptorBean() {
		super();
	}

	public InterceptorBean(Application application, OInterceptor interceptor) {
		this.application = application;
		this.interceptor = interceptor;
	}

	public Result after(Request request, Response response) {
		if(request == null)
			throw new IllegalArgumentException("invalid request argument");
		if(response == null)
			throw new IllegalArgumentException("invalid response argument");

		contentType = interceptor._asAfterType();
		
		errorContext = request.getSession().getErrorContext();
		cookieContext = request.getCookieContext();
		requestContext = request.getRequestContext();
		sessionContext = request.getSession().getSessionContext();
		messageContext = new MessageContextImpl(application.getMessageFactory(), application.getContext(), sessionContext);

		/*
		 *  INVOKE SPECIALS
		 */
		interceptor._asApplicationContext(application.getContext());
		interceptor._asCookieContext(cookieContext);
		interceptor._asErrorContext(errorContext);
		interceptor._asMessageContext(messageContext);
		interceptor._asRequestContext(requestContext);
		interceptor._asSessionContext(sessionContext);

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

				Class<?> type = interceptor._asParameters().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Controller "+this+
								"; requested url: "+request.getUri()+"");
					}
					continue;
				}
				
				// check for defined converter
				Converter<?> converter = application.getConverterFactory().getConverter(type);
				Class<?> converterClazz = interceptor._asParameterConverter(name);
				if(converterClazz != null) {
					try { converter = (Converter<?>)converterClazz.getDeclaredConstructor().newInstance(); }
					catch(Exception e) {
						throw new InterceptorBeanException(e, this);
					}
				}
				if(converter == null) {
					log.error("{}: @Converter not found to handle type: {}", application, type.getName());
					StringBuilder sb = new StringBuilder();
					sb.append(application).append(" failed to locate @Converter to deal with @Parameter ").append(this).append(".").append(name);
					String errmsg = sb.toString();
					log.error(errmsg);
					throw new RuntimeException(errmsg);
				}
				try {
					Object value = converter.convert(values.toArray(new String[0]));
					interceptor._asParameter(name, value);
				}
				catch(Exception e) {
					throw new InterceptorBeanException(e, this);
				}
			}
		}
		keys = request.getFiles().keySet();
		if(keys != null) {
			for(String name: keys) {
				FileUpload file = request.getFiles().get(name);
				Class<?> type = interceptor._asParameters().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Controller "+this+
								"; requested url: "+request.getUri());
					}
					continue;
				}
				if(type != FileUpload.class) {
					if(log.isDebugEnabled())
						log.debug("@Parameter "+name+" not declared as FileUpload at @Controller "+this+
								"; requested url: "+request.getUri());
					continue;
				}
				try {
					interceptor._asParameter(name, file);
				}
				catch(Exception e) {
					throw new InterceptorBeanException(e, this);
				}
			}
		}

		if(log.isTraceEnabled())
			log.trace("invoking @Action {}.after()", interceptor.getClass().getName());

		Result result = null;
		
		try { result = interceptor._asAfter(); }
		catch(Exception e) { throw new InterceptorBeanException(e, this); }

		if(log.isTraceEnabled())
			log.trace("@Interceptor {}.after() resulted in {}", this, result);

		return result;
	}

	public Result before(Request request, Response response) {
		if(request == null)
			throw new IllegalArgumentException("invalid request argument");
		if(response == null)
			throw new IllegalArgumentException("invalid response argument");
		
		contentType = interceptor._asBeforeType();

		errorContext = request.getSession().getErrorContext();
		cookieContext = request.getCookieContext();
		requestContext = request.getRequestContext();
		sessionContext = request.getSession().getSessionContext();
		messageContext = new MessageContextImpl(application.getMessageFactory(), application.getContext(), sessionContext);

		/*
		 *  INVOKE SPECIALS
		 */
		interceptor._asApplicationContext(application.getContext());
		interceptor._asCookieContext(cookieContext);
		interceptor._asErrorContext(errorContext);
		interceptor._asMessageContext(messageContext);
		interceptor._asRequestContext(requestContext);
		interceptor._asSessionContext(sessionContext);

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

				Class<?> type = interceptor._asParameters().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Controller "+this+
								"; requested url: "+request.getUri()+"");
					}
					continue;
				}
				
				// check for defined converter
				Converter<?> converter = application.getConverterFactory().getConverter(type);
				Class<?> converterClazz = interceptor._asParameterConverter(name);
				if(converterClazz != null) {
					try { converter = (Converter<?>)converterClazz.getDeclaredConstructor().newInstance(); }
					catch(Exception e) {
						throw new InterceptorBeanException(e, this);
					}
				}
				if(converter == null) {
					log.error("{}: @Converter not found to handle type: {}", application, type.getName());
					StringBuilder sb = new StringBuilder();
					sb.append(application).append(" failed to locate @Converter to deal with @Parameter ").append(this).append(".").append(name);
					String errmsg = sb.toString();
					log.error(errmsg);
					throw new RuntimeException(errmsg);
				}
				try {
					Object value = converter.convert(values.toArray(new String[0]));
					interceptor._asParameter(name, value);
				}
				catch(Exception e) {
					throw new InterceptorBeanException(e, this);
				}
			}
		}
		keys = request.getFiles().keySet();
		if(keys != null) {
			for(String name: keys) {
				FileUpload file = request.getFiles().get(name);
				Class<?> type = interceptor._asParameters().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Controller "+this+
								"; requested url: "+request.getUri());
					}
					continue;
				}
				if(type != FileUpload.class) {
					if(log.isDebugEnabled())
						log.debug("@Parameter "+name+" not declared as FileUpload at @Controller "+this+
								"; requested url: "+request.getUri());
					continue;
				}
				try {
					interceptor._asParameter(name, file);
				}
				catch(Exception e) {
					throw new InterceptorBeanException(e, this);
				}
			}
		}

		if(log.isTraceEnabled())
			log.trace("invoking @Action {}.before()", interceptor.getClass().getName());

		Result result = null;
		
		try { result = interceptor._asBefore(); }
		catch(Exception e) { throw new InterceptorBeanException(e, this); }

		if(log.isTraceEnabled())
			log.trace("@Interceptor {}.before() resulted in {}", this, result);

		return result;
	}

	public OInterceptor getInterceptor() {
		return interceptor;
	}

	public Object getParameter(String name) throws Exception {
		if(name == null)
			throw new IllegalArgumentException("name is null");
		return interceptor._asParameter(name);
	}

	public Set<String> getParametersName() {
		return interceptor._asParameters().keySet();
	}

	public ApplicationContext getApplicationContext() {
		return application.getContext();
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
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