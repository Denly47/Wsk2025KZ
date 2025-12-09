package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class strafe extends CommandBase {
    private static final DriveTrain drive = RobotContainer.drive;

    private final double setpointDistance;
    private final double setpointYaw; 

    private final PIDController pidXAxis;
    private final PIDController pidZAxis;

    // защита по времени
    private double startTime;
    private double allowedTime;
    private static final double MAX_SPEED = 700.0; // мм/с — подбери под своего робота

    // === переменные для плавности ===
    private double currentStrafe = 0.0;
    private double currentYaw = 0.0;
    private double lastTime = 0.0;

    private static final double STRAFE_RAMP = 0.5; // скорость изменения по X (ед/сек)
    private static final double YAW_RAMP = 0.7;    // скорость изменения yaw (ед/сек)

    public strafe(double setpointDistance, double epsilonDistance,
                  double setpointYaw, double epsilonYaw) {
        this.setpointDistance = setpointDistance;
        this.setpointYaw = setpointYaw;
        addRequirements(drive);

        pidXAxis = new PIDController(-0.01, 0, 0);
        pidXAxis.setTolerance(epsilonDistance);

        pidZAxis = new PIDController(0.02, 0, 0.0);
        pidZAxis.setTolerance(epsilonYaw);
        pidZAxis.enableContinuousInput(-180.0, 180.0);
    }

    @Override
    public void initialize() {
        drive.resetEncoders();
        pidXAxis.reset();
        pidZAxis.reset();

        allowedTime = 1.0 + (Math.abs(setpointDistance) / MAX_SPEED) * 2.5;
        startTime = Timer.getFPGATimestamp();
        lastTime = startTime;

        currentStrafe = 0.0;
        currentYaw = 0.0;
    }

    @Override
    public void execute() {
        double now = Timer.getFPGATimestamp();
        double dt = now - lastTime;
        lastTime = now;
    
        double distError = setpointDistance - drive.getBackEncoderDistance();
    
        // --- PID расчёт ---
        double strafeOutput = MathUtil.clamp(
            pidXAxis.calculate(drive.getBackEncoderDistance(), setpointDistance),
            -0.6, 0.6
        );
    
        double yawCorrection = MathUtil.clamp(
            pidZAxis.calculate(drive.getYaw(), setpointYaw),
            -0.8, 0.8
        );
    
        // --- правило: если далеко от цели -> ramp, если близко -> прямой PID ---
        if (Math.abs(distError) > 150) {
            // далеко -> плавный разгон
            currentStrafe = rampToTarget(currentStrafe, strafeOutput, STRAFE_RAMP, dt);
        } else {
            // близко -> доверяем PID напрямую (чтобы он смог затормозить)
            currentStrafe = strafeOutput;
        }
    
        currentYaw = rampToTarget(currentYaw, yawCorrection, YAW_RAMP, dt);
    
        drive.holonomicDrive(currentStrafe, 0.0, currentYaw);
    }
    
    @Override
    public void end(boolean interrupted) {
        drive.holonomicDrive(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        boolean doneX = pidXAxis.atSetpoint();
        boolean doneYaw = pidZAxis.atSetpoint();

        double distError = Math.abs(setpointDistance - drive.getBackEncoderDistance());

        double elapsed = Timer.getFPGATimestamp() - startTime;
        boolean timeExceeded = elapsed > allowedTime;

        System.out.printf("strafe error=%.1f мм | elapsed=%.2f / %.2f\n",
            distError, elapsed, allowedTime);

        return (doneX && doneYaw) || timeExceeded;
    }

    // ===== Ramp функция =====
    private double rampToTarget(double current, double target, double rate, double dt) {
        double diff = target - current;
        double maxStep = rate * dt;
        if (Math.abs(diff) <= maxStep) {
            return target;
        }
        return current + Math.copySign(maxStep, diff);
    }
}
