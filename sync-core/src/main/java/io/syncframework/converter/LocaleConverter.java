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
package io.syncframework.converter;

import java.util.Locale;

import io.syncframework.api.Converter;

/**
 * 
 * @author dfroz
 */
public class LocaleConverter implements Converter<Locale> {
	public Locale convert(String values[]) {
		if(values == null || values.length == 0 || values[0].equals(""))
			return null;
		String v = values[0];
		int p = v.indexOf('_');
		if(p == -1)
			return new Locale(v);
		String lang = v.substring(0, p);
		v = v.substring(p+1);
		p = v.indexOf('_');
		if(p == -1)
			return new Locale(lang, v);
		String country = v.substring(0, p);
		String variant = v.substring(p+1);
		return new Locale(lang, country, variant);
	}
	public String toString() {
		return "LocaleConverter";
	}
}
