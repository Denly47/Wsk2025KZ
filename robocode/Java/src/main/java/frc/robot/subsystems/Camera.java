package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.opencv.core.MatOfPoint;

public class Camera extends SubsystemBase {

    Thread m_visionThread;
    UsbCamera camera;
    CvSource outputStream, outputStream1;

    private final ShuffleboardTab tab = Shuffleboard.getTab("CCCamera");
    private final NetworkTableEntry Black = tab.add("black", false).getEntry();
    boolean blackk = false;

    private volatile boolean BlackObject = false;

    public Camera() {
        m_visionThread = new Thread(() -> {
            camera = CameraServer.getInstance().startAutomaticCapture(0);
            camera.setResolution(640, 480);
            camera.setFPS(15);
            
            CvSink cvSink = CameraServer.getInstance().getVideo();
            outputStream = CameraServer.getInstance().putVideo("Mask", 640, 480);
            outputStream1 = CameraServer.getInstance().putVideo("Camera Output", 640, 480);

            Mat mat = new Mat();
            Mat hsv = new Mat();
            Mat blackMask = new Mat();
            Mat allMask = new Mat();

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13, 13));
            Scalar lowerBlack = new Scalar(35, 50, 50);    // S >= 40
            Scalar upperBlack = new Scalar(85, 255, 255);
            // Scalar lowerBound = new Scalar(35, 50, 50);
            // Scalar upperBound = new Scalar(85, 255, 255);
             // Scalar lowerBound = new Scalar(10, 80, 40);  // ближе к 21°, чуть ниже S и V
    //             // Scalar upperBound = new Scalar(22, 255, 255); // ближе к 41°, максимум S и V
               //             Scalar lowerBound = new Scalar(10, 80, 40);  // ближе к 21°, чуть ниже S и V
    //             Scalar upperBound = new Scalar(22, 255, 255); // ближе к 41°, максимум S и V
            // Scalar lowerBlack = new Scalar(0, 40, 0);    // S >= 40
            // Scalar upperBlack = new Scalar(180, 255, 40);
            
             


            while (!Thread.interrupted()) {
                if (cvSink.grabFrame(mat) == 0) {
                    continue;
                }

                Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

                Core.inRange(hsv, lowerBlack, upperBlack, blackMask);
                // Process each color
                BlackObject = processColor(blackMask, mat, kernel, new Scalar(0, 255, 255));

                outputStream1.putFrame(mat);
                outputStream.putFrame(blackMask); 
            }
        });

        m_visionThread.setDaemon(true);
        m_visionThread.start();
    }

    private boolean processColor(Mat mask, Mat output, Mat kernel, Scalar color) {
        Imgproc.erode(mask, mask, kernel);
        Imgproc.dilate(mask, mask, kernel);

        int pixelCount = Core.countNonZero(mask);
        if (pixelCount < 1000) {
            // blackk = true;
            return false;
        } 
        // else {
        //     blackk = false;
        // }

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            Imgproc.rectangle(output, rect.tl(), rect.br(), color, 2);

            int centerX = rect.x + rect.width / 2;
            int centerY = rect.y + rect.height / 2;
            Imgproc.putText(output, "(" + centerX + ", " + centerY + ")", new Point(centerX, centerY),
                    Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);
        }

        return true;
    }

    public boolean isBlackDetected() {
        return BlackObject;
    }

    @Override
    public void periodic() {
        Black.setBoolean(BlackObject);
    }
}
