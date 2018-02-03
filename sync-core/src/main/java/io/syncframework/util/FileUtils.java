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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 
 * @author dfroz
 *
 */
public class FileUtils {
	private static final int FILE_COPY_BUFFER_SIZE = 30 * 1024;
	
	public static void copyTo(File srcFile, File destFile) throws IOException {
		if(srcFile == null)
			throw new IllegalArgumentException("srcfile");
		if(destFile == null)
			throw new IllegalArgumentException("destfile");
		
		if(destFile.isDirectory()) {
			destFile = new File(destFile, srcFile.getName());
		}
		if(srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
			throw new IOException("Source '"+srcFile+"' and destination '"+destFile+"' are the same");
		}
		File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && destFile.canWrite() == false) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            try { output.close(); } catch(Exception ignore) {}
            try { fos.close(); } catch(Exception ignore) {}
            try { input.close(); } catch(Exception ignore) {}
            try { fis.close(); } catch(Exception ignore) {}
        }
	}
	
	public static void delete(File file) {
		if(file == null)
			throw new IllegalArgumentException("file");
		
		if(file.isDirectory()) {
			for(File f: file.listFiles()) {
				if(f.getName().equals(".") || f.getName().equals(".."))
					continue;
				delete(f);
			}
		}
		
		if(!file.delete())
			throw new RuntimeException("failed to delete file: "+file);
	}
	
	public static String getRelativePath(File file, File basedir) {
		if(basedir == null)
			throw new IllegalArgumentException("basedir");
		if(file == null)
			throw new IllegalArgumentException("file");
		String path = file.getAbsolutePath();
		int p = path.indexOf(basedir.getAbsolutePath());
		if(p == -1)
			return path;
		return path.substring(basedir.getAbsolutePath().length() + File.separator.length()); 
	}
}
