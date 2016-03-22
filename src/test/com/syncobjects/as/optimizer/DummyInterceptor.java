package com.syncobjects.as.optimizer;

import com.syncobjects.as.api.Interceptor;
import com.syncobjects.as.api.Result;

@Interceptor
public class DummyInterceptor {
	public Result before() {
		return null;
	}
	public Result after() {
		return null;
	}
}
