package com.syncobjects.as.optimizer;

import java.util.Date;

import com.syncobjects.as.api.Action;
import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.Controller;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.Parameter;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.ResultFactory;
import com.syncobjects.as.api.SessionContext;

/**
 * An example of a @Controller
 * @author dfroz
 */
@Controller(url="/*")
public class ExampleController {
	private ApplicationContext application;
	private ErrorContext errors;
	private SessionContext session;
	@Parameter
	private String name;
	@Parameter(converter=SimpleDateConverter.class)
	private Date date;
	
	@Action
	public Result main() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.render("/main.ftl");
	}
	
	@Action(interceptedBy={ ExampleInterceptor.class, DummyInterceptor.class })
	public Result upload() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.render("/upload.ftl");
	}
	
	@Action(interceptedBy=ExampleInterceptor.class)
	public Result save() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.render("/save.ftl");
	}
	
	@Action
	public Result redir() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.redirect("/redir");
	}
	
	public ApplicationContext getApplication() {
		return application;
	}

	public void setApplication(ApplicationContext application) {
		this.application = application;
	}

	public ErrorContext getErrors() {
		return errors;
	}

	public void setErrors(ErrorContext errors) {
		this.errors = errors;
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
