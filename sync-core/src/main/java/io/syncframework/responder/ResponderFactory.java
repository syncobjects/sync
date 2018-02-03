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
package io.syncframework.responder;

import io.syncframework.api.FileResult;
import io.syncframework.api.RedirectResult;
import io.syncframework.api.RenderResult;
import io.syncframework.api.Result;
import io.syncframework.core.Application;

/**
 * 
 * @author dfroz
 *
 */
public class ResponderFactory {
	private FileResponder file;
	private RedirectResponder redirect;
	private RenderResponder render;

	public ResponderFactory() {
		this.file = new FileResponder();
		this.redirect = new RedirectResponder();
		this.render = new RenderResponder();
	}

	public void destroy() throws Exception {
		file.destroy();
		redirect.destroy();
		render.destroy();
	}

	public void init(Application application) throws Exception {
		file.init(application.getContext());
		redirect.init(application.getContext());
		render.init(application.getContext());
	}

	public Responder find(Result result) {
		if(result instanceof FileResult) {
			return file;
		}
		if(result instanceof RedirectResult) {
			return redirect;
		}
		if(result instanceof RenderResult) {
			return render;
		}
		return null;
	}
}
