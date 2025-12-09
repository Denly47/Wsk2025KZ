package frc.robot;


public final class Constants
{
    /**
     * Motor Constants
     */
    public static final int TITAN_ID        = 42;

    public static final int RIGHT_MOTOR      = 2;
    public static final int BACK_MOTOR     = 0;
    public static final int LEFT_MOTOR      = 3;

    public static final int LIFT_MOTOR      = 1;

    /**
     * Servo Constants
     */
    public static final int SERVO_Y         = 2;
    public static final int SERVO_X         = 1;
    public static final int CLAW         = 0;

    /**
     * Digital Input Constants
     */
    // public static final int DI       = 0;
    // public static final int DI_1     = 2;
    // public static final int DI_2     = 2;
    //  public static final int DI_3     = 3;
    // public static final int DI_4     = 4;
    // public static final int DI_5     = 17;
    // public static final int DI_6     = 18;
    // public static final int DI_7     = 19;
    // public static final int DI_8     = 20;
    // public static final int DI_9     = 21;


    /**
     * Encoder Constants
     */

    //Radius of drive wheel in mm
    public static final double wheelRadius             = 62.5;

    //Encoder pulses per rotation of motor shaft
    public static final int pulsePerRotation        = 1464;

    //Gear ratio between motor shaft and output shaft
    public static final double gearRatio            = 1/1;

    //Pulse per rotation combined with gear ratio
    public static final double encoderPulseRatio    = pulsePerRotation * gearRatio;

    //Distance per tick
    public static final double distancePerTick      = (Math.PI * 2 * wheelRadius) / encoderPulseRatio;

    
    //Radius of drive wheel in mm
    public static final double wheelRadius1             = 0.075;

    //Encoder pulses per rotation of motor shaft
    public static final int pulsePerRotation1        = 1464;

    //Gear ratio between motor shaft and output shaft
    public static final double gearRatio1            = 1/1;

    //Pulse per rotation combined with gear ratio
    public static final double encoderPulseRatio1    = pulsePerRotation * gearRatio;

    //Distance per tick
    public static final double ElevatorDist      = (Math.PI * 2 * wheelRadius) / encoderPulseRatio;
}
