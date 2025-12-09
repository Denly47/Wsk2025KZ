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
// public class ser extends CommandBase
// {
//     //Grab the subsystem instance from RobotContainer
//     private static final DriveTrain drive = RobotContainer.drive;
//     private static final Det det = RobotContainer.det;
//     double strafe, forward, rotation;
//     double predOtr, otpravka, sortirovka;
    

//     /**
//      * Constructor
//      */
//     public ser(double predOtr, double otpravka, double sortirovka)
//     {
//         addRequirements(drive); // Adds the subsystem to the command
//         this.predOtr = predOtr;
//         this.otpravka = otpravka;
//         this.sortirovka = sortirovka;
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
//             drive.setArmAngle(otpravka);
//             drive.setClawAngle(predOtr);
//             drive.setServoYAngle(sortirovka);
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
//         return false;
//     }

// }