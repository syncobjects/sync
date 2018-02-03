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
import io.syncframework.optimizer.OController;

/**
 * Represents a \@Controller so abstract the optimized Controller interface from other code
 * 
 * @author dfroz
 */
public class ControllerBean implements ResponseBean {
	private static Logger log = LoggerFactory.getLogger(ControllerBean.class);
	private String action;
	private Application application;
	private String contentType;
	private OController controller;
	private CookieContext cookieContext;
	private ErrorContext errorContext;
	private MessageContext messageContext;
	private RequestContext requestContext;
	private SessionContext sessionContext;

	public ControllerBean() {
		super();
	}

	public ControllerBean(Application application, OController controller, String action) {
		this.controller = controller;
		this.application = application;
		this.action = action;
	}

	/**
	 * Identifies the action associated with the URL.
	 * The Action can be identified by either URL pattern group or by the request parameter "action"
	 * @return
	 * @throws Exception
	 */
	public Result action(Request request, Response response) throws ControllerBeanException {
		if(request == null)
			throw new IllegalArgumentException("invalid request argument");
		if(response == null)
			throw new IllegalArgumentException("invalid response argument");

		if(log.isDebugEnabled())
			log.debug("@Action "+this+"."+action+"()");
		
		contentType = controller._asActionType(action);

		Session session = request.getSession();

		errorContext = request.getSession().getErrorContext();
		cookieContext = request.getCookieContext();
		requestContext = request.getRequestContext();
		sessionContext = request.getSession().getSessionContext();
		messageContext = new MessageContextImpl(application.getMessageFactory(), application.getContext(), session.getSessionContext());

		/*
		 *  INVOKE SPECIALS
		 */
		controller._asApplicationContext(application.getContext());
		controller._asCookieContext(cookieContext);
		controller._asErrorContext(errorContext);
		controller._asMessageContext(messageContext);
		controller._asRequestContext(requestContext);
		controller._asSessionContext(sessionContext);

		/*
		 *  INVOKE SETTERS
		 */

		Set<String> keys = request.getParameters().keySet();
		if(keys != null) {
			for(String name: keys) {
				List<String> values = request.getParameters().get(name);
				if(values.size() == 0)
					continue;

				if(log.isTraceEnabled())
					log.trace("binding ... @Parameter "+this+"."+name);

				Class<?> type = controller._asParameters().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Controller "+this+
								"; requested url: "+request.getUri()+"");
					}
					continue;
				}
				
				// check for defined converter
				Converter<?> converter = application.getConverterFactory().getConverter(type);
				Class<?> converterClazz = controller._asParameterConverter(name);
				if(converterClazz != null) {
					try { converter = (Converter<?>)converterClazz.getDeclaredConstructor().newInstance(); }
					catch(Exception e) {
						throw new ControllerBeanException(e, this);
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
					controller._asParameter(name, value);
				}
				catch(Exception e) {
					throw new ControllerBeanException(e, this);
				}
			}
		}
		keys = request.getFiles().keySet();
		if(keys != null) {
			for(String name: keys) {
				FileUpload file = request.getFiles().get(name);
				Class<?> type = controller._asParameters().get(name);
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
					controller._asParameter(name, file);
				}
				catch(Exception e) {
					throw new ControllerBeanException(e, this);
				}
			}
		}

		if(log.isTraceEnabled())
			log.trace("invoking @Action {}.{}()", controller.getClass().getName(), action);

		Result result = null;
		
		try { result = controller._asAction(action); }
		catch(Exception e) { throw new ControllerBeanException(e, this); }

		if(log.isTraceEnabled())
			log.trace("@Controller {}.{}() resulted in {}", this, action, result);

		return result;
	}
	
	public Class<?>[] interceptedBy() {
		if(action == null)
			return null;
		return controller._asActionInterceptors(action);
	}

	// getters & setters

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public OController getController() {
		return controller;
	}

	public void setController(OController controller) {
		this.controller = controller;
	}
	
	@Override
	public Object getParameter(String name) throws Exception {
		return controller._asParameter(name);
	}

	@Override
	public Set<String> getParametersName() {
		return controller._asParameters().keySet();
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
		return controller.getClass().getName();
	}
}