package tr.com.yusufgunduz.cam2ip;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class EventEndpoint extends WebSocketAdapter {
    //    In Java, a `CountDownLatch` is a synchronization mechanism provided by the `java.util.concurrent` package that allows one or more threads to wait for a set of operations or events to complete before they can proceed. It's often used to coordinate the execution of multiple threads in a concurrent program.
//
//A `CountDownLatch` is initialized with a count, and threads can call the `await()` method on it to wait until the count reaches zero. Other threads can call the `countDown()` method to decrement the count. Once the count reaches zero, all waiting threads are released and can continue their execution.
//
//Here's a basic example to illustrate the usage of `CountDownLatch`:
//
//```java
//import java.util.concurrent.CountDownLatch;
//
//public class CountDownLatchExample {
//
//    public static void main(String[] args) throws InterruptedException {
//        int threadCount = 3;
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        for (int i = 0; i < threadCount; i++) {
//            new Thread(new Worker(latch, i)).start();
//        }
//
//        // Main thread waits until all workers are done
//        latch.await();
//
//        System.out.println("All workers have completed.");
//    }
//
//    static class Worker implements Runnable {
//        private final CountDownLatch latch;
//        private final int id;
//
//        Worker(CountDownLatch latch, int id) {
//            this.latch = latch;
//            this.id = id;
//        }
//
//        @Override
//        public void run() {
//            System.out.println("Worker " + id + " started.");
//            // Simulating some work
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            System.out.println("Worker " + id + " completed.");
//            latch.countDown();
//        }
//    }
//}
//```
//
//In this example:
//
//1. We create a `CountDownLatch` with a count of 3, indicating that three worker threads need to complete their tasks.
//
//2. We create three worker threads, passing the `CountDownLatch` instance to each of them.
//
//3. Each worker thread simulates some work and then decrements the latch count using `countDown()`.
//
//4. The main thread calls `latch.await()` to wait until the count reaches zero.
//
//5. Once all workers are done and the count reaches zero, the main thread continues its execution.
//
//`CountDownLatch` is commonly used in scenarios where you have multiple threads that need to wait for a specific set of tasks to complete before proceeding, such as waiting for initialization tasks or coordinating the execution of parallel tasks.
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
