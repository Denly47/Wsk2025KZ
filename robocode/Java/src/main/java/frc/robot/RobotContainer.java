/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

//Java imports
import java.util.HashMap;
import java.util.Map;

//WPI imports
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.Teleop;
//Command imports
import frc.robot.commands.auto.AutoCommand;
import frc.robot.commands.auto.DriveMotor;
import frc.robot.gamepad.OI;
//import frc.robot.subsystems.Camera;
// import frc.robot.subsystems.Det;
//Subsystem imports
import frc.robot.subsystems.DriveTrain;

/**
 * RobotContainer Class
 * <p>
 * This class is used for creating the instances of subsystems and organizing commands
 */
public class RobotContainer
{
  //Define subsystems
  public static DriveTrain drive;
  public static OI oi;
  // public static Camera camera;
  // public static Det det;

  //Define the auto selector
  public static SendableChooser<String> autoChooser;
  public static Map<String, AutoCommand> autoMode = new HashMap<>();

  /**
   * Constructor
   */
  public RobotContainer()
  {
    //Create an instance of subsystems
    drive = new DriveTrain();
   // camera = new Camera();
    // det = new Det();
    oi = new OI();
    drive.setDefaultCommand(new Teleop());
  }

  /**
   * Used for getting the autonomous command to be executed
   * @return autonmous command to execute
   */
  public Command getAutonomousCommand()
  {
    String mode = RobotContainer.autoChooser.getSelected();
    SmartDashboard.putString("Chosen Auto Mode", mode);
    return autoMode.getOrDefault(mode, new DriveMotor());
  }
}
