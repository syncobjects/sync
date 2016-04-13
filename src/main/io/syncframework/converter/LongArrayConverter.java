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

import io.syncframework.api.Converter;

/**
 * @author dfroz
 */
public class LongArrayConverter implements Converter<Long[]> {
	public Long[] convert(String values[]) {
		if(values == null || values.length == 0 || values[0].equals(""))
			return null;
		Long longs[] = new Long[values.length];
		for(int i=0; i < values.length; i++) {
			longs[i] = Long.valueOf(values[i]);
		}
		return longs;
	}
	public String toString() {
		return "LongArrayConverter";
	}
}
