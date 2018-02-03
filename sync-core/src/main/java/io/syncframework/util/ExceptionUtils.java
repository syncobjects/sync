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
package io.syncframework.util;

import java.lang.reflect.InvocationTargetException;

import io.syncframework.core.ControllerBeanException;
import io.syncframework.core.InterceptorBeanException;

/**
 * 
 * @author dfroz
 *
 */
public class ExceptionUtils {
	
	public static String printStackTrace(Throwable t) {
		StringBuffer sb = new StringBuffer();
		while(t != null) {
			if(t instanceof InvocationTargetException) {
				t = t.getCause();
				continue;
			}
			if(t instanceof ControllerBeanException) {
				t = t.getCause();
				continue;
			}
			if(t instanceof InterceptorBeanException) {
				t = t.getCause();
				continue;
			}
			sb.append(t).append(": ").append(t.getMessage()).append("\n");
			for(StackTraceElement elem: t.getStackTrace()) {
				sb.append("\tat ");
				sb.append(elem.getClassName());
				sb.append(".");
				sb.append(elem.getMethodName());
				sb.append("(");
				sb.append(elem.getFileName());
				sb.append(":");
				sb.append(elem.getLineNumber());
				sb.append(")\n");
			}
			t = t.getCause();
		}
		
		return sb.toString();
	}
	
	public static String printStackTraceHtml(Throwable t) {
		StringBuffer sb = new StringBuffer();
		
		while(t != null) {
			if(t instanceof InvocationTargetException) {
				t = t.getCause();
				continue;
			}
			if(t instanceof ControllerBeanException) {
				t = t.getCause();
				continue;
			}
			if(t instanceof InterceptorBeanException) {
				t = t.getCause();
				continue;
			}
			sb.append("<b>").append(t).append(": ").append(t.getMessage()).append("</b><br/>\n");
			sb.append("<ul>\n");
			for(StackTraceElement elem: t.getStackTrace()) {
				sb.append("<li>at ");
				sb.append(elem.getClassName());
				sb.append(".");
				sb.append(elem.getMethodName());
				sb.append("(");
				sb.append(elem.getFileName());
				sb.append(":");
				sb.append(elem.getLineNumber());
				sb.append(")</li>\n");
			}
			sb.append("</ul>\n");
			t = t.getCause();
		}
		
		return sb.toString();
	}
}
