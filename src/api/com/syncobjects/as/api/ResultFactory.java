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
package com.syncobjects.as.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultFactory {
	private static final Map<String,RedirectResult> redirect = new ConcurrentHashMap<String,RedirectResult>();
	private static final Map<String,RenderResult> render = new ConcurrentHashMap<String,RenderResult>();
	
	public static Result redirect(String s) {
		return redirect(s, null, true);
	}
	
	public static Result redirect(String s, boolean cache) {
		return redirect(s, null, cache);
	}
	
	public static Result redirect(String s, RedirectResult result) {
		return redirect(s, result, true);
	}
	
	public static Result redirect(String s, RedirectResult result, boolean cache) {
		RedirectResult r = result;
		if(cache && r == null)
			r = redirect.get(s);
		if(r == null)
			r = new RedirectResult(s);
		if(cache)
			redirect.put(s, r);
		return r;
	}
	
	public static Result render(String s) {
		return render(s, null, true);
	}
	
	public static Result render(String s, boolean reuse) {
		return render(s, null, reuse);
	}
	
	public static Result render(String s, RenderResult result) {
		return render(s, result, true);
	}
	
	public static Result render(String s, RenderResult result, boolean cache) {
		RenderResult r = result;
		if(cache && r == null) {
			r = render.get(s);
		}
		if(r == null) {
			r = new RenderResult(s);
		}
		if(cache)
			render.put(s, r);
		return r;
	}
	
	public static void clear() {
		ResultFactory.redirect.clear();
		ResultFactory.render.clear();
	}
}
