package io.syncframework.optimizer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ControllerOptimizerTest.class,
	InterceptorOptimizerTest.class,
	InitializerOptimizerTest.class
})
public class MainTest {
}
