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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultFactory {
	private static final Map<String,FileResult> file = new ConcurrentHashMap<String,FileResult>();
	private static final Map<String,RedirectResult> redirect = new ConcurrentHashMap<String,RedirectResult>();
	private static final Map<String,RenderResult> render = new ConcurrentHashMap<String,RenderResult>();
	
	public static Result file(String path, FileResult.Type type) {
		return file(path, type, true);
	}
	
	public static Result file(String path, FileResult.Type type, boolean cache) {
		FileResult r = null;
		if(cache)
			r = file.get(path.toLowerCase());
		if(r == null)
			r = new FileResult(path, type);
		if(cache)
			file.put(path.toLowerCase(), r);
		return r;
	}
	
	public static Result redirect(String url) {
		return redirect(url, true);
	}
	
	public static Result redirect(String url, boolean cache) {
		RedirectResult r = null;
		if(cache && r == null)
			r = redirect.get(url);
		if(r == null)
			r = new RedirectResult(url);
		if(cache)
			redirect.put(url, r);
		return r;
	}
	
	public static Result render(String template) {
		return render(template, true);
	}
	
	public static Result render(String template, boolean cache) {
		RenderResult r = null;
		if(cache) {
			r = render.get(template);
		}
		if(r == null) {
			r = new RenderResult(template);
		}
		if(cache)
			render.put(template, r);
		return r;
	}
	
	public static void clear() {
		ResultFactory.file.clear();
		ResultFactory.redirect.clear();
		ResultFactory.render.clear();
	}
}
