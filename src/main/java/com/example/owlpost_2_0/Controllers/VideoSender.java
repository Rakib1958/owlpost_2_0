package com.example.owlpost_2_0.Controllers;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.MatOfByte;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VideoSender {
    private static volatile boolean isRunning = false;
    private static VideoCapture cap;
    private static DatagramSocket socket;
    private static Thread senderThread;

    public static void start(String targetIP, int targetPort) throws Exception {
        if (isRunning) {
            stop();
        }

        OpenCV.loadLocally();

        cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            System.out.println("Camera not available.");
            return;
        }

        socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(targetIP);

        isRunning = true;

        senderThread = new Thread(() -> {
            Mat frame = new Mat();
            MatOfByte buffer = new MatOfByte();
            MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 30);

            try {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    if (cap.read(frame) && !frame.empty()) {
                        Imgproc.resize(frame, frame, new Size(320, 240));
                        HighGui.imshow("My Camera", frame);
                        if (HighGui.waitKey(1) == 27) break;
                        Imgcodecs.imencode(".jpg", frame, buffer, params);
                        byte[] data = buffer.toArray();
                        if (data.length <= 65507) {
                            DatagramPacket packet = new DatagramPacket(data, data.length, address, targetPort);
                            socket.send(packet);
                        } else {
                            System.out.println("Frame too large: " + data.length + " bytes, skipping...");
                        }
                        Thread.sleep(33);
                    }
                }
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("Video sender error: " + e.getMessage());
                }
            } finally {
                cleanup();
            }
        });

        senderThread.start();
    }

    public static void stop() {
        isRunning = false;
        if (senderThread != null) {
            senderThread.interrupt();
        }
        cleanup();
    }

    private static void cleanup() {
        try {
            if (cap != null && cap.isOpened()) {
                cap.release();
            }
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