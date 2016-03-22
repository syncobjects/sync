package com.syncobjects.as.optimizer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.syncobjects.as.api.Action;
import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.Interceptor;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.Parameter;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.ResultFactory;
import com.syncobjects.as.api.SessionContext;

@Interceptor
public class ExampleOptimizedInterceptor implements OInterceptor {
	private ErrorContext errors;
	private MessageContext message;
	private SessionContext session;
	@Parameter
	private String name;
	@Parameter(converter=SimpleDateConverter.class)
	private Date date;
	
	@Action
	public Result before() {
		return ResultFactory.render("/interceptor.ftl");
	}
	
	@Action
	public Result after() {
		return null;
	}

	public ErrorContext getErrors() {
		return errors;
	}

	public void setErrors(ErrorContext errors) {
		this.errors = errors;
	}

	public MessageContext getMessage() {
		return message;
	}

	public void setMessage(MessageContext message) {
		this.message = message;
	}

	public SessionContext getSession() {
		return session;
	}

	public void setSession(SessionContext session) {
		this.session = session;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	//
	// ASM generated code.
	//
	private static Map<String, Class<?>> _asConverters;
	private static Map<String, Class<?>> _asParameters;
	
	static {
		try {				
			_asParameters = new HashMap<String, Class<?>>();
			_asParameters.put("name", String.class);
			_asParameters.put("date", Date.class);
			
			_asConverters = new HashMap<String, Class<?>>();
			_asConverters.put("date", SimpleDateConverter.class);
		}
		catch(Throwable t) {
			throw t;
		}
	}
	
	@Override
	public Result _asBefore() {
		// return before();
		return null;
	}
	
	@Override
	public Result _asAfter() {
		return after();
	}
	
	@Override
	public Map<String, Class<?>> _asParameters() {
		return _asParameters;
	}

	/**
	 * getter implementation of parameter
	 */
	@Override
	public Object _asParameter(String name) {
		if(name.equals("name"))
			return getName();
		if(name.equals("date"))
			return getDate();
		return null;
	}

	@Override
	public void _asParameter(String name, Object value) {
		if(name.equals("name")) {
			setName((String)value);
			return;
		}
		if(name.equals("date")) {
			setDate((Date)value);
			return;
		}
	}
	
	@Override
	public Class<?> _asParameterConverter(String name) {
		return _asConverters.get(name);
	}

	@Override
	public void _asApplicationContext(ApplicationContext application) {
		// do nothing
	}
	
	@Override
	public void _asCookieContext(CookieContext cookies) {
		// do nothing
	}

	@Override
	public void _asErrorContext(ErrorContext errors) {
		this.errors = errors;
	}

	@Override
	public void _asMessageContext(MessageContext messages) {
		// do nothing
	}

	@Override
	public void _asRequestContext(RequestContext request) {
		// do nothing
	}

	@Override
	public void _asSessionContext(SessionContext session) {
		this.session = session;
	}
}
