package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class resetEnc extends CommandBase
{
    //Bring in the Drive Train subsystem
    private static final DriveTrain drive = RobotContainer.drive;

    private double setpointDistance;

    //Create two PID Controllers
    PIDController pidYAxis;

    public resetEnc()
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
        drive.resetEncoders();
    }

    @Override
    public void end (boolean interrupted)
    {
        drive.resetEncoders();
        drive.resetEncoders();

    }

    @Override
    public boolean isFinished()
    {
       return false;
    }
}