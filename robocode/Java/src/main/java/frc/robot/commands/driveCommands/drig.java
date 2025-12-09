package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class drig extends CommandBase
{
    //Bring in the Drive Train subsystem
    private static final DriveTrain drive = RobotContainer.drive;

    private double setpointDistance;

    //Create two PID Controllers
    PIDController pidYAxis;

    public drig(double setpointDistance,double epsilonDistance)
    {
        this.setpointDistance = setpointDistance;
        addRequirements(drive);

        pidYAxis = new PIDController(10000.0, 0, 0);
        pidYAxis.setTolerance(epsilonDistance);
    }

    @Override
    public void initialize()
    {
        pidYAxis.reset();
        drive.liftReset();
        drive.resetEncoders();
        drive.liftReset();
    }

    @Override
    public void execute()
    {
        if (drive.getElevatorMin()){    
        drive.setElevatorSpeed(
         MathUtil.clamp(pidYAxis.calculate(drive.getLiftEncoderDistance(), setpointDistance), -1.0, 1.0));
        } else {
            drive.setElevatorSpeed(0);
        }
    }

    @Override
    public void end (boolean interrupted)
    {
        drive.setElevatorSpeed(0);
    }

    @Override
    public boolean isFinished()
    {
        return pidYAxis.atSetpoint() ;
    }
}