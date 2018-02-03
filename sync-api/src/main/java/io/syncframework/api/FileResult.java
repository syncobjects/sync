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

import java.util.Iterator;

/**
 * FileResult shall be utilized in case where your application will deliver a file located under the app/private/ folder.
 * 
 * @author dfroz
 */
public class FileResult extends Result {
	public enum Type { PUBLIC, PRIVATE };
	private String path;
	private Type type;
	
	public FileResult(String path) {
		this(path, Type.PRIVATE);
	}
	public FileResult(String path, Type type) {
		this.path = path;
		this.type = type; 
	}
	public String getPath() {
		return path;
	}
	public Result setPath(String path) {
		this.path = path;
		return this;
	}
	public Type getType() {
		return type;
	}
	public Result setType(Type type) {
		this.type = type;
		return this;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FileResult { ");
		sb.append("path: ").append(path).append(", ");
		sb.append("type: ").append(type.toString()).append(", ");
		sb.append("parameters: { ");
		Iterator<String> it = this.getParameters().keySet().iterator();
		while(it.hasNext()) {
			String param = it.next();
			sb.append(param).append(": ").append(this.getParameters().get(param));
			if(it.hasNext())
				sb.append(", ");
		}
		sb.append(" }, ");
		sb.append("headers: { ");
		it = this.getHeaders().keySet().iterator();
		while(it.hasNext()) {
			String header = it.next();
			sb.append(header).append(": ").append(this.getHeaders().get(header));
			if(it.hasNext())
				sb.append(", ");
		}
		sb.append("}");
		sb.append("}");
		return sb.toString();
	}
}
