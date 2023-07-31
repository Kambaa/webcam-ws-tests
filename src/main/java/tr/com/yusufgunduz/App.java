package tr.com.yusufgunduz;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");


        // Initialize the video capture object for the default webcam
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(1);

//        grabber.getAudioMetadata();
        grabber.getVideoMetadata();


        try {
            // Start the video capture
            grabber.start();

            // Create a window to display the captured images
            CanvasFrame canvas = new CanvasFrame("Webcam Image Stream");

            // Continuously capture and display frames until the user closes the window
            while (canvas.isVisible()) {
                // Capture an image from the webcam
                Frame frame = grabber.grab();

                // Display the captured image in the window
                canvas.showImage(frame);

                System.out.println("getFormat:" + grabber.getFormat());
                System.out.println("getFrameRate:" + grabber.getFrameRate());
                System.out.println("getImageWidth:" + grabber.getImageWidth());
                System.out.println("getImageHeight:" + grabber.getImageHeight());
                System.out.println("getImageMode:" + grabber.getImageMode());
                // Adjust the frame rate (you can set the desired frame rate in milliseconds)
                Thread.sleep(25);
            }

            // Release resources
            canvas.dispose();
            grabber.stop();
            grabber.close();
            // canvas.addWindowListener(new java.awt.event.WindowAdapter() {
            // @Override
            // public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            // // Perform cleanup operations here
            // System.out.println("Performing cleanup before application exit...");
            // try {
            // grabber.close();
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // }
            // });
        } catch (FrameGrabber.Exception | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
