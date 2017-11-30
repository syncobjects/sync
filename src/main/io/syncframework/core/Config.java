/*
 * Copyright 2012-2017 SyncObjects Ltda.
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

import java.util.Properties;

/**
 * @author dfroz
 */
public class Config extends Properties {
	private static final long serialVersionUID = -5646816006054590490L;
	
	public boolean getBoolean(String key, boolean defaultValue) {
		String value = getProperty(key);
		if(value != null) {
			value = value.trim();
			return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enabled");
		}
		else {
			setProperty(key, Boolean.toString(defaultValue));
			return defaultValue;
		}
	}
	
	public long getLong(String key, long defaultValue) {
		String value = getProperty(key);
		if(value != null)
			return Long.parseLong(value.trim());
		else {
			setProperty(key, Long.valueOf(defaultValue).toString());
			return defaultValue;
		}
	}
	
	public int getInt(String key, int defaultValue) {
		String value = getProperty(key);
		if(value != null)
			return Integer.parseInt(value.trim());
		else {
			setProperty(key, Integer.valueOf(defaultValue).toString());
			return defaultValue;
		}
	}
	
	public String getString(String key) {
		return getProperty(key);
	}
	
	public String getString(String key, String defaultValue) {
		String value = getProperty(key, defaultValue);
		if(defaultValue != null) {
			if(value == null || value.equals(defaultValue))
				setProperty(key, defaultValue);
		}
		return value.trim();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Object key: this.keySet()) {
			sb.append("\t").append(key).append(": ").append(this.get(key)).append("\n");
		}
		return sb.toString();
	}
}