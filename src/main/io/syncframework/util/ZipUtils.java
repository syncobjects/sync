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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dfroz
 *
 */
public class ZipUtils {
	private static final Logger log = LoggerFactory.getLogger(ZipUtils.class);
	private static ZipFile zipFile;

	private static void listFiles(List<File> list, File dir) {
		for(File f: dir.listFiles()) {
			if(f.isDirectory()) {
				listFiles(list, f);
				continue;
			}
			list.add(f);
		}
	}

	public static void compress(File srcdir, File destfile) throws Exception {
		if(!srcdir.isDirectory())
			throw new IllegalArgumentException(srcdir.getAbsolutePath()+" is not directory");

		byte buffer[] = new byte[4*1024];
		int n = 0;

		BufferedInputStream bis = null;
		FileOutputStream fos = new FileOutputStream(destfile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(fos));

		List<File> files = new LinkedList<File>();
		listFiles(files, srcdir);

		for (int i=0; i < files.size(); i++) {
			File f = files.get(i);
			if(log.isTraceEnabled())
				log.trace("compressing file: "+f.getAbsolutePath());
			FileInputStream fi = new FileInputStream(f);
			bis = new  BufferedInputStream(fi, buffer.length);
			ZipEntry entry = new ZipEntry(f.getPath());
			out.putNextEntry(entry);
			while((n = bis.read(buffer, 0, buffer.length)) >= 0)
				out.write(buffer, 0, n);
			bis.close();
		}
		out.close();
		fos.close();
	}

	public static void uncompress(File srcfile, File destdir) throws Exception {
		if(!destdir.isDirectory() && !destdir.mkdir()) {
			throw new IllegalArgumentException(destdir.getAbsolutePath()+
					" is not directory and could not be created");
		}

		byte buffer[] = new byte[4*1024];
		int n = 0;

		zipFile = new ZipFile(srcfile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while(entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if(entry.isDirectory()) {
				File dir = new File(destdir, entry.getName());
				if(!dir.isDirectory()) {
					if(log.isTraceEnabled())
						log.trace("creating directory: "+dir);
					if(!dir.mkdirs())
						throw new RuntimeException("failed to create directory: "+dir.getAbsolutePath());
				}
				continue;
			}
			File destfile = new File(destdir, entry.getName());
			File parent = destfile.getParentFile(); 
			if(!parent.isDirectory()) {
				if(log.isTraceEnabled())
					log.trace("creating directory: "+parent);
				if(!parent.mkdirs())
					throw new RuntimeException("failed to create directory: "+parent.getAbsolutePath());
			}
			
			if(log.isTraceEnabled())
				log.trace("uncompressing file: "+destfile.getAbsolutePath());
			
			InputStream is = zipFile.getInputStream(entry);
			OutputStream os = new BufferedOutputStream(new FileOutputStream(destfile));
			while((n = is.read(buffer, 0, buffer.length)) >= 0)
				os.write(buffer, 0, n);
			os.close();
			is.close();
		}
		
		zipFile.close();
	}
}
