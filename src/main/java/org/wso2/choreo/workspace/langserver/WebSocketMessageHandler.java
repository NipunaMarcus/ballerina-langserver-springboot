/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */
package org.wso2.choreo.workspace.langserver;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.MessageIssueHandler;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

/**
 * WebSocket message handler that parses JSON messages and forwards them to a {@link MessageConsumer}.
 */
public class WebSocketMessageHandler {
	
	private MessageConsumer callback;
	private MessageJsonHandler jsonHandler;
	private MessageIssueHandler issueHandler;
	
	public void configure(MessageConsumer callback, MessageJsonHandler jsonHandler, MessageIssueHandler issueHandler) {
		this.callback = callback;
		this.jsonHandler = jsonHandler;
		this.issueHandler = issueHandler;
	}
	
	public void onMessage(String content) {
		try {
			Message message = jsonHandler.parseMessage(content);
			callback.consume(message);
		} catch (MessageIssueException exception) {
			// An issue was found while parsing or validating the message
			issueHandler.handle(exception.getRpcMessage(), exception.getIssues());
		}
	}
	
}
