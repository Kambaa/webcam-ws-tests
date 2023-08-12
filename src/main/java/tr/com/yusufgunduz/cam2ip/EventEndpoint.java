package tr.com.yusufgunduz.cam2ip;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class EventEndpoint extends WebSocketAdapter {
    private final CountDownLatch closureLatch = new CountDownLatch(1);


    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        System.out.println("Endpoint connected: {}" + sess);
        try {
            sess.getRemote().sendString("you are connected baboş");
            new Thread(() -> {
                while (getSession().isOpen()) {
                    try {
                        getSession().getRemote().sendString(Cam2Ip.webcamBase64ImgData);
                        TimeUnit.MILLISECONDS.sleep(1000 / 5);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
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

        } else if (message.toLowerCase(Locale.US).contains("bye")) {
            try {
                getSession().getRemote().sendString("kapama istegi geldi, " + message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            getSession().close(StatusCode.NORMAL, "kapama istegi geldi");
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        System.out.println("Socket Closed: [{}] {}" + statusCode + reason);
        closureLatch.countDown();
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
