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

public class RequestContextMock extends Context<String, Object> {
	private static final long serialVersionUID = 4129335630680225434L;
	public static final String REMOTE_ADDRESS = "com.syncobjects.as.api.RequestContext.REMOTE_ADDRESS";
	public static final String REMOTE_PORT = "com.syncobjects.as.api.RequestContext.REMOTE_PORT";
	public static final String URL = "com.syncobjects.as.api.RequestContext.URL";
	
	public RequestContextMock() {
		put(REMOTE_ADDRESS, "127.0.0.1");
		put(REMOTE_PORT, "12345");
		put(URL, "/");
	}
	
	public void clear() {
		super.clear();
		put(REMOTE_ADDRESS, "127.0.0.1");
		put(REMOTE_PORT, "12345");
		put(URL, "/");
	}
}
