package tr.com.yusufgunduz;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

// simple websockets :
// ws://localhost:8080/events/
//https://github.com/jetty-project/embedded-jetty-websocket-examples/blob/10.0.x/native-jetty-websocket-example/src/main/java/org/eclipse/jetty/demo/EventClient.java
public class Bb {
    public static void main(String[] args) throws Exception {
        Bb server = new Bb();
        server.setPort(8080);
        server.start();
        server.join();
    }

    private final Server server;
    private final ServerConnector connector;

    public Bb() {
        server = new Server();
        connector = new ServerConnector(server);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Configure specific websocket behavior
        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) ->
        {
            // Configure default max size
            wsContainer.setMaxTextMessageSize(65535);

            // Add websockets
            wsContainer.addMapping("/events/*", new EventEndpointCreator());
        });
    }

    public void setPort(int port) {
        connector.setPort(port);
    }

    public void start() throws Exception {
        server.start();
    }

    public URI getURI() {
        return server.getURI();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws InterruptedException {
        System.out.println("Use Ctrl+C to stop server");
        server.join();
    }

}


class EventEndpointCreator implements JettyWebSocketCreator {
    @Override
    public Object createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest, JettyServerUpgradeResponse jettyServerUpgradeResponse) {
        return new EventEndpoint();
    }
}


class EventEndpoint extends WebSocketAdapter {
    private final CountDownLatch closureLatch = new CountDownLatch(1);

    private FrameGrabber grabber;
    private Java2DFrameConverter converter;
    private boolean streaming;


    private String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private void sendWebcamBytes(RemoteEndpoint remote) {
        try {
            grabber = FrameGrabber.createDefault(1);
            grabber.start();
            converter = new Java2DFrameConverter();
            streaming = true;

            new Thread(() -> {
                try {
                    while (streaming) {
                        BufferedImage image = converter.convert(grabber.grab());
                        if (image != null) {
                            String base64Image = convertToBase64(image);
                            remote.sendString(base64Image);
                            Thread.sleep(1000 / 25); // Adjust the delay as needed
                        }
//                        RAW IMAGE SENDING AS byte array.
//                        BufferedImage image = converter.convert(grabber.grab());
//                        if (image != null) {
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            ImageIO.write(image, "jpg", baos);
//                            ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
//                            remote.sendBytes(buffer);
//                            Thread.sleep(300); // Adjust the delay as needed
//                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        System.out.println("Endpoint connected: {}" + sess);
        try {
            sess.getRemote().sendString("you are connected baboş");
            sendWebcamBytes(getSession().getRemote());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        System.out.println("Received TEXT message: {}" + message);
        try {
            getSession().getRemote().sendString("bana dedin ha: " + message
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (message.toLowerCase(Locale.US).contains("webcam")) {
//            sendWebcamBytes(getRemote());
        } else if (message.toLowerCase(Locale.US).contains("bye")) {
            streaming = false;
            getSession().close(StatusCode.NORMAL, "Thanks");
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        System.out.println("Socket Closed: [{}] {}" + statusCode + reason);
        closureLatch.countDown();
        streaming = false;
        try {
            if (grabber != null) {
                grabber.stop();
                grabber.release();
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }

    public void awaitClosure() throws InterruptedException {
        System.out.println("Awaiting closure from remote");
        closureLatch.await();
    }
}