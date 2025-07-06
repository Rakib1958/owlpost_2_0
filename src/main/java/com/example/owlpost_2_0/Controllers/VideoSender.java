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
    public static void start(String targetIP, int targetPort) throws Exception {
        OpenCV.loadLocally();

        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            System.out.println("Camera not available.");
            return;
        }

        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(targetIP);

        Mat frame = new Mat();
        MatOfByte buffer = new MatOfByte();
        MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 50); // ⬅️ compression quality (0–100)

        while (true) {
            if (cap.read(frame)) {
                // ↓ Resize frame before encoding
                Imgproc.resize(frame, frame, new Size(320, 240));  // You can try 480x360 or 640x480 too
                HighGui.imshow("My Camera", frame);
                HighGui.waitKey(1);
                // ↓ Encode with reduced JPEG quality
                Imgcodecs.imencode(".jpg", frame, buffer, params);
                byte[] data = buffer.toArray();

                if (data.length <= 65507) {
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, targetPort);
                    socket.send(packet);
                } else {
                    System.out.println("Frame too large, skipping...");
                }

                Thread.sleep(33); // ~30 FPS
            }
        }
    }
}