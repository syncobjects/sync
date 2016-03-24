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
package com.syncobjects.as.responder;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.RenderResult;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.SessionContext;
import com.syncobjects.as.core.ApplicationConfig;
import com.syncobjects.as.core.Response;
import com.syncobjects.as.core.ResponseBean;
import com.syncobjects.as.core.Session;
import com.syncobjects.as.util.ExceptionUtils;
import com.syncobjects.as.util.ServerCookie;

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * @author dfroz
 */
public class RenderResponder implements Responder {
	private static final Logger log = LoggerFactory.getLogger(RenderResponder.class);
	public final static String TEMPLATE_ENGINE = 
			"com.syncobjects.as.responder.RenderResponder.TEMPLATE_ENGINE";
	private final String charset = "UTF-8";
	private ApplicationContext context;

	public void destroy() throws Exception {
		// set template configuration for GC
		context.remove(TEMPLATE_ENGINE);
	}

	public void init(ApplicationContext context) throws Exception {
		this.context = context;
		
		ApplicationConfig config = (ApplicationConfig)context.get(ApplicationContext.PROPERTIES);
		
		if(log.isDebugEnabled()) {
			log.debug("render: using freemarker template version: {}", config.getTemplateVersion());
			log.debug("render: {} template cache", config.getTemplateCache() != null && config.getTemplateCache()? "enabling": "disabling");
			log.debug("render: using template charset {}", this.charset);
		}
		
		Configuration cfg = new Configuration(new Version(config.getTemplateVersion()));
		cfg.setDefaultEncoding(this.charset);
		cfg.setDirectoryForTemplateLoading(config.getPrivateDirectory());
		cfg.setLocalizedLookup(true);
		cfg.setLocale(config.getLocale());
		cfg.setNumberFormat("computer");
		cfg.setAPIBuiltinEnabled(true);
		if(config.getTemplateCache() != null && config.getTemplateCache()) {
			cfg.setTemplateUpdateDelayMilliseconds(Long.MAX_VALUE); // loads templates only once
			cfg.setCacheStorage(new MruCacheStorage(Integer.MAX_VALUE, 0));
			// configure template exception handler to RETHROW. this way makes it to production ready. 
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		}
		else {
			cfg.setTemplateUpdateDelayMilliseconds(0);
		}
		cfg.setObjectWrapper(new DefaultObjectWrapper(new Version(config.getTemplateVersion())));
		
		this.context.put(TEMPLATE_ENGINE, cfg);
	}

	@Override
	public void respond(Response response, ResponseBean bean, Result result) throws Exception {
		if(result == null || !(result instanceof RenderResult))
			throw new IllegalArgumentException("result is not an instance of RenderResult");
		if(response == null)
			throw new IllegalArgumentException("response is null");

		RenderResult rr = (RenderResult)result;
		String tpl = rr.getTemplate();
		if(tpl == null) {
			log.error("RenderResult.template not defined");
			throw new RuntimeException("RenderResult.template not defined");
		}
		if(log.isTraceEnabled())
			log.trace("template: tpl: "+tpl);

		Map<String,Object> attributes = new HashMap<String,Object>();
		if(bean.getApplicationContext() != null) {
			attributes.put("_application", bean.getApplicationContext());
		}
		if(bean.getErrorContext() != null) {
			attributes.put("_errors", bean.getErrorContext());
		}
		if(bean.getRequestContext() != null) {
			attributes.put("_request", bean.getRequestContext());
		}
		if(bean.getSessionContext() != null) {
			attributes.put("_session", bean.getSessionContext());
		}
		if(bean.getMessageContext() != null) {
			attributes.put("_messages", bean.getMessageContext());
		}

		for(String field: bean.getParametersName()) {
			Object object = bean.getParameter(field);
			attributes.put(field, object);
		}
		// result parameters directly to the template root
		if(rr.getParameters().size() > 0) {
			for(String param: rr.getParameters().keySet()) {
				attributes.put(param, rr.getParameters().get(param));
			}
		}
		
		if(log.isTraceEnabled()) {
			log.trace("render template attributes mapped");
		}
		
		ByteArrayOutputStream bos = (ByteArrayOutputStream)response.getOutputStream();
		bos.reset();
		
		try {
			Configuration cfg = (Configuration)context.get(TEMPLATE_ENGINE);
			Template template = null;
			Locale locale = null;
			if(bean.getSessionContext() != null)
				locale = (Locale)bean.getSessionContext().get(SessionContext.LOCALE);
			if(locale == null)
				locale = (Locale)bean.getApplicationContext().get(ApplicationContext.LOCALE);
			if(locale != null) {
				template = cfg.getTemplate(tpl, locale);
				if(log.isTraceEnabled())
					log.trace("rendering "+bean+" to template "+template.getName()+", with locale "+locale);
			}
			else {
				template = cfg.getTemplate(tpl);
				if(log.isTraceEnabled())
					log.trace("rendering "+bean+" to template "+template.getName());
			}
			
			response.setCode(Response.Code.OK);
			PrintWriter writer = new PrintWriter(bos);
			template.process(attributes, writer);
			bos.flush();
		}
		catch(TemplateException e) {
			StringBuffer sb = new StringBuffer();
			sb.append(e.getFTLInstructionStack().replaceAll("\n", "<br/>\n"));
			if(e.getCause() != null && e.getCause().getMessage() != null) {
				sb.append(e.getCause().getMessage()).append("<br/>\n");
				sb.append("----------<br/>\n");
			}
			sb.append(ExceptionUtils.printStackTraceHtml(e));
			// sb.append("Caused by: ").append(ExceptionUtils.printStackTraceHtml(e.getCauseException()));
			response.setCode(Response.Code.INTERNAL_ERROR);
			response.getOutputStream().write(sb.toString().getBytes());
			bos.flush();
			return;
		}
		
		String contentType = "text/html; charset="+this.charset;
		if(rr.getHeaders().containsKey(Result.CONTENT_TYPE_HEADER)) {
			contentType = rr.getHeaders().get(Result.CONTENT_TYPE_HEADER);
			if(!contentType.contains("charset"))
				contentType += "; charset="+this.charset;
			rr.getHeaders().remove(Result.CONTENT_TYPE_HEADER);
		}
		response.getHeaders().put(Result.CONTENT_TYPE_HEADER, contentType);
		
		Session session = response.getSession();
		if(session.isRecent()) {
			if(log.isDebugEnabled())
				log.debug("setting session cookie header; response session: {}", session);
			session.setRecent(false);
			ServerCookie cookie = new ServerCookie(session.getIdKey(), session.getId(), null, "/", 604800);
			response.getHeaders().put(Result.SET_COOKIE_HEADER, cookie.toString());
		}
		for(String key: rr.getHeaders().keySet()) {
			response.getHeaders().put(key, rr.getHeaders().get(key));
		}
		
		//
		// clear errors after successful render
		//
		session.getErrorContext().clear();
	}
}