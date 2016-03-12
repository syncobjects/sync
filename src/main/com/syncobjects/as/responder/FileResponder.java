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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.FileResult;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.core.Response;
import com.syncobjects.as.core.ResponseBean;
import com.syncobjects.as.core.Session;
import com.syncobjects.as.util.ServerCookie;

public class FileResponder implements Responder {
	private static final Logger log = LoggerFactory.getLogger(FileResponder.class);
	private ApplicationContext application;
	
	@Override
	public void destroy() throws Exception {
		// do nothing
	}

	@Override
	public void init(ApplicationContext context) throws Exception {
		this.application = context;
	}

	/**
	 * FileResponder safely locate the file under either application's Public or Private folders.
	 * This guarantees application's and server's security as only files located under this directories are allowed.
	 */
	@Override
	public void respond(Response response, ResponseBean bean, Result result) throws Exception {
		FileResult fr = (FileResult)result;
		
		File basedir = null;
		if(fr.getType() == FileResult.Type.PRIVATE)
			basedir = new File((String)application.get(ApplicationContext.PRIVATE_FOLDER));
		else
			basedir = new File((String)application.get(ApplicationContext.PUBLIC_FOLDER));
		File file = new File(basedir, fr.getPath());
		if(!file.exists())
			throw new RuntimeException("file "+file.getAbsolutePath()+" does not exist");
		response.setFile(file);
		// set content disposition header to guarantee correct filename.
		response.getHeaders().put(Result.CONTENT_DISPOSITION, "attachment; filename=\""+file.getName()+"\"");
		
		Session session = response.getSession();
		if(session.isRecent()) {
			if(log.isDebugEnabled())
				log.debug("setting session cookie header; response session: {}", session);
			session.setRecent(false);
			ServerCookie cookie = new ServerCookie(session.getIdKey(), session.getId(), "/");
			response.getHeaders().put(Result.SET_COOKIE_HEADER, cookie.toString());
		}		
		
		for(String key: fr.getHeaders().keySet()) {
			response.getHeaders().put(key, fr.getHeaders().get(key));
		}
		
		return;
	}
}
