package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class resetYaw extends CommandBase
{
    //Bring in the Drive Train subsystem
    private static final DriveTrain drive = RobotContainer.drive;

    private double setpointDistance;

    //Create two PID Controllers
    PIDController pidYAxis;

    public resetYaw()
    {
    }

    @Override
    public void initialize()
    {
    }

    @Override
    public void execute()
    {
        drive.resetEncoders();
        drive.resetYaw();
    }

    @Override
    public void end (boolean interrupted)
    {
    }

    @Override
    public boolean isFinished()
    {
       return false;
    }
}