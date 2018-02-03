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

import java.util.HashMap;
import java.util.Map;

import io.syncframework.api.Converter;

/**
 * 
 * @author dfroz
 *
 */
public class StringMapConverter implements Converter<Map<String,String>> {
	public Map<String,String> convert(String values[]) {		
		Map<String,String> map = new HashMap<String,String>();
		if(values == null || values.length == 0 || values[0].equals(""))
			return map;
		for(String v: values) {
			map.put(v, v);
		}
		return map;
	}
	public String toString() {
		return "StringMapConverter";
	}
}
