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
package io.syncframework.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dfroz
 *
 */
public class MimeType {
	private static final Logger log = LoggerFactory.getLogger(MimeType.class);
	public static final Map<String,String> mimes = new HashMap<String,String>();
	
	static {
		mimes.put("*", "application/octet-stream");
		mimes.put("3gpp", "video/3gpp");
		mimes.put("3gp", "video/3gpp");
		mimes.put("atom", "application/atom+xml");
		mimes.put("bin", "application/octet-stream");
		mimes.put("css", "text/css");
		mimes.put("html", "text/html");
		mimes.put("htm", "text/html");
		mimes.put("flv", "video/x-flv");
		mimes.put("gif", "image/gif");
		mimes.put("jpeg", "image/jpeg");
		mimes.put("jpg", "image/jpeg");
		mimes.put("jnlp", "application/x-java-jnlp-file");
		mimes.put("js", "application/x-javascript");
		mimes.put("m4a", "audio/x-m4a");
		mimes.put("mp3", "audio/mpeg");
		mimes.put("mp4", "video/mp4");
		mimes.put("mov", "video/quicktime");
		mimes.put("ogg", "audio/ogg");
		mimes.put("pl", "application/x-perl");
		mimes.put("png", "image/png");
		mimes.put("rtf", "application/rtf");
		mimes.put("rss", "application/rss+xml");
		mimes.put("tiff", "image/tiff");
		mimes.put("txt", "text/plain");
		mimes.put("webm", "video/webm");
		mimes.put("xhtml", "application/xhtml+xml");
	}
	
	public static void register(String extension, String mime) {
		mimes.put(extension, mime);
	}
	
	public static String get(String name) {
		int p = name.lastIndexOf('.');
		if(p == -1)
			return mimes.get("*");
		String extension = name.substring(p+1);
		String mime = mimes.get(extension);
		if(mime == null)
			mime = mimes.get("*");
		if(log.isTraceEnabled())
			log.trace("mime for name: {}; ext: {}; mime: {}", name, extension, mime);
		return mime;
	}
}
