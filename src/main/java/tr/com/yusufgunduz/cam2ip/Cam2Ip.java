package tr.com.yusufgunduz.cam2ip;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.yusufgunduz.Bb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Cam2Ip {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bb.class);

    public static String webcamBase64ImgData;

    private static String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static void main(String[] args) throws Exception {

        FrameGrabber grabber = FrameGrabber.createDefault(1);
        grabber.start();
        Java2DFrameConverter converter = new Java2DFrameConverter();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        Start a thread to continuously fill webcamBase64ImgData
        executorService.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BufferedImage image = converter.convert(grabber.grab());
                    if (image != null) {
                        webcamBase64ImgData = convertToBase64(image);
                    }
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException | IOException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
            System.out.println("thread stopped!");
        });
//        new Thread(() -> {
//            while (true) {
//                try {
//                    BufferedImage image = converter.convert(grabber.grab());
//                    if (image != null) {
//                        webcamBase64ImgData = convertToBase64(image);
//                    }
//                    TimeUnit.MILLISECONDS.sleep(1);
//                } catch (FrameGrabber.Exception e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();

        Cam2Ip server = new Cam2Ip();
        server.setPort(8080);
        server.start();
        server.join();
        LOGGER.info("Websocket server started.");
    }


    private final Server server;
    private final ServerConnector connector;


    public Cam2Ip() {
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
