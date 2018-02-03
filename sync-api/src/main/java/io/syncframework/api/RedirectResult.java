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
package io.syncframework.api;

import java.util.Iterator;

public class RedirectResult extends Result {
	public static enum Code { TEMPORARY, PERMANENT };
	private Code code;
	private String url;
	
	public RedirectResult(String url) {
		this(url, Code.TEMPORARY);
	}
	public RedirectResult(String url, Code code) {
		this.url = url;
		this.code = code;
	}
	public Code getCode() {
		return code;
	}
	public void setCode(Code code) {
		this.code = code;
	}
	public String getUrl() {
		return this.url;
	}
	public Result setUrl(String url) {
		this.url = url;
		return this;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RedirectResult { ");
		sb.append("url: ").append(url).append(", ");
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
