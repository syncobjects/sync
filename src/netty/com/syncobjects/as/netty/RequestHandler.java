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

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.DATE;
import static io.netty.handler.codec.http.HttpHeaderNames.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaderNames.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.FileResult;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.core.Application;
import com.syncobjects.as.core.ApplicationManager;
import com.syncobjects.as.core.ControllerBean;
import com.syncobjects.as.core.ControllerBeanException;
import com.syncobjects.as.core.ControllerFactory;
import com.syncobjects.as.core.InterceptorBean;
import com.syncobjects.as.core.InterceptorBeanException;
import com.syncobjects.as.core.InterceptorFactory;
import com.syncobjects.as.core.Response;
import com.syncobjects.as.core.Session;
import com.syncobjects.as.core.SessionFactory;
import com.syncobjects.as.responder.Responder;
import com.syncobjects.as.responder.ResponderFactory;
import com.syncobjects.as.util.ExceptionUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

/**
 * Main request handler class. Please note that this class handles partial requests, so handling both small requests 
 * and file upload requests.
 * 
 * @author dfroz
 */
public class RequestHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	private static final int HTTP_CACHE_SECONDS = 60;
	private HttpRequest request;
	private final RequestWrapper requestWrapper = new RequestWrapper();
	private final Response response = new Response();
	private HttpPostRequestDecoder decoder;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(64 * 1024);

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if(log.isTraceEnabled())
			log.trace("channelInactive()");
		if (decoder != null) {
			decoder.cleanFiles();
			decoder.destroy();
		}
		reset();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {
		// if(msg instanceof LastHttpContent) {
		// just ignore... request response are long gone...
		//	return;
		// }

		if(msg instanceof HttpRequest) {
			this.request = (HttpRequest)msg;

			boolean isGET = this.request.method().equals(HttpMethod.GET);
			boolean xsc = this.request.headers().getAsString("X-Sync-Client") != null ? true: false;
			if(log.isTraceEnabled())
				log.trace("Is X-Sync-Client present? {}", xsc);


			// check if GET or POST ... 
			// if GET it is possible that we may handle a static file request. 
			// In this case we won't need to create Request Response wrappers
			// this will only happen if there is no webserver front-ending the solution... 
			// in this case we shall always treat the requests with the dynamic module.
			if(isGET) {
				if(xsc) {
					if(handleRequestDynamically(ctx)) {
						// no need to continue as the action has been taken by the application
						return;
					}
				}
				else {
					if(handleRequestStatically(ctx)) {
						// no need to continue as the static file has been served
						return;
					}
					if(handleRequestDynamically(ctx)) {
						// no need to continue as the page has been delivered
						return;
					}
				}
				sendFileNotFound(ctx);
				return;
			}

			// treating POST requests...
			// posts may come with multiples reads... chunks or multipart...
			// utilize auxiliary requestWrapper.
			try {
				decoder = new HttpPostRequestDecoder(factory, request, HttpConstants.DEFAULT_CHARSET);
			} catch (ErrorDataDecoderException e1) {
				// e1.printStackTrace();
				log.error("failed to decode HTTP post request", e1);
				sendError(ctx, HttpResponseStatus.BAD_REQUEST);
				ctx.channel().close();
				return;
			}			
		}

		if(decoder != null) {
			if(msg instanceof HttpContent) {
				HttpContent chunk = (HttpContent)msg;
				try {
					decoder.offer(chunk);
				} catch(Exception e) {
					log.error("failed to decode HTTP post request", e);
					sendError(ctx, HttpResponseStatus.BAD_REQUEST);
					ctx.channel().close();
					return;
				}

				try {
					readHttpDataChunkByChunk();
				}
				catch(Exception e) {
					sendException(ctx, e);
				}

				// example of reading only if at the end
				if (chunk instanceof LastHttpContent) {
					if(log.isTraceEnabled())
						log.trace("last chunk identified; handling request");
					if(handleRequestDynamically(ctx)) {
						// no need to continue as the action has been taken by the application
						return;
					}
					sendFileNotFound(ctx);
					return;
				}
			}
		}
		else {
			sendFileNotFound(ctx);
			return;
		}
	}

	/**
	 * Example of reading request by chunk and getting values from chunk to chunk
	 */
	private void readHttpDataChunkByChunk() throws Exception {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {
						if(log.isTraceEnabled())
							log.trace("data[{}]", data);
						// new value
						if (data.getHttpDataType() == HttpDataType.Attribute) {
							Attribute attribute = (Attribute) data;
							String value = attribute.getValue();
							Map<String, List<String>> parameters = requestWrapper.getParameters();
							List<String> values = parameters.get(attribute.getName());
							if(values == null)
								values = new LinkedList<String>();
							values.add(value);
							parameters.put(attribute.getName(), values);
						}
						else if(data.getHttpDataType() == HttpDataType.FileUpload) {
							FileUpload fileUpload = (FileUpload) data;

							if(fileUpload.getFilename() == null || fileUpload.getFilename().equals("")) {
								if(log.isTraceEnabled())
									log.trace("fileupload filename is null or empty; returning");
								decoder.removeHttpDataFromClean(fileUpload);
								return;
							}

							if (fileUpload.isCompleted()) {

								// in this case we shall place the file to the <application>/tmp directory

								String domain = this.request.headers().getAsString(HttpHeaderNames.HOST);
								if(log.isTraceEnabled())
									log.trace("request host: {}", domain);
								int p = domain.indexOf(':');
								if(p != -1)
									domain = domain.substring(0, p);
								Application application = ApplicationManager.getApplication(domain);
								if(application == null) {
									throw new RuntimeException("no application found responsible for domain "+domain);
								}

								String applicationHome = (String)application.getContext().get(ApplicationContext.HOME);
								String tmpDirectoryPath = applicationHome + File.separator + "tmp";
								File tmpfile = new File(tmpDirectoryPath, fileUpload.getFilename());

								if(fileUpload.isInMemory()) {
									if(!fileUpload.renameTo(tmpfile)) {
										throw new RuntimeException("failed to place the upload file under the directory: "+tmpDirectoryPath);
									}
								}
								else {
									if(!fileUpload.getFile().renameTo(tmpfile)) {
										decoder.removeHttpDataFromClean(fileUpload);
										throw new RuntimeException("failed to place the upload file under the directory: "+tmpDirectoryPath);
									}
								}
								
								com.syncobjects.as.api.FileUpload fu = new com.syncobjects.as.api.FileUpload();
								fu.setName(fileUpload.getFilename());
								fu.setType(fileUpload.getContentType());
								fu.setFile(new File(tmpDirectoryPath, fileUpload.getFilename()));								
								requestWrapper.getFiles().put(fileUpload.getName(), fu);
							} else {
								log.error("file yet to be completed but should not");
							}
						}
						else {
							log.warn("why is falling into this category ?");
						}
					}
					finally {
						data.release();
					}
				}
			}
		}
		catch(EndOfDataDecoderException ignore) {
			// just ignore
		}
	}

	private boolean handleRequestStatically(ChannelHandlerContext ctx) throws Exception {
		if(log.isTraceEnabled())
			log.trace("handling request statically");

		String domain = getDomain(request);

		//
		// verify application
		//
		Application application = ApplicationManager.getApplication(domain);
		if(application == null) {
			if(log.isTraceEnabled())
				log.trace("no application found responsible for domain: {}", domain);
			sendFileNotFound(ctx);
			return true;
		}
		
		if(log.isTraceEnabled()) {
			log.trace("handling request to: {}:{}", application, request.uri());
		}

		String path = getUriPath(request.uri());
		if(path == null) {
			// send file not found
			if(log.isInfoEnabled())
				log.info("{}: no path found for request uri: {}", application, request.uri());
			return false;
		}

		File file = new File(application.getConfig().getPublicDirectory(), path);
		
		response.setApplication(application);
		response.setFile(file);
		
		return sendFile(ctx, response);
	}

	private boolean handleRequestDynamically(ChannelHandlerContext ctx) {
		if(log.isTraceEnabled())
			log.trace("handling request dynamically");

		String domain = getDomain(request);
		// this point forward we translate the request to sync.Request...
		requestWrapper.setRequest(this.request);
		//
		// setting request context
		//
		requestWrapper.getRequestContext().put(RequestContext.DOMAIN, domain);
		requestWrapper.getRequestContext().put(RequestContext.URL, this.request.uri());
		if(this.request.headers() != null && this.request.headers().getAsString("X-Sync-Client") != null) {
			requestWrapper.getRequestContext().put(RequestContext.REMOTE_ADDRESS, 
					this.request.headers().getAsString("X-Sync-Client"));
		}
		else {			
			InetSocketAddress isa = (InetSocketAddress)ctx.channel().remoteAddress();
			requestWrapper.getRequestContext().put(RequestContext.REMOTE_ADDRESS, isa.getHostString()+":"+isa.getPort());
		}
		requestWrapper.getRequestContext().put(RequestContext.METHOD, this.request.method().asciiName());

		try {
			Application application = ApplicationManager.getApplication(domain);
			if(application == null) {
				if(log.isTraceEnabled())
					log.trace("no application found responsible for domain: {}", domain);
				sendFileNotFound(ctx);
				return true;
			}

			Thread.currentThread().setContextClassLoader(application.getClassLoader());

			final ControllerBean controller = new ControllerBean();
			ControllerFactory controllerFactory = application.getControllerFactory();
			if(!controllerFactory.find(controller, request.uri())) {
				if(log.isTraceEnabled())
					log.trace("no @Controller found to handle request: {}", request.uri());
				// lead to not found
				return false;
			}

			SessionFactory sessionFactory = application.getSessionFactory();
			if(sessionFactory == null) {
				log.error("application malfunction detected; SessionFactory is null");
				sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				return true;
			}

			ResponderFactory responderFactory = application.getResponderFactory();
			if(responderFactory == null) {
				log.error("application malfunction detected; ResponderFactory is null");
				sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				return true;
			}

			Session session = sessionFactory.find(requestWrapper);
			requestWrapper.setSession(session);
			response.setSession(session);
			response.setApplication(application);

			InterceptorFactory interceptorFactory = application.getInterceptorFactory();
			InterceptorBean interceptors[] = interceptorFactory.find(controller.interceptedBy());
			for(int i=0; i < interceptors.length; i++) {
				//
				// default action is to return null; if not null, direct to response and end the req/resp cycle
				//
				Result interceptorResult = interceptors[i].before(requestWrapper, response);
				if(interceptorResult != null) {
					//
					// find the responder which will handle the result. populate data using the response object.
					//
					Responder responder = responderFactory.find(interceptorResult);
					if(responder == null) {
						log.error("no responder encountered to handle result: "+interceptorResult);
						sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
						return true;
					}
					responder.respond(response, interceptors[i], interceptorResult);
					if(log.isTraceEnabled())
						log.trace(interceptors[i]+".before() returned result: "+interceptorResult);
					if(interceptorResult instanceof FileResult)
						return sendFile(ctx, response);
					else
						return sendResponse(ctx, response);
				}
			}

			//
			// controller.action()
			//
			Result result = controller.action(requestWrapper, response);
			if(result == null) {
				log.error("@Controller "+controller+" has no @Action defined to handle request: "+requestWrapper.getUri());
				// not found controller's action
				return false;
			}
			if(log.isTraceEnabled())
				log.trace(controller+" returned result: "+result);

			// interceptors after()
			for(int i=0; i < interceptors.length; i++) {
				//
				// default action is to return null; if not null, direct to response and end the req/resp cycle
				//
				Result interceptorResult = interceptors[i].after(requestWrapper, response);
				if(interceptorResult != null) {
					//
					// find the responder which will handle the result. populate data using the response object.
					//
					Responder responder = responderFactory.find(interceptorResult);
					if(responder == null) {
						log.error("no responder encountered to handle result: "+interceptorResult);
						sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
						return true;
					}
					if(log.isTraceEnabled())
						log.trace(interceptors[i]+".after() returned result: "+interceptorResult);
					responder.respond(response, interceptors[i], interceptorResult);
					if(interceptorResult instanceof FileResult)
						return sendFile(ctx, response);
					else
						return sendResponse(ctx, response);
				}
			}

			//
			// find the responder which will handle the result. populate data using the response object.
			//
			Responder responder = responderFactory.find(result);
			if(responder == null) {
				log.error("no responder encountered to handle result: "+result);
				sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				return true;
			}
			
			responder.respond(response, controller, result);
			
			if(log.isTraceEnabled())
				log.trace("{}: {} delivered response: {}", application, responder, result);
			
			if(result instanceof FileResult)
				return sendFile(ctx, response);
			else
				return sendResponse(ctx, response);
			
		}
		catch(Exception e) {
			sendException(ctx, e);
		}
		return true;
	}

	private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
		response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		response.headers().set(
				LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	private boolean sendResponse(ChannelHandlerContext ctx, Response response) throws Exception {
		HttpResponseStatus responseStatus = null;
		switch(response.getCode()) {
		case INTERNAL_ERROR:
			responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
			break;
		case NOT_FOUND:
			responseStatus = HttpResponseStatus.NOT_FOUND;
			break;
		case PERMANENT_REDIRECT:
			responseStatus = HttpResponseStatus.MOVED_PERMANENTLY;
			break;
		case TEMPORARY_REDIRECT:
			responseStatus = HttpResponseStatus.FOUND;
			break;
		default:
			responseStatus = HttpResponseStatus.OK;
			break;
		}

		ByteArrayOutputStream bos = (ByteArrayOutputStream)response.getOutputStream();
		ByteBuf buf = copiedBuffer(bos.toByteArray());

		// Build the response object.
		FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, buf);

		httpResponse.headers().set(HttpHeaderNames.SERVER, "Sync-AS");
		httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
		
		//
		// if response has declared specific Headers, then this may or may not override the default headers
		// declared above.
		//
		if(response.getHeaders() != null) {
			if(log.isTraceEnabled())
				log.trace("custom response headers identified... passing to the response");
			for(String header: response.getHeaders().keySet()) {
				if(log.isTraceEnabled())
					log.trace("setting response header: {}: {}", header, response.getHeaders().get(header));
				httpResponse.headers().set(header, response.getHeaders().get(header));
			}
		}

		// Write the response.
		ctx.channel().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
		reset();
		
		return true;
	}
	
	private boolean sendFile(ChannelHandlerContext ctx, Response response) throws Exception {
		Application application = response.getApplication();
		if(application == null) {
			log.error("no response.application has been set");
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			return true;
		}
		
		File file = response.getFile();
		if(file == null) {
			log.error("no response.file has been set");
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			return true;
		}
		
		if(!file.exists()) {
			// file not found try request dynamically
			if(log.isDebugEnabled())
				log.debug("{}: file not found: {}", application, file);
			return false;
		}
		if(file.isHidden()) {
			if(log.isDebugEnabled()) {
				log.debug("{}: file {} is hidden; returning File Not Found", 
						application, file.getAbsolutePath());
			}
			sendFileNotFound(ctx);
			return true;
		}
		if(file.isDirectory()) {
			// once is a directory a dynamic handler can take it ...
			// even if a index.html, the @Action Controller.main() shall handle it
			return false;
		}
		//
		// Check if the file resides under the PUBLIC or PRIVATE folders. 
		// More important point for this verification is with PUBLIC requests where multiples ../../../..
		// may lead to security breach - exposing unwanted system files.
		//
		String path = file.getAbsolutePath();
		if(!path.startsWith(application.getConfig().getPublicDirectory().getAbsolutePath())
				&& !path.startsWith(application.getConfig().getPrivateDirectory().getAbsolutePath())) {
			log.error("{}: file {} returned, is not located under Public or Private folders", 
					application, file.getAbsolutePath());
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return true;
		}
		if(!file.isFile()) {
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return true;
		}

		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ignore) {
			sendError(ctx, NOT_FOUND);
			return true;
		}
		long fileLength = raf.length();

		if(log.isTraceEnabled())
			log.trace("{}: returning file: {}", application, file);

		HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpUtil.setContentLength(httpResponse, fileLength);
		httpResponse.headers().set(CONTENT_TYPE, MimeUtils.getContentType(file));
		setDateAndCacheHeaders(httpResponse, file);
		httpResponse.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
		//
		// if response has declared specific Headers, then this may or may not override the default headers
		// declared above.
		//
		if(response.getHeaders() != null) {
			if(log.isTraceEnabled())
				log.trace("custom response headers identified... passing to the response");
			for(String header: response.getHeaders().keySet()) {
				if(log.isTraceEnabled())
					log.trace("setting response header: {}: {}", header, response.getHeaders().get(header));
				httpResponse.headers().set(header, response.getHeaders().get(header));
			}
		}

		// Write the initial line and the header.
		ctx.write(httpResponse);

		// Write the content.
		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;

		sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
		lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) { // total unknown
					if(log.isTraceEnabled())
						log.trace("{}: "+future.channel() + " transfer progress: " + progress, application);
				} else {
					if(log.isTraceEnabled())
						log.trace("{}: "+future.channel() + " Transfer progress: " + progress + " / " + total, application);
				}
			}

			@Override
			public void operationComplete(ChannelProgressiveFuture future) {
				if(log.isTraceEnabled())
					log.trace("{}: "+future.channel() + " transfer complete.", application);
				if(raf != null) {
					try {  raf.close(); } catch(Exception ignore) {
						if(log.isTraceEnabled())
							log.trace("exception caught: {}", ignore);
					}
				}
			}
		});

	    // Close the connection when the whole content is written out.
		ctx.flush();
		lastContentFuture.addListener(ChannelFutureListener.CLOSE);
			
		return true;
	}

	private void sendFileNotFound(ChannelHandlerContext ctx) {
		sendError(ctx, HttpResponseStatus.NOT_FOUND);
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		reset();
	}

	private void sendException(ChannelHandlerContext ctx, Exception e) {

		if(log.isTraceEnabled())
			log.trace("delivering exception message to the client");

		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>Error</title></head>");
		sb.append("<style>");
		sb.append("body { font-size: 13px; font-family: \"Helvetica Neue\",Helvetica,Arial,\"Lucida Grande\",sans-serif; }\n");
		sb.append("ul { list-style-type: none }\n");
		sb.append(".code { margin: 10px 0px 10px 0px; padding: 10px; border: 1px solid #c0c0c0; background: #f0f000 }\n");
		sb.append("</style>");
		sb.append("<body>");

		if(e instanceof ControllerBeanException) {
			ControllerBeanException cbe = (ControllerBeanException)e;
			sb.append("Exception caught while executing @Controller ").append(cbe.getController()).append("<br/>\n");
			sb.append("<div class=\"code\">");
			sb.append(ExceptionUtils.printStackTraceHtml(e));
			sb.append("</div>");
			log.error("Exception caught while executing @Controller {}", cbe.getController());
			log.error(ExceptionUtils.printStackTrace(e));
		}
		else if(e instanceof InterceptorBeanException) {
			InterceptorBeanException ibe = (InterceptorBeanException)e;
			sb.append("Exception caught while executing @Interceptor ").append(ibe.getInterceptor()).append("<br/>\n");
			sb.append("<div class=\"code\">");
			sb.append(ExceptionUtils.printStackTraceHtml(e));
			sb.append("</div>");
			log.error("Exception caught while executing @Interceptor {}", ibe.getInterceptor());
			log.error(ExceptionUtils.printStackTrace(e));
		}
		else {
			sb.append("Exception caught while executing request<br/>\n");
			sb.append("<div class=\"code\">");
			sb.append(ExceptionUtils.printStackTraceHtml(e));
			sb.append("</div>");
			log.error(ExceptionUtils.printStackTrace(e));
		}
		sb.append("</body></html>");

		ByteBuf buf = copiedBuffer(sb.toString().getBytes());

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buf);
		response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		reset();
	}

	private static String getDomain(HttpRequest request) {
		String domain = request.headers().getAsString(HttpHeaderNames.HOST);
		int p = domain.indexOf(':');
		if(p != -1)
			domain = domain.substring(0, p);
		return domain;
	}

	private static String getUriPath(String uri) {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		if (uri.isEmpty() || uri.charAt(0) != '/') {
			return null;
		}

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);
		
		//
		// remove parameters from the URL
		//
		int p = uri.indexOf('?');
		if(p != -1) {
			uri = uri.substring(0, p);
		}

		return uri;
	}

	private void reset() {
		if(log.isTraceEnabled())
			log.trace("reset()");
		request = null;
		
		if(requestWrapper != null) {
			if(log.isTraceEnabled())
				log.trace("reset(): RequestWrapper.recycle()");
			requestWrapper.recycle();
		}
		
		// destroy the decoder to release all resources
		if(decoder != null) {
			decoder.destroy();
			decoder = null;
		}
	}

	@Override
	public void finalize() {
		reset();
	}
}