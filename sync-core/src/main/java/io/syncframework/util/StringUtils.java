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

import java.util.Locale;

/**
 * 
 * @author dfroz
 *
 */
public class StringUtils {
	public static String capitalize(String s) {
		if(StringUtils.isBlank(s))
			return null;
		if(s.length() == 1)
			return s.toUpperCase();
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
	
	public static boolean isBlank(String str) {
		return str == null || str.equals("");
	}
	
	public static Locale toLocale(String str) {
		String lang = null;
		String country = null;
		String variant = null;
		int p = str.indexOf("_");
		if(p == -1) {
			return new Locale(str);
		}
		lang = str.substring(0, p);
		country = str.substring(p+1);
		p = country.indexOf("_");
		if(p == -1) {
			return new Locale(lang, country);
		}
		variant = country.substring(p+1);
		country = country.substring(0,p);
		return new Locale(lang, country, variant);
	}
}
