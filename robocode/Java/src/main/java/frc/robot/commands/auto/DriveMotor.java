package frc.robot.commands.auto;

import frc.robot.RobotContainer;
import frc.robot.commands.driveCommands.Arm;
import frc.robot.commands.driveCommands.Claw;
import frc.robot.commands.driveCommands.Cobra;
import frc.robot.commands.driveCommands.DriveEnc;
import frc.robot.commands.driveCommands.Elevator;
import frc.robot.commands.driveCommands.Elevator_max;
import frc.robot.commands.driveCommands.Elevator_min;
//import frc.robot.commands.driveCommands.MazeNavigator;
import frc.robot.commands.driveCommands.Sensor;
import frc.robot.commands.driveCommands.Sensor_Lidar;

//import frc.robot.commands.driveCommands.Sensor;
// import frc.robot.commands.driveCommands.SimpleDrive;
// import frc.robot.commands.driveCommands.Claw;
// import frc.robot.commands.driveCommands.DriveEnc;
// // import the commands
// import frc.robot.commands.driveCommands.SimpleDrive;
// import frc.robot.commands.driveCommands.StopMotors;
// import frc.robot.commands.driveCommands.TurnToAngle;
// import frc.robot.commands.driveCommands.resetYaw;
import frc.robot.commands.driveCommands.StopMotors;
import frc.robot.commands.driveCommands.TurnToAngle;
import frc.robot.commands.driveCommands.drig;
import frc.robot.commands.driveCommands.resetEnc;
import frc.robot.commands.driveCommands.resetYaw;
import frc.robot.commands.driveCommands.strafe;

/**
 * DriveMotor class
 * <p>
 * This class creates the inline auto command to drive the motor
 */
public class DriveMotor extends AutoCommand
{
    /**
     * Constructor
     */
    public DriveMotor()
    {
        /**
         * Calls SimpleDrive at a speed of 50% waits 5 seconds and stops the motors
         */
        super(
           // new SimpleDrive(-0.3, 0.0, 0.0),
            // new Arm(300, 200).withTimeout(2),
        //    new Claw(300).withTimeout(2),
        //    new Claw(0).withTimeout(2),
        //    new Claw(300).withTimeout(2),
      //  new MazeNavigator(0, 0.1),
        //  new MazeNavigatord(0.0, 0.1).withTimeout(19),
        //  new MazeNavigator(0.0, 0.1),


           new resetYaw().withTimeout(0.3),
          // new ser(predOtr, otpravka, sortirovka)

      //  new drig(100, 0.1).withTimeout(3),
    //   new ser(6, 289, 90).withTimeout(3),
    //   new SimpleDrive().withTimeout(10),
          //  new DriveEnc(200, 200, 0.1, 0.1, 0.0, 0.1),
          new DriveEnc(700, 700, 0.1, 0.1, 0.0, 0.1),
        
            new Arm(300, 240).withTimeout(2),
            new Arm(300, 240).withTimeout(2),
            new TurnToAngle(-92, 0.1),
            new resetYaw().withTimeout(0.3),
            new DriveEnc(1450, 1450, 0.1, 0.1, 0.0, 0.1),
            new TurnToAngle(0, 0.1),
            // new strafe(2100, 0.1, 0.0, 0.1),
            new Claw(0).withTimeout(2),
            new TurnToAngle(-93, 0.1),
            new Arm(300, 240).withTimeout(2),
            new Arm(300, 240).withTimeout(2),
            new resetYaw().withTimeout(0.3),
            new Arm(200, 240).withTimeout(2),
            new resetYaw().withTimeout(0.3),
            new DriveEnc(600, 600, 0.1, 0.1, 0.0, 0.1),
            new Claw(300).withTimeout(2),   
            new Arm(300, 240).withTimeout(2),
            new Arm(300, 240).withTimeout(2),
            new DriveEnc(-500, -500, 0.1, 0.1, 0.0, 0.1),
            new TurnToAngle(0, 0.1),
            new TurnToAngle(90, 0.1),
            new Claw(0).withTimeout(2),
            new TurnToAngle(0, 0.1),
            new TurnToAngle(-8, 0.1),
            new resetYaw().withTimeout(0.3),
            new Arm(250, 240).withTimeout(2),
            new DriveEnc(500, 500, 0.1, 0.1, 0.0, 0.1),
            new Claw(300).withTimeout(4),
            new DriveEnc(-500, -500, 0.1, 0.1, 0.0, 0.1),
            new Claw(0).withTimeout(2),
            new TurnToAngle(-90, 0.1),
            new Arm(300, 240).withTimeout(2),
            new DriveEnc(1600, 1600, 0.1, 0.1, 0.0, 0.1),
            new strafe(90000, 0.1, 0.0, 0.1),

            //    new ser(6, 289, 90).withTimeout(3),
        //    new ser(6, 150, 90).withTimeout(3),
        //    new ser(6, 289, 90).withTimeout(3),
        //    new ser(15, 289, 90).withTimeout(3),
        //    new ser(6, 289, 90).withTimeout(3),
        //    new SimpleDrive().withTimeout(10),
        //     new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new strafe(-1600, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(-3000, -3000, 0.1, 0.1, 0.0, 0.1),


        //    new DriveEnc(830, 830, 0.1, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new strafe(-1400, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(-700, -700, 0.1, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(700, 700, 0.1, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new strafe(900, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(1750, 1750, 0.1, 0.1, 0.0, 0.1),
        //     new TurnToAngle(-45.0, 0.1),
        //     new resetYaw().withTimeout(0.3),
        //     new DriveEnc(-1200, -1200, 0.1, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(1900, 1900, 0.1, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(-1200, -1200, 0.1, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new TurnToAngle(45.0, 0.1),
        //    new resetYaw().withTimeout(0.3),
        //    new DriveEnc(1200, 1200, 0.1, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new strafe(400, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new strafe(-400, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(-1200, -1200, 0.1, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new TurnToAngle(0.0, 0.1),
        //    new strafe(600, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new TurnToAngle(0.0, 0.1),
        //    new DriveEnc(-1600, -1600, 0.1, 0.1, 0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new strafe(600, 0.1, 0.0, 0.1),
        //    new TurnToAngle(0.0, 0.1),
        //    new Arm(300, 240).withTimeout(2),
        //    new Arm(300, 240).withTimeout(2),
        //    new DriveEnc(-800, -800, 0.1, 0.1, 0.0, 0.1),
        //    new StopMotors().withTimeout(120),
        //    new MazeNavigatord(0.0, 0.1).withTimeout(300),
        //    new StopMotors().withTimeout(120),
        //    new resetYaw().withTimeout(0.3),
        //    new DriveEnc(-800, -800, 0.1, 0.1, 0.0, 0.1),
        //    new StopMotors().withTimeout(120),
        //    new MazeNavigatord(0.0, 0.1).withTimeout(300),
        //    new StopMotors().withTimeout(120),
        //    new MazeNavigatord(0.0, 0.1).withTimeout(300),
        //    new StopMotors().withTimeout(120),
           


        new StopMotors()
        );  
    }
}