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
package com.syncobjects.as.netty;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.Globals;
import com.syncobjects.as.core.Application;
import com.syncobjects.as.core.ApplicationManager;
import com.syncobjects.as.core.Server;
import com.syncobjects.as.core.ServerConfig;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * All App Server implementation is here.
 * @author dfroz
 *
 */
public class ServerImpl implements Server {
	private static final Logger log = LoggerFactory.getLogger(ServerImpl.class);
	private final ServerConfig config = new ServerConfig();
	private List<Application> applications;
	
	@Override
	public void init() {
		String basedir = System.getProperty(Globals.SYNC_BASE);
		
		//
		// configure & initialize logging subsystem with logback
		//
		File logbackfile = new File(basedir, "logback.xml");
		if(logbackfile.exists() && logbackfile.isFile()) {
			try {
				LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				context.reset();
				configurator.doConfigure(logbackfile);
			}
			catch(JoranException je) {
				System.err.println("failed to configure logback: "+je.getMessage());
				je.printStackTrace();
				System.exit(1);
			}
		}
		
		if(log.isInfoEnabled())
			log.info("initializing Sync AS with Netty using directory {}", basedir);
		
		File mimesfile = new File(basedir, "conf"+File.separator+"mime.types");
		if(mimesfile.exists()) {
			if(log.isTraceEnabled())
				log.trace("loading mimes from {}", mimesfile.getAbsolutePath());
			try { MimeUtils.init(mimesfile); }
			catch(Exception e) {
				log.error("failed to initialize mimes.type file: {}", mimesfile, e);
				System.exit(1);
			}
		}
		else {
			if(log.isTraceEnabled())
				log.trace("loading default/known mime types.");
			MimeUtils.init();
		}
		
		// init configuration
		File configFile = new File(basedir, Globals.SERVER_PROPERTIES);
		try {
			FileInputStream fis = new FileInputStream(configFile);
			config.load(fis);
		}
		catch(FileNotFoundException e) {
			throw new RuntimeException(configFile+" does not exist");
		}
		catch(IOException e) {
			throw new RuntimeException("failed to read configuration from "+configFile, e);
		}
		
		File appdir = new File(basedir, Globals.APPLICATIONS_DIRNAME);
		File files[] = appdir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if(f.isDirectory())
					return true;
				return false;
			}
		});
		
		applications = new LinkedList<Application>();
		for(File dir: files) {
			Application application = new Application(dir);
			try {
				application.start();
			}
			catch(Exception e) {
				log.error("failed to initialize application: {}", application);
				log.error("exception caught: ", e);
			}
			ApplicationManager.register(application);
			applications.add(application);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread("shutdown") {
			public void run() {
				if(log.isInfoEnabled())
					log.info("initializing graceful shutdown");
				
				// stopping process
				for(Application application: applications) {
					try {
						if(log.isInfoEnabled())
							log.info("stopping application {}", application);
						application.stop();
					}
					catch(Exception e) {
						log.error("failed to stop application {}: ",application, e);
					}
				}
				
				if(log.isInfoEnabled())
					log.info("Applications stopped. Goodbye!");
			}
		});
		
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ServerInitializer());
            try {
            	ChannelFuture chf = null;
            	if(config.getListenAddress() != null)
            		chf = b.bind(config.getListenAddress(), config.getListenPort());
            	else
            		chf = b.bind(config.getListenPort());
	            Channel ch = chf.sync().channel();
	            ch.closeFuture().sync();
            }
            catch(Exception e) {
            	log.error("failed to bind to socket: ", e);
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
}
