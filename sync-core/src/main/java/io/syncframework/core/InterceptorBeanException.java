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
package io.syncframework.core;

/**
 * 
 * @author dfroz
 *
 */
public class InterceptorBeanException extends RuntimeException {
	private static final long serialVersionUID = -8259212980587382023L;
	private InterceptorBean interceptor;
	
	public InterceptorBeanException(Exception exception, InterceptorBean interceptor) {
		super(exception);
		this.interceptor = interceptor;
	}
	public InterceptorBean getInterceptor() {
		return interceptor;
	}
}
