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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author dfroz
 *
 */
public class Response implements Recyclable {
	public enum Code {
		OK,
		INTERNAL_ERROR,
		PERMANENT_REDIRECT,
		TEMPORARY_REDIRECT,
		NOT_FOUND
	}
	private Application application;
	private Code code;
	private File file;
	private final Map<String, String> headers = new ConcurrentHashMap<String, String>();
	private final OutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
	private Session session;
	
	public Response() {
		code = Code.OK;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	@Override
	public void recycle() {
		application = null;
		file = null;
		session = null;
		headers.clear();
		((ByteArrayOutputStream)outputStream).reset();
	}
}