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
package io.syncframework.i18n;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author dfroz
 *
 */
public class ResourceBundleMessageFactory implements MessageFactory {
	private final Map<Locale,ResourceBundle> resources = new ConcurrentHashMap<Locale,ResourceBundle>();
	private final Map<String,MessageFormat> formatters = new ConcurrentHashMap<String,MessageFormat>();
	private File dir;
	
	public ResourceBundleMessageFactory(File dir) {
		this.dir = dir;
	}

	public void init() throws Exception {
		if(dir == null)
			throw new RuntimeException("init called without dir");
		if(!dir.isDirectory())
			throw new RuntimeException(dir+" is not a directory");
		File files[] = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(!file.isFile())
					return false;
				String name = file.getName();
				if(!name.startsWith(MESSAGES_BASENAME))
					return false;
				if(!name.endsWith(".properties"))
					return false;
				return true;
			}
		});
		for(File file: files) {
			// get locale country variant from filename string

			ResourceBundle bundle = null;

			Locale locale = null;
			String name = file.getName();
			int istart = name.indexOf('_');
			if(istart == -1) {
				// messages.properties file
				FileInputStream fis = new FileInputStream(file);
				bundle = new PropertyResourceBundle(fis);
				resources.put(Locale.getDefault(), bundle);
				continue;
			}

			istart++;
			int iend = name.indexOf(".properties");
			String localeString = name.substring(istart, iend);

			String parts[] = localeString.split("_");
			if(parts.length == 1) {
				locale = new Locale(parts[0]);
			}
			else if(parts.length == 2) {
				locale = new Locale(parts[0], parts[1]);
			}
			else if(parts.length == 3) {
				locale = new Locale(parts[0], parts[1], parts[2]);
			}
			else {
				throw new RuntimeException("messages.properties file ("+name+") using invalid filename format");
			}

			FileInputStream fis = new FileInputStream(file);
			bundle = new PropertyResourceBundle(fis);
			resources.put(locale, bundle);
		}
	}

	public String get(Locale locale, String key, Object ... args) {		
		ResourceBundle bundle = resources.get(locale);
		// System.out.println("bundle == null? "+(bundle == null ? true: false)+": "+locale);
		if(bundle == null && locale.getVariant() != null) {
			locale = new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant());
			bundle = resources.get(locale);
		}
		if(bundle == null && locale.getCountry() != null) {
			locale = new Locale(locale.getLanguage(), locale.getCountry());
			bundle = resources.get(locale);
		}
		if(bundle == null) {
			locale = new Locale(locale.getLanguage());
			bundle = resources.get(locale);
		}
		if(bundle == null) {
			locale = Locale.getDefault();
			bundle = resources.get(locale);
		}
		if(bundle == null) {
			// no bundle found
			return key;
		}
		if(!bundle.containsKey(key)) {
			// key not defined
			return key;
		}
		
		String pattern = bundle.getString(key);

		String formatterKey = key+"-"+locale.toString();
		MessageFormat formatter = formatters.get(formatterKey);
		if(formatter == null) {
			formatter = new MessageFormat(pattern, locale);
			formatters.put(formatterKey, formatter);
		}

		String message = null;
		synchronized (formatter){
			message = formatter.format(args);
		}
		return message;
	}
}
