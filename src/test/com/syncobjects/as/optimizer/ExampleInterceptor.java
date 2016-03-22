package com.syncobjects.as.optimizer;

import java.util.Date;

import com.syncobjects.as.api.Action;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.Interceptor;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.Parameter;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.ResultFactory;
import com.syncobjects.as.api.SessionContext;

@Interceptor
public class ExampleInterceptor {
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
}
