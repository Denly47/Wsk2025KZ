package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class Elevator_max extends CommandBase
{
    //Bring in the Drive Train subsystem
    private static final DriveTrain drive = RobotContainer.drive;

    public Elevator_max()
    {
    }

    @Override
    public void initialize()
    {
        drive.liftReset();
        drive.resetEncoders();
    }

    @Override
    public void execute()
    {
        if(drive.getElevatorMax()) {
            drive.setElevatorSpeed(-0.6);
        } else {
            drive.setElevatorSpeed(0.0);
        }
        
    }

    @Override
    public void end (boolean interrupted)
    {
        drive.setElevatorSpeed(-0.05);
        drive.liftReset();
    }

    @Override
    public boolean isFinished()
    {
        return !drive.getElevatorMax();
    }
}