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
package hello;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.syncobjects.as.api.RenderResult;
import com.syncobjects.as.api.Result;


/**
 * This Test Unit ensures MainController will work as expected.
 * 
 * @author dfroz
 */

public class MainControllerTest {
	private MainController controller;
	
	/**
	 * Each @Controller at SAS are created upon the request. This simple method ensures that the production behavior is 
	 * the same in your test cases.
	 */
	@Before
	public void before() {
		controller = new MainController();
	}
	
	/**
	 * This test case covers the @Action main() from the MainController.
	 * Very simple test case where the MainController returns RenderResult pointing to /main.ftl template.
	 * Template is available under the app/private directory/folder.
	 */
	@Test
	public void main() {
		Result r = controller.main();
		Assert.assertTrue(r instanceof RenderResult);
		RenderResult render = (RenderResult)r;
		Assert.assertTrue(render.getTemplate().equals("/main.ftl"));
	}
}
