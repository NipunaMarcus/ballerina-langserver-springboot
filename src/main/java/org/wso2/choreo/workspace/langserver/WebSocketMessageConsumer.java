/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */
package org.wso2.choreo.workspace.langserver;

import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Message consumer that sends messages via a WebSocket connection.
 */
public class WebSocketMessageConsumer implements MessageConsumer {

    private static final Logger LOGGER = Logger.getLogger(WebSocketMessageConsumer.class.getName());
    private final MessageJsonHandler jsonHandler;
    private WebSocketSession session;

    public WebSocketMessageConsumer(WebSocketSession session, MessageJsonHandler jsonHandler) {
        this.session = session;
        this.jsonHandler = jsonHandler;
    }

    public WebSocketSession getSession() {
        return session;
    }

    @Override
    public void consume(Message message) {
        String content = jsonHandler.serialize(message);
        try {
            sendMessage(content);
        } catch (IOException exception) {
            throw new JsonRpcException(exception);
        }
    }

    protected void sendMessage(String message) throws IOException {
        if (session.isOpen()) {
            int length = message.length();
            // TODO Handle if length is larger than max frame size
            TextMessage textMessage = new TextMessage(message);
            session.sendMessage(textMessage);
        } else {
            LOGGER.info("Ignoring message due to closed connection: " + message);
        }

    }

}
