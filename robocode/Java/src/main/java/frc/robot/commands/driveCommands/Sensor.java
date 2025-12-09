package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class Sensor extends CommandBase {
    private final DriveTrain drive = RobotContainer.drive;

    private final double targetRightX;
    private final double targetLeftX;
    private final double targetLeftY;
    private final double targetRightY;
    private final double targetYaw;

    private final PIDController pidX;
    private final PIDController pidY;
    private final PIDController pidZAxis;
    double xSpeed;
    double ySpeed;
    double rotation;
    // double rotation;
    public Sensor(double targetRightX, double targetLeftX, double epsilonX, double targetLeftY, double targetRightY, 
    double epsilonY, double targetYaw,double epsilonYaw) {
        this.targetRightX = targetRightX;
        this.targetLeftY = targetLeftY;
        this.targetRightY = targetRightY;

        this.targetLeftX = targetLeftX;

        this.targetYaw = targetYaw;
        

        // PID по X (вбок)
        pidX = new PIDController(0.006, 0, 0.0);
        pidX.setTolerance(epsilonX);

        // PID по Y (вперёд)
        pidY = new PIDController(0.05, 0, 0.0);
        pidY.setTolerance(epsilonY);


        pidZAxis = new PIDController(0.0095, 0.0, 0.00000);
        pidZAxis.setTolerance(epsilonYaw);
        pidZAxis.enableContinuousInput(-180.0, 180.0);

        addRequirements(drive);
    }

    @Override
    public void initialize() {
        drive.resetEncoders();

        pidX.reset();
        pidY.reset();
        drive.StartLidar();
    }

    @Override
    public void execute() {
        if(targetRightX != 0){
            xSpeed = -MathUtil.clamp(pidX.calculate(targetRightX, drive.getRightLidar()), -0.6, 0.6);
        } else if(targetLeftX != 0){
            xSpeed = -MathUtil.clamp(pidX.calculate(drive.getLeftLidar(), targetLeftX), -0.6, 0.6);
        } else {
            xSpeed = 0.0;
        }
    if(targetLeftY != 0){
         ySpeed = MathUtil.clamp(pidY.calculate(drive.getleftLight(), targetLeftY), -1.0, 1.0);
    } else if(targetRightY != 0){
        ySpeed = MathUtil.clamp(pidY.calculate(drive.getrightLight(), targetRightY), -1.0, 1.0);
    } else {
        ySpeed = 0.0;
    }
    double targetOutput = MathUtil.clamp(
        pidZAxis.calculate(drive.getYaw(), targetYaw),
        -0.3, 0.3
);
    

        drive.holonomicDrive(-xSpeed, -ySpeed, targetOutput);
    }

    @Override
    public void end(boolean interrupted) {
        drive.holonomicDrive(0, 0, 0);
    }

    @Override
    public boolean isFinished() {
        boolean xDone = pidX.atSetpoint();
        boolean yDone = pidY.atSetpoint();
        boolean yawDone = pidZAxis.atSetpoint();
        System.out.printf("X: %.2f | Y: %.2f | Rot: %.2f\n", xSpeed, ySpeed, rotation);

        System.out.println("X done: " + xDone);
        System.out.println("Y done: " + yDone);
        System.out.println("Y done: " + yawDone);

        return xDone && yDone && yawDone;
    }
}
