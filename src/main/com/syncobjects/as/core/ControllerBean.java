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

import com.syncobjects.as.api.Action;
import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.Converter;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.FileUpload;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.SessionContext;
import com.syncobjects.as.i18n.MessageContextImpl;

/**
 * 
 * @author dfroz
 *
 */
public class ControllerBean implements ResponseBean {
	private static Logger log = LoggerFactory.getLogger(ControllerBean.class);
	private Method action;
	private Application application;
	private IController controller;
	private CookieContext cookieContext;
	private ErrorContext errorContext;
	private MessageContext messageContext;
	private RequestContext requestContext;
	private SessionContext sessionContext;

	public ControllerBean() {
		super();
	}

	public ControllerBean(Application application, IController controller, Method action) {
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
			log.debug("@Action "+this+"."+action.getName()+"()");

		Session session = request.getSession();

		errorContext = request.getSession().getErrorContext();
		requestContext = request.getRequestContext();
		sessionContext = request.getSession().getSessionContext();
		messageContext = new MessageContextImpl(application.getMessageFactory(), application.getContext(), session.getSessionContext());

		Method method = null;

		/*
		 *  INVOKE SPECIALS
		 */
		method = controller._asSettersApplicationContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(controller, application.getContext()); }
			catch(Exception e) { throw new ControllerBeanException(e, this); }
		}

		method = controller._asSettersCookieContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(controller, request.getCookieContext()); }
			catch(Exception e) { throw new ControllerBeanException(e, this); }
		}

		method = controller._asSettersErrorContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(controller, session.getErrorContext()); }
			catch(Exception e) { throw new ControllerBeanException(e, this); }
		}

		method = controller._asSettersMessageContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(controller, messageContext); }
			catch(Exception e) { throw new ControllerBeanException(e, this); }
		}

		method = controller._asSettersRequestContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(controller, request.getRequestContext()); }
			catch(Exception e) { throw new ControllerBeanException(e, this); }
		}

		method = controller._asSettersSessionContext();
		if(method != null) {
			if(log.isTraceEnabled())
				log.trace("binding ... @Context "+this+"."+method.getName()+"()");
			try { method.invoke(controller, session.getSessionContext()); }
			catch(Exception e) { throw new ControllerBeanException(e, this); }
		}

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

				Class<?> type = controller._asFields().get(name);
				if(type == null) {
					if(log.isDebugEnabled()) {
						log.debug("@Parameter "+name+" not declared by @Controller "+this+
								"; requested url: "+request.getUri()+"");
					}
					continue;
				}

				Converter<?> converter = application.getConverterFactory().getConverter(type);
				if(converter == null) {
					String errmsg = "failed to convert @Parameter "+name+" to @Controller "+this+"."+type.getName();
					log.error(errmsg);
					log.error("Converter not found for type: \""+type.getName()+"\"");
					throw new RuntimeException(errmsg);
				}
				try {
					Object argument = converter.convert(values.toArray(new String[0]));
					method = controller._asSetters().get(name);
					method.invoke(controller, argument);
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
				Class<?> type = controller._asFields().get(name);
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
					method = controller._asSetters().get(name);
					method.invoke(controller, file);
				}
				catch(Exception e) {
					throw new ControllerBeanException(e, this);
				}
			}
		}

		if(log.isTraceEnabled())
			log.trace("invoking @Action {}.{}()", controller.getClass().getName(), action.getName());

		Result result = null;
		
		try { result = (Result)action.invoke(controller, new Object[0]); }
		catch(Exception e) { throw new ControllerBeanException(e, this); }

		if(log.isTraceEnabled())
			log.trace("@Controller {}.{}() resulted in {}", this, action.getName(), result);

		return result;
	}

	public Class<?>[] interceptedBy() {
		if(action == null)
			return null;
		Action annotation = action.getAnnotation(Action.class);
		return annotation.interceptedBy();
	}

	// getters & setters

	public Method getAction() {
		return action;
	}

	public void setAction(Method action) {
		this.action = action;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public IController getController() {
		return controller;
	}

	public void setController(IController controller) {
		this.controller = controller;
	}

	public Object getField(String name) throws Exception {
		if(name == null)
			throw new IllegalArgumentException("name is null");
		Method method = controller._asGetters().get(name);
		if(method == null) {
			throw new RuntimeException("@Controller "+this+" has no field: "+name);
		}
		return method.invoke(controller, new Object[0]);
	}

	public Set<String> getFields() {
		return controller._asFields().keySet();
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