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

public class RenderResult extends Result {
	private String template;
	
	public RenderResult(String template) {
		this.template = template;
	}
	public String getTemplate() {
		return template;
	}
	public Result setTemplate(String template) {
		this.template = template;
		return this;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RenderResult { ");
		sb.append("template: ").append(template).append(", ");
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
