package org.wso2.choreo.workspace.langserver;

import java.net.URI;
import java.util.Scanner;
import javax.websocket.*;
import javax.websocket.RemoteEndpoint.Basic;

@ClientEndpoint
public class MyWebSocketClient {
    @OnMessage
    public void onMessage(String message) {
        System.out.println(">>>>>>> " + message);
    }

    public static void main(String[] args) {
        WebSocketContainer container = null;
        Session session = null;
        Basic basicRemote = null;
        Scanner inputScanner = null;

        try {
            container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(MyWebSocketClient.class, URI.create("ws://localhost:8080/orgs/ramith/apps/myapp/workspace/lang-server"));
            basicRemote = session.getBasicRemote();
            inputScanner = new Scanner(System.in);

            while (true) {
                String input = inputScanner.nextLine();
                basicRemote.sendText("{ \"name\": \"" + input + "\" }");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}