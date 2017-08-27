/*
 * Copyright (c) 2016-2017. SyncObjects Ltda.
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
package io.syncframework.api;

import java.io.File;

/**
 * <p>File upload class representation. It is a special {link @Parameter}. Any multipart form data uploaded to the
 * @Application will made available to your {link @Controller} using this class.
 * </p>
 * <p>Important to note that you're application is the ultimate responsible for handling files uploaded to the server.
 * In this case file system clean up shall be handled within your {link @Controller}'s code</p>
 * 
 * @author dfroz
 */
public class FileUpload {
	private File file;
	private String name;
	private String type;
	
	public FileUpload() {
		// nothing to be done
	}
	public FileUpload(String name, String type, File file) {
		this.name = name;
		this.type = type;
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}