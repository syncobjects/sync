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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.syncframework.Globals;
import io.syncframework.core.Application;
import io.syncframework.core.ApplicationManager;
import io.syncframework.core.Server;
import io.syncframework.core.ServerConfig;
import io.syncframework.util.SarUtils;

/**
 * All App Server implementation is here.
 * @author dfroz
 *
 */
public class ServerImpl implements Server {
	private static final Logger log = LoggerFactory.getLogger(ServerImpl.class);
	private final ServerConfig config = new ServerConfig();
	private List<Application> applications;
	private static final String name = "Netty";

	@Override
	public ServerConfig config() {
		return config;
	}

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
				System.err.println(this+" has failed to configure logback: "+je.getMessage());
				je.printStackTrace();
				System.exit(1);
			}
		}

		if(log.isInfoEnabled())
			log.info("{} initializing using directory {}", this, basedir);

		File mimesfile = new File(basedir, "conf"+File.separator+"mime.types");
		if(mimesfile.exists()) {
			if(log.isTraceEnabled())
				log.trace("{} loading mimes from {}", this, mimesfile.getAbsolutePath());
			try { MimeUtils.init(mimesfile); }
			catch(Exception e) {
				log.error("{} has failed to initialize mimes.type file: {}", this, mimesfile, e);
				System.exit(1);
			}
		}
		else {
			if(log.isTraceEnabled())
				log.trace("{} loading default/known mime types.", this);
			MimeUtils.init();
		}

		// init configuration
		File configFile = new File(basedir, Globals.SERVER_PROPERTIES);
		try {
			FileInputStream fis = new FileInputStream(configFile);
			config.load(fis);
		}
		catch(FileNotFoundException e) {
			log.error("{} has failed to locate {}", this, configFile);
			System.exit(1);
		}
		catch(IOException e) {
			log.error("{} has failed to read configuration file {}", this, configFile);
			System.exit(1);
		}

		File appdir = new File(basedir, Globals.APPLICATIONS_DIRNAME);
		if(!appdir.isDirectory()) {
			System.err.println("directory "+appdir.getAbsolutePath()+" does not exist");
			System.exit(1);
		}
		// scan for .sar files
		File files[] = appdir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if(f.getName().endsWith(".sar"))
					return true;
				return false;
			}
		});
		// unpacking sar files...
		for(File sar: files) {
			try {
				if(log.isInfoEnabled())
					log.info("deploying SAR {}", sar.getName());
				SarUtils.unpack(sar, appdir);
			}
			catch(Exception e) {
				log.error("failed to uncompress sar file {}: {}", sar.getName(), e);
			}
		}
		// scan for directories
		files = appdir.listFiles(new FileFilter() {
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
				ApplicationManager.register(application);
				applications.add(application);
			}
			catch(Throwable t) {
				log.error("{} has failed to initialize application: {}", this, application);
				log.error("exception caught: ", t);
				log.error("invalidating {} until the problem is fixed", application);
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread("shutdown") {
			public void run() {
				if(log.isInfoEnabled())
					log.info("{} is initializing graceful shutdown", this);

				// stopping process
				for(Application application: applications) {
					try {
						if(log.isInfoEnabled())
							log.info("stopping {}", application);
						application.stop();
					}
					catch(Exception e) {
						log.error("{} failed to stop {}: ", this, application, e);
					}
				}

				if(log.isInfoEnabled())
					log.info("@Applications stopped. Goodbye!");
			}
		});
		
		if(log.isInfoEnabled())
			log.info("{} starting network", this);

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ServerInitializer(this));
			try {
				ChannelFuture chf = null;
				if(config.getListenAddress() != null)
					chf = b.bind(config.getListenAddress(), config.getListenPort());
				else
					chf = b.bind(config.getListenPort());
				if(log.isInfoEnabled())
					log.info("{} accepting connections...", this);
				Channel ch = chf.sync().channel();
				ch.closeFuture().sync();
			}
			catch(Exception e) {
				log.error("{} has failed to bind to socket: ", this, e);
				System.exit(1);
			}
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Server ").append(name);
		return sb.toString();
	}
}
