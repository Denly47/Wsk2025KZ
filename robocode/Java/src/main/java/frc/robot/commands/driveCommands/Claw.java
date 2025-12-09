package frc.robot.commands.driveCommands;

//WPI imports
import edu.wpi.first.wpilibj2.command.CommandBase;

//RobotContainer import
import frc.robot.RobotContainer;

//Subsystem imports
import frc.robot.subsystems.DriveTrain;

/**
 * SimpleServo class
 * <p>
 * This class drives a continuous servo motor
 */
public class Claw extends CommandBase
{
    //Grab the subsystem instance from RobotContainer
    private static final DriveTrain drive = RobotContainer.drive;

    double claw;

    /**
     * Constructor
     */
    public Claw(double claw)
    {
        this.claw = claw;
    }

    /**
     * Runs before execute
     */
    @Override
    public void initialize()
    {
        drive.resetEncoders();
        drive.liftReset();
    }

    /**
     * Called continously until command is ended
     */
    @Override
    public void execute()
    {
        drive.setClawAngle(claw);
    }

    /**
     * Called when the command is told to end or is interrupted
     */
    @Override
    public void end(boolean interrupted)
    {
        
    }

    /**
     * Creates an isFinished condition if needed
     */
    @Override
    public boolean isFinished()
    {
        return false;
    }

}