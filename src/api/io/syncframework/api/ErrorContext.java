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

/**
 * Special parameter to your {link @Controller}. ErrorContext is available during the request cycle and persisted whether the Result
 * is a Redirect. In this case, if you set an Error while responding with RedirectResult, next call to RenderResult, the information
 * will be there to be handled either by @Controller or by the Render engine.
 * 
 * @author dfroz
 */
public class ErrorContext extends Context<String,String> {
	private static final long serialVersionUID = 92320367354664164L;
}
