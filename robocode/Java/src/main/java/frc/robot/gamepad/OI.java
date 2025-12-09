package frc.robot.gamepad;

// Import the joystick class
import edu.wpi.first.wpilibj.Joystick;

public class OI
{
    // Create the joystick
    Joystick drivePad;

    public OI()
    {
        // Initialize the joystick
        drivePad = new Joystick(GamepadConstants.DRIVE_USB_PORT);
    }

    /**
     * Drive Controller
     */

    /**
     * @return the y-axis value from the drivePad right joystick
     */
    public double getRightDriveY()
    {
        double joy = drivePad.getRawAxis(GamepadConstants.RIGHT_STICK_Y);
        if (Math.abs(joy) < 0.05)
            return 0.0;
        else
            return joy;
    }

    /**
     * @return the x-axis value from the drivePad right joystick
     */
    public double getRightDriveX()
    {
        double joy = drivePad.getRawAxis(GamepadConstants.RIGHT_STICK_X);
        if (Math.abs(joy) < 0.05)
            return 0.0;
        else
            return joy;
    }

    /**
     * @return the y-axis value from the drivePad left joystick
     */
    public double getLeftDriveY()
    {
        double joy = drivePad.getRawAxis(GamepadConstants.LEFT_STICK_Y);
        if (Math.abs(joy) < 0.05)
            return 0.0;
        else
            return joy;
    }

    /**
     * @return the x-axis value from the drivePad left joystick
     */
    public double getLeftDriveX()
    {
        double joy = drivePad.getRawAxis(GamepadConstants.LEFT_STICK_X);
        if (Math.abs(joy) < 0.05)
            return 0.0;
        else
            return joy;
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveRightTrigger()
    {
        return drivePad.getRawAxis(GamepadConstants.RIGHT_TRIGGER) > 0.05;
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveRightBumper()
    {
        return drivePad.getRawButton(GamepadConstants.RIGHT_BUMPER);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveLeftTrigger()
    {
        return drivePad.getRawAxis(GamepadConstants.LEFT_TRIGGER) > 0.05;
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveLeftBumper()
    {
        return drivePad.getRawButton(GamepadConstants.LEFT_BUMPER);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveAButton()
    {
        return drivePad.getRawButton(GamepadConstants.A_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveBButton()
    {
        return drivePad.getRawButton(GamepadConstants.B_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveXButton()
    {
        return drivePad.getRawButton(GamepadConstants.X_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveYButton()
    {
        return drivePad.getRawButton(GamepadConstants.Y_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveBackButton()
    {
        return drivePad.getRawButton(GamepadConstants.BACK_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveStartButton()
    {
        return drivePad.getRawButton(GamepadConstants.START_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveLeftStickButton()
    {
        return drivePad.getRawButton(GamepadConstants.LEFT_STICK_BUTTON);
    }

    /**
     * @return a true or false depending on the input
     */
    public boolean getDriveRightStickButton()
    {
        return drivePad.getRawButton(GamepadConstants.RIGHT_STICK_BUTTON);
    }

    /**
     * @return the current D-Pad direction
     */
    public int getDPad()
    {
        return drivePad.getPOV();
    }
}
