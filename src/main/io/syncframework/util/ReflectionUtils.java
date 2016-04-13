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

/**
 * 
 * @author dfroz
 *
 */
public class ReflectionUtils {
	public static String getGetMethodName(String base) {
		String methodName = "get" + Character.toUpperCase(base.charAt(0)) + base.substring(1);
		return methodName;
	}
	public static String getSetMethodName(String base) {
		String methodName = "set" + Character.toUpperCase(base.charAt(0)) + base.substring(1);
		return methodName;
	}
}
