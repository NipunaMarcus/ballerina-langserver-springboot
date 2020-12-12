/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */
package org.wso2.choreo.workspace.langserver;

import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

/**
 * JSON-RPC launcher builder for use with msf4j.
 *
 * @param <T> remote service interface type
 */
public class WebSocketLauncherBuilder<T> extends Launcher.Builder<T> {
	
	protected WebSocketMessageHandler messageHandler;

	protected WebSocketSession session;

	public Collection<Object> getLocalServices() {
		return localServices;
	}
	
	public WebSocketLauncherBuilder<T> setSession(WebSocketSession session) {
		this.session = session;
		return this;
	}

	public WebSocketLauncherBuilder<T> setMessageHandler(WebSocketMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
		return this;
	}
	
	@Override
	public Launcher<T> create() {
		if (localServices == null)
			throw new IllegalStateException("Local service must be configured.");
		if (remoteInterfaces == null)
			throw new IllegalStateException("Remote interface must be configured.");
		if (session == null)
			throw new IllegalStateException("WebSocket session must be configured.");
		if (messageHandler == null)
			throw new IllegalStateException("MessageHandler must be configured.");
		
		MessageJsonHandler jsonHandler = createJsonHandler();
		RemoteEndpoint remoteEndpoint = createRemoteEndpoint(jsonHandler);
		addMessageHandlers(jsonHandler, remoteEndpoint);
		T remoteProxy = createProxy(remoteEndpoint);
		return createLauncher(null, remoteProxy, remoteEndpoint, null);
	}
	
	@Override
	protected RemoteEndpoint createRemoteEndpoint(MessageJsonHandler jsonHandler) {
		MessageConsumer outgoingMessageStream = new WebSocketMessageConsumer(session, jsonHandler);
		outgoingMessageStream = wrapMessageConsumer(outgoingMessageStream);
		Endpoint localEndpoint = ServiceEndpoints.toEndpoint(localServices);
		RemoteEndpoint remoteEndpoint;
		if (exceptionHandler == null)
			remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint);
		else
			remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint, exceptionHandler);
		jsonHandler.setMethodProvider(remoteEndpoint);
		return remoteEndpoint;
	}
	
	protected void addMessageHandlers(MessageJsonHandler jsonHandler, RemoteEndpoint remoteEndpoint) {
		MessageConsumer messageConsumer = wrapMessageConsumer(remoteEndpoint);
		messageHandler.configure(messageConsumer, jsonHandler, remoteEndpoint);
	}
	
}
