package io.syncframework.optimizer;

import io.syncframework.api.Interceptor;
import io.syncframework.api.Result;

@Interceptor
public class DummyInterceptor {
	public Result before() {
		return null;
	}
	public Result after() {
		return null;
	}
}
