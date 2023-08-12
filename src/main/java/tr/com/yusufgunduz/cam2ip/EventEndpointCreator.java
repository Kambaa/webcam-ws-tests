package tr.com.yusufgunduz.cam2ip;

import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;

class EventEndpointCreator implements JettyWebSocketCreator {
    @Override
    public Object createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest, JettyServerUpgradeResponse jettyServerUpgradeResponse) {
        return new EventEndpoint();
    }
}