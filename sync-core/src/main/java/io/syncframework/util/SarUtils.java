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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on ZipUtils... does the same but creates the application directory.
 * It supports zip files compressed considering the directory structure as: <app>/<file> or <file>.
 * 
 * @author dfroz
 */
public class SarUtils {
	private static final Logger log = LoggerFactory.getLogger(SarUtils.class);

	public static void unpack(File srcfile, File outbasedir) throws Exception {
		if(!srcfile.getName().endsWith(".sar")) {
			throw new IllegalArgumentException("invalid source file");
		}

		String appname = srcfile.getName().substring(0, srcfile.getName().length() - ".sar".length());
		File appdir = new File(outbasedir, appname);		
		if(appdir.exists()) {
			//
			// First we need to delete the previous application... 
			// This guarantees that previous versions of the same application will not conflict
			// For instance... if you rename a @Controller... both the new and old copy of the same @Controller will be bound to the same 
			// URL pattern. SYNC will not distiguish this case. Therefore both versions will compete for the requests, 
			// leading to unpredictable results. Deleting the previous application will solve this problem...
			//
			if(log.isTraceEnabled())
				log.trace("deleting previous application {}", appdir.getName());
			FileUtils.delete(appdir);
		}
		
		if(log.isTraceEnabled())
			log.trace("unpacking new application {}", appdir.getName());
		
		if(!appdir.isDirectory() && !appdir.mkdir()) {
			throw new IllegalArgumentException(appdir.getAbsolutePath()+
					" is not directory and could not be created");
		}

		byte buffer[] = new byte[4*1024];
		int n = 0;

		ZipFile zipFile = new ZipFile(srcfile);
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				
				if(entry.isDirectory()) {
					File dir = null;
					if(entry.getName().startsWith(appname)) {
						dir = new File(outbasedir, entry.getName());
					}
					else {
						dir = new File(appdir, entry.getName());
					}
					if(!dir.isDirectory()) {
						if(log.isTraceEnabled())
							log.trace("creating directory: "+dir);
						if(!dir.mkdirs())
							throw new RuntimeException("failed to create directory: "+dir.getAbsolutePath());
					}
					continue;
				}
				
				File destfile = null;
				if(entry.getName().startsWith(appname)) {
					destfile = new File(outbasedir, entry.getName());
				}
				else {
					destfile = new File(appdir, entry.getName());
				}
				
				File parent = destfile.getParentFile(); 
				if(!parent.isDirectory()) {
					if(log.isTraceEnabled())
						log.trace("creating parent directory: {}", parent);
					if(!parent.mkdirs())
						throw new RuntimeException("failed to create directory: "+parent.getAbsolutePath());
				}

				if(log.isTraceEnabled())
					log.trace("uncompressing file: {}", destfile.getAbsolutePath());

				InputStream is = zipFile.getInputStream(entry);
				OutputStream os = new BufferedOutputStream(new FileOutputStream(destfile));
				while((n = is.read(buffer, 0, buffer.length)) >= 0)
					os.write(buffer, 0, n);
				os.close();
				is.close();
			}
		}
		finally {
			zipFile.close();
		}
	}
}
