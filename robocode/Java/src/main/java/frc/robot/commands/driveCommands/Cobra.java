package frc.robot.commands.driveCommands;

//WPI imports
import edu.wpi.first.wpilibj2.command.CommandBase;

//RobotContainer import
import frc.robot.RobotContainer;

//Subsystem imports
import frc.robot.subsystems.DriveTrain;

/**
 * SimpleDrive class
 * <p>
 * This class drives a motor 
 */
public class Cobra extends CommandBase
{
    //Grab the subsystem instance from RobotContainer
    private static final DriveTrain drive = RobotContainer.drive;

    double strafe, forward, rotation;
    boolean stop = false;

    /**
     * Constructor
     */
    public Cobra(double strafe, double forward, double rotation)
    {
        addRequirements(drive); // Adds the subsystem to the command
        this.strafe = strafe;
        this.forward = forward;
        this.rotation = rotation;
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
        drive.holonomicDrive(strafe, forward, rotation);

        if (drive.getCobraS3() >= 4.9){
            stop = true;

        } 
        else {
            stop = false;
        }
        System.out.println("Start work");
    }

    /**
     * Called when the command is told to end or is interrupted
     */
    @Override
    public void end(boolean interrupted)
    {
        System.out.println("ENDDDDDD work");
    }

    /**
     * Creates an isFinished condition if needed
     */
    @Override
    public boolean isFinished()
    {
        return stop;
    }

}