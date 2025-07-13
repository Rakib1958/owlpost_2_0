package com.example.owlpost_2_0.Controllers;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.HighGui;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class VideoReceiver {
    private static volatile boolean isRunning = false;
    private static DatagramSocket socket;
    private static Thread receiverThread;
    private static Consumer<Image> imageCallback;

    public static void start(int listenPort, Consumer<Image> callback) throws Exception {
        if (isRunning) {
            stop();
        }

        OpenCV.loadLocally();
        socket = new DatagramSocket(listenPort);
        imageCallback = callback;
        isRunning = true;

        receiverThread = new Thread(() -> {
            byte[] buffer = new byte[65507];
            MatOfByte mob = new MatOfByte();

            try {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                    try {
                        mob.fromArray(data);
                        Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);

                        if (!image.empty()) {
                            MatOfByte matOfByte = new MatOfByte();
                            Imgcodecs.imencode(".jpg", image, matOfByte);
                            byte[] byteArray = matOfByte.toArray();
                            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
                            BufferedImage bufferedImage = ImageIO.read(bis);

                            if (bufferedImage != null) {
                                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                                if (imageCallback != null) {
                                    Platform.runLater(() -> imageCallback.accept(fxImage));
                                }
                            }
                            HighGui.imshow("Incoming Video", image);
                            if (HighGui.waitKey(1) == 27) break;
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing frame: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("Video receiver error: " + e.getMessage());
                }
            } finally {
                cleanup();
            }
        });

        receiverThread.start();
    }

    public static void stop() {
        isRunning = false;
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        cleanup();
    }

    private static void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            HighGui.destroyAllWindows();
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }
}