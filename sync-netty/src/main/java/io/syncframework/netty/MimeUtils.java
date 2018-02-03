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
package io.syncframework.netty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dfroz
 *
 */
public class MimeUtils {
	private static final Logger log = LoggerFactory.getLogger(MimeUtils.class);
	/**
	 * maps to mimes. Key = file extension. Value = mime-type.
	 */
	private static final Map<String,String> mimes = new HashMap<String,String>();
	private static final String DEFAULT_MIME = "application/octet-stream";
	
	/**
	 * Initializing the mimes map with most used / known mime types.
	 */
	public static void init() {
		mimes.put("3gpp", "video/3gpp");
		mimes.put("3gp", "video/3gpp");
		mimes.put("ai", "application/postscript");
		mimes.put("atom", "application/atom+xml");
		mimes.put("asf", "video/x-ms-asf");
		mimes.put("asx", "video/x-ms-asf");
		mimes.put("avi", "video/x-msvideo");
		mimes.put("bmp", "image/x-ms-bmp");
		mimes.put("css", "text/css");
		mimes.put("doc", "application/msword");
		mimes.put("ear", "application/java-archive");
		mimes.put("eps", "application/postscript");
		mimes.put("flv", "video/x-flv");
		mimes.put("gif", "image/gif");
		mimes.put("htm", "text/html");
		mimes.put("html", "text/html");
		mimes.put("ico", "image/x-icon");
		mimes.put("jar", "application/java-archive");
		mimes.put("jng", "image/x-jng");
		mimes.put("jnlp", "application/x-java-jnlp-file");
		mimes.put("jpeg", "image/jpeg");
		mimes.put("jpg", "image/jpeg");
		mimes.put("js", "application/javascript");
		mimes.put("json", "application/json");
		mimes.put("mid", "audio/midi");
		mimes.put("midi", "audio/midi");
		mimes.put("m4a", "audio/x-m4a");
		mimes.put("m4v", "video/x-m4v");
		mimes.put("mp3", "audio/mpeg");
		mimes.put("mp4", "video/mp4");
		mimes.put("mpeg", "video/mpeg");
		mimes.put("mpg", "video/mpeg");
		mimes.put("mov", "video/quicktime");
		mimes.put("ogg", "audio/ogg");
		mimes.put("pdf", "application/pdf");
		mimes.put("pl", "application/x-perl");
		mimes.put("png", "image/png");
		mimes.put("ppt", "application/vnd.ms-powerpoint");
		mimes.put("pptx", "application/vnd.ms-powerpoint");
		mimes.put("ps", "application/postscript");
		mimes.put("ra", "audio/x-realaudio");
		mimes.put("rss", "application/rss+xml");
		mimes.put("rtf", "application/rtf");
		mimes.put("shtml", "text/html");
		mimes.put("svg", "image/svg+xml");
		mimes.put("svgz", "image/svg+xml");
		mimes.put("tif", "image/tiff");
		mimes.put("tiff", "image/tiff");
		mimes.put("ts", "video/mp2t");
		mimes.put("xls", "application/vnd.ms-excel");
		mimes.put("xlsx", "application/vnd.ms-excel");
		mimes.put("xml", "text/xml");
		mimes.put("zip", "application/zip");
		mimes.put("war", "application/java-archive");
		mimes.put("webm", "video/webm");
		mimes.put("wmv", "video/x-ms-wmv");
	}
	
	/**
	 * Initialize mimes map with the information from the mapsfile. The parser utilized will parse the file with the following format:
	 * <SP>content-type<SP|TAB>extension1[<SP>extension2[<SP>extension3[...]]];<LF>
	 * 
	 * @param mapsfile
	 * @throws Exception
	 */
	public static void init(File mapsfile) throws Exception {
		Scanner scanner = new Scanner(mapsfile);
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			Pattern p = Pattern.compile("(\\S+)");
			Matcher m = p.matcher(line);
			
			String value = null; 
			boolean isfirst = true;
			while(m.find()) {
				if(isfirst) {
					value = m.group(1);
					isfirst = false;
					continue;
				}
				String ext = m.group(1);
				int l = ext.lastIndexOf(';');
				if(l != -1) {
					ext = ext.substring(0, l);
				}
				mimes.put(ext.toLowerCase(), value);
			}
		}
		scanner.close();
	}
	
	public static String getContentType(File file) {
		String name = file.getName();
		int p = name.lastIndexOf('.');
		if(p == -1)
			return DEFAULT_MIME;
		
		String extension = name.substring(p+1).toLowerCase();
		String mime = mimes.get(extension);
		if(mime == null)
			mime = DEFAULT_MIME;
		
		if(log.isTraceEnabled())
			log.trace("getContentType(): file: {}; content-type: {}", file, mime);
		
		return mime;
	}
}
