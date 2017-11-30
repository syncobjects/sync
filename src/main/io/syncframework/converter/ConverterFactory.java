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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.syncframework.api.Converter;

/**
 * @author dfroz
 */
public class ConverterFactory {
	private final Map<Class<?>, Converter<?>> converters = new ConcurrentHashMap<Class<?>, Converter<?>>();

	public ConverterFactory() {
		converters.put(Boolean.class, new BooleanConverter());
		converters.put(Boolean[].class, new BooleanArrayConverter());
		converters.put(Double.class, new DoubleConverter());
		converters.put(Double[].class, new DoubleArrayConverter());
		// TODO: converters.put(FileUpload.class, new FileUploadConverter());
		// TODO: converters.put(FileUpload[].class, new FileUploadArrayConverter());
		converters.put(Integer.class, new IntegerConverter());
		converters.put(Integer[].class, new IntegerArrayConverter());
		converters.put(Locale.class, new LocaleConverter());
		converters.put(Long.class, new LongConverter());
		converters.put(Long[].class, new LongArrayConverter());
		converters.put(String.class, new StringConverter());
		converters.put(String[].class, new StringArrayConverter());
		converters.put(Map.class, new StringMapConverter());
	}
	
	public boolean isConverter(Class<?> clazz) {
		if(clazz == null)
			throw new IllegalArgumentException("clazz");
		for(Type type: clazz.getGenericInterfaces()) {
			if(!(type instanceof ParameterizedType))
				continue;
			ParameterizedType pt = (ParameterizedType)type;
			if(pt.getRawType().equals(Converter.class))
				return true;
		}
		return false;
	}

	public Converter<?> getConverter(Class<?> clazz) {
		if(clazz == null)
			throw new IllegalArgumentException("clazz");
		return converters.get(clazz);
	}

	public Map<Class<?>, Converter<?>> getConverters() {
		return converters;
	}

	public boolean register(Class<?> clazz) {
		if(clazz == null)
			throw new IllegalArgumentException("clazz");

		for(Type type: clazz.getGenericInterfaces()) {
			if(!(type instanceof ParameterizedType))
				continue;
			ParameterizedType pt = (ParameterizedType)type;
			
			if(!pt.getRawType().equals(Converter.class))
				continue;
			// System.out.println("pt: "+pt);
			Type args[] = pt.getActualTypeArguments();
			assert args.length == 1;
			try {
				ParameterizedType ptt = (ParameterizedType)args[0];
				Class<?> c = (Class<?>)ptt.getRawType();
				converters.put(c, (Converter<?>)clazz.getDeclaredConstructor().newInstance());
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ConverterFactory [\n");
		for(Class<?> clazz: converters.keySet()) {
			sb.append("\tclass: "+clazz.getName()+", "+converters.get(clazz)+"\n");
		}
		sb.append("]");
		return sb.toString();
	}
}
