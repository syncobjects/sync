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

/**
 * <p>URLPattern responsible for handling url patterns included in the Controllers</p>
 * 
 * Possible cases:<br/>
 * 
 * Pattern = /users/*
 * Request to /; return false
 * Request to /users; return false
 * Request to /users/; return true (matches action "main")
 * Request to /users/add; return true (matches action "add")
 * Request to /users/add.do; return true (matches action "add")
 * Request to /users/list.html;allusers,max=10; return true (matches action "list")
 * 
 * @param url
 * @author dfroz
 */
public class URLPattern {
	private String pattern;
	private boolean wildcard;

	public URLPattern() {
		this.wildcard = false;
	}
	
	public void compile(String pattern) {
		this.pattern = pattern;
		
		if(pattern == null)
			throw new IllegalArgumentException("Pattern not provided");
		if(pattern.charAt(0) != '/')
			throw new RuntimeException("Illegal pattern "+pattern+": not starting with /");

		// reset wildcard state
		wildcard = false;

		int sp = -1;
		int ap = -1;
		// System.out.println("pattern: "+pattern);
		for(int i=0; i < pattern.length(); i++) {
			char ch = pattern.charAt(i);
			
			// validating url pattern
			// only allows [*\./9-0a-zA-Z]+
			// System.out.print(ch+" ");
			if(ch != 42 && ch != 46 && ch != 47 && 
					!(ch >= 48 && ch <= 57) && !(ch >= 65 && ch <= 90)
					&& !(ch >= 97 && ch <= 122)) {
				throw new URLPatternException("unsupported pattern: "+pattern);
			}
				
			if(ch == '/') {
				sp = i;
				continue;
			}
			if(ap == -1 && ch == '*') {
				// search for wildcard
				ap = i;
				continue;
			}
		}
		// System.out.println("");
		
		if(ap != -1) {
			if(ap <= sp) {
				throw new RuntimeException("Illegal pattern "+pattern+
						": wildcard '*' cannot be used in the middle of the pattern");
			}
			wildcard = true;
			this.pattern = pattern.substring(0, ap);
		}
	}

	/**
	 * This method returns the controller's action name which will be utilized to handle the request.<br/>
	 * In the case that the controller is not responsible for the request, null is returned.
	 * In case that there is no action defined for the requested URL, "main" is returned.
	 * 
	 * @param url
	 * @return action name
	 */
	public String action(String url) {		
		if(url == null)
			throw new IllegalArgumentException("url cannot be null");

		// Special case where pattern is greater than the URL itself
		// e.g.: Pattern="/users/*", request url="/". since will never match, discarding this ASAP
		// after this URLs and Patterns may be interpreted
		if(pattern.length() > url.length()) {
			return null;
		}
		
		String action = null;
		
		// identify Last Question Pointer ('?') and remove everything after that
		// int lqp = url.lastIndexOf('?');
		// if(lqp != -1)
		//	url = url.substring(0, lqp);
		
		// Treats the URL removing the url-path instructions (everything as: [;?%_]+ etc)
		// speed up things when further comparison occurs between the URL and Pattern
		int i=0;
		for(i=0; i < url.length(); i++) {
			char ch = url.charAt(i);
			// next if . or / or 0-9
			if(ch == 46 || ch == 47 || (ch >= 48 && ch <= 57)) 
				continue;
			// next if A-Za-z
			ch = (char)(ch & ~0x20);
			if(ch >= 65 && ch <= 90)
				continue;
			break;
		}
		if(i > 0)
			url = url.substring(0, i);
		
		// Last Slash Pointer variable, index to the url last slash.
		int lsp = url.lastIndexOf('/');
		if(lsp == -1)
			throw new IllegalArgumentException("invalid url");
		
		// This is the special case  where Pattern="/*" and request url="/"
		// the @Action main() shall handle this request ... since this may happen very often... 
		// this condition has the 2nd priority
		if(wildcard) {
			if(lsp == 0 && url.length() == 1) {
				action = "main";
				return action;
			}
		}

		//
		// treating patterns with no wildcard
		//
		if(!wildcard) {
			// without wildcard utilization the requested URL and the pattern must match
			// in this case action will be returned from the suffixed url.
			if(pattern.equals(url)) {
				action = url.substring(lsp+1);
				if(action == null || action.length() == 0)
					return null;
				// guarantees that only alphanumeric characters are part of the action method
				for(i=0; i < action.length(); i++) {
					char ch = action.charAt(i);
					if(ch >= 48 && ch <= 57)
						continue;
					ch = (char)(ch & ~0x20);
					if(ch >= 65 && ch <= 90) {
						continue;
					}
					break;
				}
				return action.substring(0, i);
			}
			else {
				return null;
			}
		}

		//
		// All cases with wildcards are treated here 
		//
		
		// verify if the URL matches with the prefix
		
		// URL: /test/action.do && Pattern: /*; Last Slash Pointer (lsp) will be more than pattern
		if(lsp > pattern.length()) {
			return null;
		}
		
		// At this point if the Pattern is not part of the URL, 
		// the controller is not responsible for this request... 
		// Expensive operation but needed
		if(!url.startsWith(pattern)) {
			return null;
		}

		// action shall be the one from the Last Slash Point (lsp) to the special character (excluding .)
		action = url.substring(lsp + 1);
		if(action.length() == 0) {
			return "main";
		}
		
		for(i=0; i < action.length(); i++) {
			char ch = action.charAt(i);
			if(ch >= 48 && ch <= 57)
				continue;
			ch = (char)(ch & ~0x20);
			if(ch >= 65 && ch <= 90) {
				continue;
			}
			break;
		}
		action = action.substring(0, i);
		return action;
	}

	public String toString() {
		return pattern;
	}
}