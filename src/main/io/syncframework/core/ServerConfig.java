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

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration class for the server.properties file.
 * @author dfroz
 */
public class ServerConfig extends Config {
	private static final long serialVersionUID = 3938816221618284519L;
	private String listenAddress;
	private Integer listenPort;
	private Boolean trustedProxyMode;
	
	public void load(InputStream is) throws IOException {
		super.load(is);
		listenAddress = getString("listen.address");
		listenPort = getInt("listen.port", 8080);
		trustedProxyMode = getBoolean("trusted.proxy.mode", false);
	}

	public String getListenAddress() {
		return listenAddress;
	}

	public void setListenAddress(String listenAddress) {
		this.listenAddress = listenAddress;
	}

	public Integer getListenPort() {
		return listenPort;
	}

	public void setListenPort(Integer listenPort) {
		this.listenPort = listenPort;
	}

	public Boolean getTrustedProxyMode() {
		return trustedProxyMode;
	}

	public void setTrustedProxyMode(Boolean trustedProxyMode) {
		this.trustedProxyMode = trustedProxyMode;
	}
}
