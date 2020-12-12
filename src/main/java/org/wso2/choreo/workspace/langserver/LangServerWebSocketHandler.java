package org.wso2.choreo.workspace.langserver;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class LangServerWebSocketHandler extends TextWebSocketHandler {

    private BallerinaLanguageServer languageServer;
    private WebSocketMessageHandler messageHandler;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        System.out.println(message.toString());
        System.out.println(session.getAttributes());
        if (messageHandler != null) {
            messageHandler.onMessage(message.getPayload());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("new connection established - " + session.getId() + " from: " + session.getRemoteAddress());
        try{
            languageServer = new BallerinaLanguageServer();
            messageHandler = new WebSocketMessageHandler();
            WebSocketLauncherBuilder<ExtendedLanguageClient> builder = new WebSocketLauncherBuilder<>();
            builder
                    .setSession(session)
                    .setMessageHandler(messageHandler)
                    .setLocalService(languageServer)
                    .setRemoteInterface(ExtendedLanguageClient.class);
            Launcher<ExtendedLanguageClient> extendedLanguageClientLauncher = builder.create();
            languageServer.connect(extendedLanguageClientLauncher.getRemoteProxy());
        } catch (Throwable th) {
            th.printStackTrace();
            throw th;
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("removing session due to errors: " + session.toString());
        exception.printStackTrace();
        languageServer.shutdown();
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("shutting down language server due to connection close: " + session.toString()
                            + " close status: " + status.toString());
        languageServer.shutdown();
    }


}