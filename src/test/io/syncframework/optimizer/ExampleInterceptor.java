package io.syncframework.optimizer;

import java.util.Date;

import io.syncframework.api.Action;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.Interceptor;
import io.syncframework.api.MessageContext;
import io.syncframework.api.Parameter;
import io.syncframework.api.Result;
import io.syncframework.api.ResultFactory;
import io.syncframework.api.SessionContext;

@Interceptor
public class ExampleInterceptor {
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
}
