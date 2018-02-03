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
public class RequestUtils {
	public static String getDomainName(String domain) {
		if(domain == null)
			throw new IllegalArgumentException("domain");
		int domainColonPos = domain.indexOf(':');
		if(domainColonPos != -1) {
			domain = domain.substring(0, domainColonPos);
		}
		return domain;
	}
}
