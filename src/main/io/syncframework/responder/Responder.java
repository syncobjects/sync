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

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.Result;
import io.syncframework.core.Response;
import io.syncframework.core.ResponseBean;

/**
 * 
 * @author dfroz
 *
 */
public interface Responder {
	public void destroy() throws Exception;
	public void init(ApplicationContext context) throws Exception;
	public void respond(Response response, ResponseBean bean, Result result) throws Exception;
}
