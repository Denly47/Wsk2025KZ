package frc.robot.subsystems;

//Vendor imports
import com.studica.frc.TitanQuad;
import com.studica.frc.TitanQuadEncoder;
import com.kauailabs.navx.frc.AHRS;
import com.studica.frc.Cobra;
import com.studica.frc.Lidar;
import com.studica.frc.Servo;
import com.studica.frc.ServoContinuous;

import edu.wpi.first.wpilibj.AnalogInput;
//WPI imports
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.networktables.NetworkTableEntry;
//import the DigitalInput Library

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;

/**
 * DriveTrain class 
 * <p>
 * This class creates the instance of the Titan and enables and sets the speed of the defined motor. 
 */
public class DriveTrain extends SubsystemBase
{
    /**
     * Motors
     */
    private TitanQuad LeftMotor, RightMotor, BackMotor, LiftMotor;


    private Cobra cobra;
    
      /**
     * Encoders
     */
    private TitanQuadEncoder leftEncoder, rightEncoder, backEncoder, liftEncoder;
    private DigitalOutput led;

    /**
     * Servo
     */
    private Servo servoC, Claw, servoY;


    private DigitalInput Elevator_max, Elevator_min;
    /**
     * Sensor
     */
    private AHRS navx;

    // //Create the DigitalInput Object
    // private DigitalInput Switch_Elevator_min, Switch_Elevator_max;

        // Lidar Library
   private Lidar lidar;
        // Lidar Scan Data Storage Class
    private Lidar.ScanData scanData;
        // Dashboard flag to prevent updating when not scanning
    public boolean scanning = false;

       private Ultrasonic sonar;
       private DigitalOutput startled, stopled, star;
       private DigitalInput startBtn, stopBtn;
    // private Ultrasonic rightSonar, leftSonar;
    private AnalogInput rightLight, leftLight;

    private ShuffleboardTab tab = Shuffleboard.getTab("Training Robot"); 
    private NetworkTableEntry leftEncoderValue = tab.add("Left Encoder", 0)
                                                    .getEntry();
    private NetworkTableEntry rightEncoderValue = tab.add("Right Encoder", 0)
                                                    .getEntry();  
    private NetworkTableEntry backEncoderValue = tab.add("Back Encoder", 0)
                                                    .getEntry(); 
                                                    
                                                    
    private NetworkTableEntry gyroValue = tab.add("NavX Yaw", 0)
                                                     .getEntry();
    // private NetworkTableEntry Switch_min = tab.add("Switch Elevator Min", 0)
    //                                                 .getEntry();
    // private NetworkTableEntry Switch_max = tab.add("Switch Elevator Max", 0)
    //                                                 .getEntry(); 
                                                    
    // private NetworkTableEntry LeftVelocity = tab.add("Left SpeedMMperSec", 0)
    //                                                 .getEntry();
    // private NetworkTableEntry RightVelocity = tab.add("Right SpeedMMperSec", 0)
    //                                                 .getEntry(); 
    /**
     * Sensors
     */
    // private NetworkTableEntry Ultra = tab.add("Left Ultrasonic ", 0).getEntry();
    // private NetworkTableEntry Ultra1 = tab.add("Right Ultrasonic ", 0).getEntry();
    private NetworkTableEntry Ik = tab.add("Left  IR ", 0).getEntry();
    private NetworkTableEntry Ik1 = tab.add("Right  IR ", 0).getEntry();

    private NetworkTableEntry S0 = tab.add("Left  S0 ", 0).getEntry();
    private NetworkTableEntry S3 = tab.add("Right  S2 ", 0).getEntry();

    float s0;
    float s3;
    
    /**
     * Constructor
     */
    public DriveTrain() 
    {
                /**
         * Top USB 2.0 port of VMX = kUSB1
         * Bottom USB 2.0 port of VMX = kUSB2
         */
        lidar = new Lidar(Lidar.Port.kUSB1); //Lidar will start spinning the moment this is called
        //Motors
        RightMotor = new TitanQuad(Constants.TITAN_ID, Constants.RIGHT_MOTOR);
        LeftMotor =  new TitanQuad(Constants.TITAN_ID, Constants.LEFT_MOTOR);
        BackMotor = new TitanQuad(Constants.TITAN_ID, Constants.BACK_MOTOR);

        LiftMotor = new TitanQuad(Constants.TITAN_ID, Constants.LIFT_MOTOR);

        LeftMotor.setInverted(false);
        RightMotor.setInverted(false);
        BackMotor.setInverted(false);

       //Servo 
        servoC = new Servo(Constants.SERVO_X);
        Claw = new Servo(Constants.CLAW);
        servoY = new Servo(Constants.SERVO_Y);

       
        navx = new AHRS(SPI.Port.kMXP);
          //Sensors
          rightEncoder = new TitanQuadEncoder(RightMotor, Constants.RIGHT_MOTOR, Constants.distancePerTick);
          leftEncoder = new TitanQuadEncoder(LeftMotor, Constants.LEFT_MOTOR, Constants.distancePerTick);
          backEncoder = new TitanQuadEncoder(BackMotor, Constants.BACK_MOTOR, Constants.distancePerTick);
          liftEncoder = new TitanQuadEncoder(LiftMotor, Constants.LIFT_MOTOR, Constants.ElevatorDist);

          rightEncoder.setReverseDirection();
        //   backEncoder.setReverseDirection();

          //Constuct a new instance
        Elevator_min = new DigitalInput(11);
        Elevator_max = new DigitalInput(10);

        startBtn    = new DigitalInput(8); 
        stopBtn    = new DigitalInput(9); 
    
        startled    = new DigitalOutput(17); 
        star    = new DigitalOutput(19);
        stopled    = new DigitalOutput(16); 
        setStarLed(false);
        setStartedBtnLed(false);
        setStopBtnLed(false);
        // led    = new DigitalOutput(8); 

        // rightSonar = new Ultrasonic(10, 11);
        // leftSonar = new Ultrasonic(8, 9);
        // rightSonar.setAutomaticMode(false);
        // leftSonar.setAutomaticMode(true);

        rightLight = new AnalogInput(0);
        leftLight = new AnalogInput(1);
        
        cobra = new Cobra();
    }

    public boolean getStartBtn() {
        return startBtn.get();
    }
    public boolean getStopBtn() {
        return stopBtn.get();
    }
    public void setStarLed(boolean bb) {
    
        star.set(bb);
    }
    public void setStopBtnLed(boolean bb) {
    
        stopled.set(bb);
    }
    public void setStartedBtnLed(boolean b) {
        
        startled.set(b);
    }
    public boolean getStopBtnLed() {
    
       return stopled.get();
    }
      /**
     * Starts the lidar if it was stopped
     */
    public void startScan()
    {
        lidar.start();
        scanning = true;
    }

    /**
     * Stops the lidar if needed. This will reduce the overhead of CPU and RAM by very little. 
     */
    public void stopScan()
    {
        lidar.stop();
        scanning = false;
    }
    public void setTankSpeed(double rightspeed, double leftspeed, double yaw) {
        RightMotor.set(-rightspeed );
        LeftMotor.set(leftspeed );
        BackMotor.set(yaw);
    }
       /**
     * Sets the angle of the Y servo
     * <p>
     * @param angle угол в градусах (0–300, зависит от модели серво)
     */
    public void setServoYAngle(double angle) {
        servoY.setAngle(angle);
    }

    /**
     * Gets the current angle of the Y servo
     * <p>
     * @return текущий угол сервопривода
     */
    public double getServoYAngle() {
        return servoY.getAngle();
    }

    /**
     * Sets the speed of the motor
     * <p>
     * @param speed range -1 to 1 (0 stop)
     */
    public void holonomicDrive(double x, double y, double z) {
        
        double rightSpeed = ((x / 3) - (y / Math.sqrt(3)) + z) * Math.sqrt(3);
        double leftSpeed = ((x / 3) + (y / Math.sqrt(3)) + z) * Math.sqrt(3);
        double backSpeed = (-3.68 * x / 3) + z;
    
        double max = Math.max(Math.abs(rightSpeed),
                     Math.max(Math.abs(leftSpeed), Math.abs(backSpeed)));
    
        if (max > 1.0) {
            rightSpeed /= max;
            leftSpeed  /= max;
            backSpeed  /= max;
        }
    
        RightMotor.set(rightSpeed);
        LeftMotor.set(leftSpeed);
        BackMotor.set(backSpeed);
    }

    public void setTankSpeed(double rightspeed, double leftspeed)
    {
        RightMotor.set(-rightspeed);
        LeftMotor.set(leftspeed);
    }
    public double getCobraS0() {
        return s0 = cobra.getVoltage(0);
    }
    public double getCobraS3() {
        return s3 = cobra.getVoltage(3);
    }
    /**
    * Ultrasonic Sensor
    */
    // public double getRightSonic()
    // {
    // return rightSonar.getRangeMM();
    // }
    // public double getLeftSonic()
    // {
    // return leftSonar.getRangeMM();
    // }
    /**
     * IR Sensor
     */
    public double getrightLight()
    {
    return (Math.pow(rightLight.getAverageVoltage(), -1.2045)) * 27.726;
    }
    public double getleftLight()
    {
    return (Math.pow(leftLight.getAverageVoltage(), -1.2045)) * 27.726;
    }

    /**
     * Sets the speed of the continuous servo motor
     * <p>
     * @param speed range -1 to 1 (0 stop)
     */
    public void setArmAngle(double ServoC)
    {
        servoC.setAngle(ServoC);
    }
    public void setClawAngle(double claw)
    {
        Claw.setAngle(claw);
    }

    public double getRightEncoderDistance()
    {
        return rightEncoder.getEncoderDistance();
    }

    public double getLeftEncoderDistance()
    {
        return leftEncoder.getEncoderDistance();
    }

    public double getBackEncoderDistance()
    {
        return backEncoder.getEncoderDistance();
    }
    public double getLiftEncoderDistance()
    {
        return liftEncoder.getEncoderDistance();
    }
    

    public double getFrontLidar() { return scanData.distance[180]; // 90° вправо
}

    public double getRightLidar() {   return scanData.distance[270]; // 90° вправо
    }
    
    public double getLeftLidar() {    return scanData.distance[90]; // 270° влево
    }

    public double getBackLidar() {    return scanData.distance[0]; // 180° влево
    }



    // SmartDashboard.putNumber("Lidar Front", scanData.distance[180]);
    // SmartDashboard.putNumber("Lidar Right", scanData.distance[270]);
    // SmartDashboard.putNumber("Lidar Back", scanData.distance[0]);
    // SmartDashboard.putNumber("Lidar Left", scanData.distance[90]);
    // SmartDashboard.putNumber("Lidar Konec", scanData.distance[160]);

    public void StartLidar()  { lidar.start(); scanning = true; }
    public void StopLidar() { lidar.stop(); scanning = false; }

    public double getYaw(){ return navx.getYaw(); }
    public void resetYaw(){    navx.zeroYaw();}

    // public boolean getElevatorMin()
    // {
    //     return Switch_Elevator_min.get();
    // }

    public boolean getElevatorMax(){ return Elevator_max.get(); }

    public boolean getElevatorMin(){ return Elevator_min.get(); }
    /**
     * Resets all encoders
     */
    public void resetEncoders() { leftEncoder.reset(); rightEncoder.reset(); backEncoder.reset(); }
    public void liftReset(){ liftEncoder.reset(); }

    public double getAverageForwardEncoderDistance()
    {
        return (getLeftEncoderDistance() + getRightEncoderDistance()) / 2; 
    }

    public void setElevatorSpeed(double speed)
    {
        LiftMotor.set(speed);
    }
    

        // Где-нибудь в public-секции DriveTrain:



    /**
     * Gets the RPM of the motor
     * <p>
     * @return the RPM of the motor
     */
    public double getLeftSpeedMMperSec() {
        double rpm = LeftMotor.getRPM(); // об/мин
        double rps = rpm / 60.0; // об/с
        return rps * (2 * Math.PI * Constants.wheelRadius); // мм/с
    }
    
    public double getRightSpeedMMperSec() {
        double rpm = RightMotor.getRPM();
        double rps = rpm / 60.0;
        return rps * (2 * Math.PI * Constants.wheelRadius);
    }

    public void Scan()
    {
        lidar.start();
        scanning = true;
        scanData = lidar.getData();
    }

    public double getLidarSector(int startAngle, int endAngle) {
        if (scanData == null || scanData.distance == null) {
            return Double.NaN;
        }
    
        // Нормализуем диапазон (360° — круг)
        startAngle = ((startAngle % 360) + 360) % 360;
        endAngle = ((endAngle % 360) + 360) % 360;
    
        double sum = 0;
        int count = 0;
    
        // Если сектор "не переворачивается" (например, 30°–90°)
        if (startAngle <= endAngle) {
            
            for (int i = startAngle; i <= endAngle; i++) {
                double d = scanData.distance[i];
                if (!Double.isNaN(d) && d > 0) {
                    sum += d;
                    count++;
                }
            }
        } else {
            // Если сектор "через 0°", например 350°–10°
            for (int i = startAngle; i < 360; i++) {
                double d = scanData.distance[i];
                if (!Double.isNaN(d) && d > 0) {
                    sum += d;
                    count++;
                }
            }
            for (int i = 0; i <= endAngle; i++) {
                double d = scanData.distance[i];
                if (!Double.isNaN(d) && d > 0) {
                    sum += d;
                    count++;
                }
            }
        }
    
        if (count == 0) return Double.NaN;
        return sum / count;
    }
    public double front(){ return scanData.distance[180]; }
    public double frontSector(){ return getLidarSector(170, 190); }
    public double leftSector(){ return getLidarSector(80, 100); }
    public double rightSector(){ return getLidarSector(260, 280); }
    public double backSector(){ return getLidarSector(350, 10); }
// Диагонали (45°)
public double frontRightSector() { return getLidarSector(210, 230); }    // вперед-вправо210, 230
public double frontLeftSector()  { return getLidarSector(120, 150); }  // вперед-влево
public double backRightSector()  { return getLidarSector(310, 330); }  // назад-вправо310, 330
public double backLeftSector()   { return getLidarSector(30, 50); }  // назад-влево


    //D = diagonal , S = sector, F = Front, L = left 

    public double DSBR(){ return getLidarSector(170, 100); }

    // public double frontSector(){ return getLidarSector(170, 190); }
    // public double leftSector(){ return getLidarSector(80, 100); }
    // public double rightSector(){ return getLidarSector(260, 280); }
    // public double backSector(){ return getLidarSector(350, 10); }

    // public double frontSector(){ return getLidarSector(260, 280); }
    // public double leftSector(){ return getLidarSector(170, 190); }
    // public double rightSector(){ return getLidarSector(350, 10); }
    // public double backSector(){ return getLidarSector(80, 100); }

    public double KonecLeft(){  return scanData.distance[160]; }
    public double KonecRight(){  return scanData.distance[206]; }

        @Override
    public void periodic() {

        
        // Ultra.setDouble(getRightSonic());
        // Ultra1.setDouble(getLeftSonic());
        // System.out.println(" SO" + getCobraS0());
        
        // System.out.println(" S3" + getCobraS3());
        S0.setDouble(getCobraS0());
        S3.setDouble(getCobraS3());
        Ik.setDouble(getleftLight());
        Ik1.setDouble(getrightLight());
        // System.out.println(getRightSonic());
        // System.out.println(getLeftSonic());
        // if (!input.get()) {
        //     led.set(false);   // нажатие выключает
        // } else {
        //     led.set(true);    // отпущено → горит
        // }
      
        // Движение
        leftEncoderValue.setDouble(getLeftEncoderDistance());
        rightEncoderValue.setDouble(getRightEncoderDistance());
        backEncoderValue.setDouble(getBackEncoderDistance());
        // LeftVelocity.setDouble(getLeftSpeedMMperSec());
        // RightVelocity.setDouble(getRightSpeedMMperSec());
        // SmartDashboard.putNumber("Average Forward Distance", getAverageForwardEncoderDistance());

        // Навигация
        gyroValue.setDouble(getYaw());
        SmartDashboard.putNumber("Pitch", navx.getPitch());
        SmartDashboard.putNumber("Roll", navx.getRoll());
        SmartDashboard.putBoolean("NavX Connected", navx.isConnected());

        // // Лифт
        SmartDashboard.putNumber("Lift Encoder", getLiftEncoderDistance());
        SmartDashboard.putBoolean("Elevator_Max", Elevator_max.get());
        SmartDashboard.putBoolean("Elevator_Min", Elevator_min.get());
        // Switch_min.setBoolean(!getElevatorMin());

      //  System.out.println(Elevator_max.get());
        // System.out.println(Switch_Elevator_min.get());
        // System.out.println(Switch_Elevator_max.getChannel());
        

        // Лидар
        if (scanning) {
            scanData = lidar.getData();
            SmartDashboard.putNumber("Lidar Front", scanData.distance[180]);
            SmartDashboard.putNumber("Lidar Right", scanData.distance[270]);
            SmartDashboard.putNumber("Lidar Back", scanData.distance[0]);
            SmartDashboard.putNumber("Lidar Left", scanData.distance[90]);
            SmartDashboard.putNumber("Lidar Konec", scanData.distance[160]);

            SmartDashboard.putNumber("Lidar Sector Front", frontSector());
            SmartDashboard.putNumber("Lidar Sector Right", rightSector());
            SmartDashboard.putNumber("Lidar Sector Back", backSector());
            SmartDashboard.putNumber("Lidar Sector Left", leftSector());

            SmartDashboard.putNumber("Lidar Sector Front RIGHt", frontRightSector());
            SmartDashboard.putNumber("Lidar Sector FRONT LEFT", frontLeftSector());
            SmartDashboard.putNumber("Lidar Sector BackRIGHT", backRightSector());
            SmartDashboard.putNumber("Lidar Sector BACKLeft", backLeftSector());
        }
    }


}