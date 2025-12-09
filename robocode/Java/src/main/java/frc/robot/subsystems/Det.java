// package frc.robot.subsystems;

// import java.util.ArrayList;
// import java.util.List;

// import org.opencv.core.*;
// import org.opencv.imgproc.Imgproc;

// import edu.wpi.cscore.CvSink;
// import edu.wpi.cscore.CvSource;
// import edu.wpi.cscore.UsbCamera;
// import edu.wpi.first.cameraserver.CameraServer;
// import edu.wpi.first.networktables.NetworkTableEntry;
// import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
// import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
// import edu.wpi.first.wpilibj2.command.SubsystemBase;
// import org.opencv.core.MatOfPoint;

// public class Det extends SubsystemBase {

//     Thread m_visionThread;
//     UsbCamera camera;
//     CvSource outputStream, outputStream1;
//     double chet= 0;

//     private final ShuffleboardTab tab = Shuffleboard.getTab("CCCamera");
//     private final NetworkTableEntry Reds = tab.add("Red", false).getEntry();
//     private final NetworkTableEntry Green = tab.add("Green", false).getEntry();

//     private volatile boolean RedObject = false;
//     private volatile boolean GreenObject = false;
//     boolean lastX = false;
//     public Det() {
//         m_visionThread = new Thread(() -> {
//             camera = CameraServer.getInstance().startAutomaticCapture(0);
//             // camera.setResolution(176, 144);
//             camera.setBrightness(80);
//             camera.setFPS(15);
//             //640, 480
//             //360, 240
//             CvSink cvSink = CameraServer.getInstance().getVideo();
//             outputStream = CameraServer.getInstance().putVideo("Mask", 176, 144);
//             outputStream1 = CameraServer.getInstance().putVideo("Camera Output", 176, 144);

//             Mat mat = new Mat();
//             Mat hsv = new Mat();
//             Mat yellowMask = new Mat();
//             Mat blueMask = new Mat();
//             Mat redMask1 = new Mat();
//             Mat redMask2 = new Mat();
//             Mat redMask = new Mat();
//             Mat allMask = new Mat();
//             Mat greenMask = new Mat();

//             Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13, 13));

//             Scalar lowerYellow = new Scalar(20, 50, 50);
//             Scalar upperYellow = new Scalar(40, 255, 255);

//             Scalar lowerBlue = new Scalar(80, 100, 50);
//             Scalar upperBlue = new Scalar(140, 255, 255);

//             Scalar lowerRed1 = new Scalar(0, 100, 100);
//             Scalar upperRed1 = new Scalar(10, 255, 255);
//             Scalar lowerRed2 = new Scalar(160, 100, 100);
//             Scalar upperRed2 = new Scalar(180, 255, 255);

//             Scalar lowerGreen = new Scalar(35, 50, 50);    // S >= 40
//             Scalar upperGreen = new Scalar(85, 255, 255);

//             while (!Thread.interrupted()) {
//                 if (cvSink.grabFrame(mat) == 0) {
//                     continue;
//                 }

//                 Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

//                 Core.inRange(hsv, lowerYellow, upperYellow, yellowMask);
//                 Core.inRange(hsv, lowerBlue, upperBlue, blueMask);
//                 Core.inRange(hsv, lowerRed1, upperRed1, redMask1);
//                 Core.inRange(hsv, lowerRed2, upperRed2, redMask2);
//                 Core.inRange(hsv, lowerGreen, upperGreen, greenMask);
//                 Core.addWeighted(redMask1, 1.0, redMask2, 1.0, 0.0, redMask);

//                 Core.bitwise_or(yellowMask, blueMask, allMask);
//                 Core.bitwise_or(greenMask, blueMask, allMask);
//                 Core.bitwise_or(allMask, redMask, allMask);
//                 // Process each color

//                 RedObject = processColor(redMask, mat, kernel, new Scalar(0, 0, 255));
//                 GreenObject = processColor(greenMask, mat, kernel, new Scalar(0, 0, 0));


//                 outputStream1.putFrame(mat);
//                 outputStream.putFrame(allMask); 
//             }
//         });

//         m_visionThread.setDaemon(true);
//         m_visionThread.start();
//     }

//     private boolean processColor(Mat mask, Mat output, Mat kernel, Scalar color) {
//         Imgproc.erode(mask, mask, kernel);
//         Imgproc.dilate(mask, mask, kernel);

//         int pixelCount = Core.countNonZero(mask);
//         if (pixelCount < 10) {
//             return false;
//         }

//         List<MatOfPoint> contours = new ArrayList<>();
//         Mat hierarchy = new Mat();
//         Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

//         for (MatOfPoint contour : contours) {
//             Rect rect = Imgproc.boundingRect(contour);
//             Imgproc.rectangle(output, rect.tl(), rect.br(), color, 2);

//             int centerX = rect.x + rect.width / 2;
//             int centerY = rect.y + rect.height / 2;
//             Imgproc.putText(output, "(" + centerX + ", " + centerY + ")", new Point(centerX, centerY),
//                     Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);
//         }

//         return true;
//     }


//     public boolean isRedDetected() {
//         return RedObject;
//     }

//     public boolean isGreenDetected() {
//         return GreenObject;
//     }

//     @Override
//     public void periodic() {
//         Reds.setBoolean(RedObject);
//         Green.setBoolean(GreenObject);
      
//     }
// }
