package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class Sensor_Lidar extends CommandBase {
    private final DriveTrain drive = RobotContainer.drive;

    // --- целевые значения ---
    private final double targetRightX;
    private final double targetLeftX;
    private final double targetFrontY;
    private final double targetBackY;
    private final double targetYaw;

    // --- PID контроллеры ---
    private final PIDController pidX;
    private final PIDController pidY;
    private final PIDController pidYaw;

    // === Конструктор ===
    public Sensor_Lidar(double targetRightX, double targetLeftX, double epsilonX,
                  double targetFrontY, double targetBackY, double epsilonY,
                  double targetYaw, double epsilonYaw) {
        this.targetRightX = targetRightX;
        this.targetLeftX = targetLeftX;
        this.targetFrontY = targetFrontY;
        this.targetBackY = targetBackY;
        this.targetYaw = targetYaw;

        // PID по X (боковое смещение)
        pidX = new PIDController(0.007, 0, 0.0);
        pidX.setTolerance(epsilonX);

        // PID по Y (вперёд-назад)
        pidY = new PIDController(0.0038, 0, 0.0);
        pidY.setTolerance(epsilonY);

        // PID по углу (Yaw)
        pidYaw = new PIDController(0.02, 0, 0.0);
        pidYaw.setTolerance(epsilonYaw);
        pidYaw.enableContinuousInput(-180.0, 180.0);

        addRequirements(drive);
    }

    @Override
    public void initialize() {
        drive.startScan();
        drive.StartLidar();
        drive.resetEncoders();

        pidX.reset();
        pidY.reset();
        pidYaw.reset();
    }

    @Override
    public void execute() {
        System.out.println("SESNOR WORK");
        double xSpeed, ySpeed;

        // --- боковая коррекция X ---
        if (targetRightX != 0) {
            xSpeed = -MathUtil.clamp(
                pidX.calculate(drive.getRightLidar(), targetRightX),
                -0.6, 0.6
            );
        } else if (targetLeftX != 0) {
            xSpeed = MathUtil.clamp(
                pidX.calculate(drive.getLeftLidar(), targetLeftX),
                -0.6, 0.6
            );
        } else {
            xSpeed = 0.0;
        }

        // --- продольная коррекция Y ---
        if (targetFrontY != 0) {
            ySpeed = MathUtil.clamp(
                pidY.calculate(drive.getFrontLidar(), targetFrontY),
                -0.6, 0.6
            );
        } else if (targetBackY != 0) {
            ySpeed = -MathUtil.clamp(
                pidY.calculate(drive.getBackLidar(), targetBackY),
                -0.6, 0.6
            );
        } else {
            ySpeed = 0.0;
        }

        // --- поворот ---
        double rotation = MathUtil.clamp(
            pidYaw.calculate(drive.getYaw(), targetYaw),
            -0.5, 0.5
        );

        // движение напрямую без плавности
        drive.holonomicDrive(xSpeed, -ySpeed, rotation);

        System.out.printf("X=%.2f | Y=%.2f | Rot=%.2f\n", xSpeed, ySpeed, rotation);
    }

    @Override
    public void end(boolean interrupted) {
        drive.holonomicDrive(0, 0, 0);
  
    }

    @Override
    public boolean isFinished() {
        boolean xDone = (targetRightX != 0 || targetLeftX != 0) ? pidX.atSetpoint() : true;
        boolean yDone = (targetFrontY != 0 || targetBackY != 0) ? pidY.atSetpoint() : true;
        boolean yawDone = pidYaw.atSetpoint();

        return xDone && yDone && yawDone;
    }
}
