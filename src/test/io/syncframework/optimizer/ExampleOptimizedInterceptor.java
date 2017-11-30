package io.syncframework.optimizer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.syncframework.api.Action;
import io.syncframework.api.ApplicationContext;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.Interceptor;
import io.syncframework.api.MessageContext;
import io.syncframework.api.Parameter;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.ResultFactory;
import io.syncframework.api.SessionContext;

@Interceptor
public class ExampleOptimizedInterceptor implements OInterceptor {
	private ErrorContext errors;
	private MessageContext message;
	private SessionContext session;
	@Parameter
	private String name;
	@Parameter(converter=SimpleDateConverter.class)
	private Date date;
	
	@Action(type="text/html")
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
	private static String _asAfterType;
	private static String _asBeforeType;
	private static Map<String, Class<?>> _asConverters;
	private static Map<String, Class<?>> _asParameters;
	
	static {
		try {
			_asAfterType = "text/html";
			_asBeforeType = "text/html";
			
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
	public Result _asAfter() {
		return after();
	}
	
	@Override
	public String _asAfterType() {
		return _asAfterType;
	}
	
	@Override
	public Result _asBefore() {
		// return before();
		return null;
	}
	
	@Override
	public String _asBeforeType() {
		return _asBeforeType;
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
