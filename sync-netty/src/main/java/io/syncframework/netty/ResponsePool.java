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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.syncframework.core.ObjectPool;
import io.syncframework.core.Response;

/**
 * 
 * @author dfroz
 *
 */
public class ResponsePool implements ObjectPool<Response> {
	private static Response pool[];
	private static Map<Response,Response> inuse = new ConcurrentHashMap<Response,Response>();
	
	public ResponsePool() {
		pool = new Response[100];
		for(int i=0; i < pool.length; i++) {
			pool[i] = new Response();
		}
	}
	
	@Override
	public Response acquire() {
		synchronized(pool) {
			for(int i=0; i < pool.length; i++) {
				if(!inuse.containsKey(pool[i])) {
					inuse.put(pool[i], pool[i]);
					return pool[i];
				}
			}
		}
		return new Response();
	}

	@Override
	public void release(Response t) {
		t.recycle();
		inuse.remove(t);
	}
}
