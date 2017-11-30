package io.syncframework.optimizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syncframework.api.Action;
import io.syncframework.api.ApplicationContext;
import io.syncframework.api.Controller;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.MessageContext;
import io.syncframework.api.Parameter;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.ResultFactory;
import io.syncframework.api.SessionContext;

/**
 * Example of an optimized @Controller
 * We utilize this class only to use ASMifier and to serve as model to our implementation
 * @author dfroz
 *
 */
@Controller(url="/*")
public class ExampleOptimizedController implements OController {
	private ApplicationContext application;
	private ErrorContext errors;
	private SessionContext session;
	@Parameter
	private String name;
	@Parameter(converter=SimpleDateConverter.class)
	private Date date;
	
	@Action(type="text/html")
	public Result main() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.render("/main.ftl");
	}
	
	@Action(type="text/html", interceptedBy={ ExampleInterceptor.class, DummyInterceptor.class })
	public Result upload() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.render("/upload.ftl");
	}
	
	@Action(type="text/html", interceptedBy=ExampleInterceptor.class)
	public Result save() {
		int i=0;
		application.put("i", i++);
		errors.put("i", "String");
		session.put("i", i++);
		return ResultFactory.render("/save.ftl");
	}
	
	@Action(type="text/html")
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

	//
	// ASM generated code.
	//
	private static Map<String, Boolean> _asActions;
	private static Map<String, String> _asActionsType;
	private static Map<String, Class<?>> _asConverters;
	private static Map<String, Class<?>[]> _asInterceptors;
	private static Map<String, Class<?>> _asParameters;
	
	static {
		try {
			_asActions = new HashMap<String, Boolean>();
			_asActions.put("main", true);
			_asActions.put("upload", true);
			_asActions.put("save", true);
			_asActions.put("redir", true);
			
			_asActionsType = new HashMap<String,String>();
			_asActionsType.put("main", "text/html");
			_asActionsType.put("upload", "text/html");
			_asActionsType.put("save", "text/html");
			_asActionsType.put("redir", "text/html");
			
			_asInterceptors = new HashMap<String, Class<?>[]>();
			List<Class<?>> l = new ArrayList<Class<?>>();
			l.clear();
			l.add(ExampleInterceptor.class);
			l.add(DummyInterceptor.class);
			_asInterceptors.put("upload", l.toArray(new Class[0]));
			l.clear();
			l.add(ExampleInterceptor.class);
			_asInterceptors.put("save", l.toArray(new Class[0]));
				
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
	public String _asUrl() {
		return "/*";
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
		this.application = application;
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
	
	@Override
	public String _asActionType(String name) {
		return _asActionsType.get(name);
	}

	@Override
	public Result _asAction(String name) {		
		if(name.equals("upload"))
			return upload();
		else if(name.equals("save"))
			return save();
		else if(name.equals("main"))
			return main();
		else if(name.equals("redir"))
			return redir();
		else
			throw new RuntimeException("no action named "+name);
	}
	
	@Override
	public Class<?>[] _asActionInterceptors(String name) {
		return _asInterceptors.get(name);
	}
	

	@Override
	public boolean _asActionIsDefined(String name) {
		if(_asActions.containsKey(name))
			return true;
		return false;
	}
}
