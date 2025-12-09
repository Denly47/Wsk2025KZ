// package frc.robot.commands.driveCommands;

// //WPI imports
// import edu.wpi.first.wpilibj2.command.CommandBase;

// //RobotContainer import
// import frc.robot.RobotContainer;
// import frc.robot.subsystems.Det;
// //Subsystem imports
// import frc.robot.subsystems.DriveTrain;

// /**
//  * SimpleDrive class
//  * <p>
//  * This class drives a motor 
//  */
// public class SimpleDrive extends CommandBase
// {
//     //Grab the subsystem instance from RobotContainer
//     private static final DriveTrain drive = RobotContainer.drive;
//     private static final Det det = RobotContainer.det;
//     double strafe, forward, rotation;
//     double predOtr, otpravka, sortirovka;
//     double chet = 0;
//     boolean lastX = false;
//     boolean pravda = false;

//     /**
//      * Constructor
//      */
//     public SimpleDrive()
//     {
//         addRequirements(drive); // Adds the subsystem to the command
//         // this.predOtr = predOtr;
//         // this.strafe = strafe;
//         // this.forward = forward;
//         // this.rotation = rotation;

//     }

//     /**
//      * Runs before execute
//      */
//     @Override
//     public void initialize()
//     {
        
//     }

//     /**
//      * Called continously until command is ended
//      */
//     @Override
//     public void execute()
//     {



//                 if (det.isRedDetected() && !lastX) {
//                    chet +=1;
//                 }
//                 lastX = det.isRedDetected();
//                 if(det.isRedDetected()){
//                     drive.setServoYAngle(255);
//                 } else if(det.isGreenDetected()){
//                     drive.setServoYAngle(60.0);
//                 } else {
//                     drive.setServoYAngle(90.0);
//                 }

//                 if (chet == 2){
//                     pravda = true;
//                 }

//             System.out.println("Красный шарик" + chet);
//             System.out.println("prafda" + pravda);
// //        if(det.isBlueDetected()){
//         //     drive.setServoYAngle(255);
//         // }
//         // else if(det.isGreenDetected() || det.isRedDetected() || det.isYellowDetected()){
//         //     drive.setServoYAngle(60.0);
//         // } else {
//         //     drive.setServoYAngle(91.0);
//         // }
//     }

//     /**
//      * Called when the command is told to end or is interrupted
//      */
//     @Override
//     public void end(boolean interrupted)
//     {
        
//     }

//     /**
//      * Creates an isFinished condition if needed
//      */
//     @Override
//     public boolean isFinished()
//     {
//         return pravda;
//     }

// }