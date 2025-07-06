package com.example.owlpost_2_0.Controllers;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.HighGui;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class VideoReceiver {
    public static void start(int listenPort) throws Exception {
        OpenCV.loadLocally();
        DatagramSocket socket = new DatagramSocket(listenPort);

        byte[] buffer = new byte[65507];
        MatOfByte mob = new MatOfByte();

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
            mob.fromArray(data);

            Mat image = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
            if (!image.empty()) {
                HighGui.imshow("Incoming Video", image);
                if (HighGui.waitKey(1) == 27) break;
            }
        }

        socket.close();
        HighGui.destroyAllWindows();
    }
}