package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class TurnToAngle extends CommandBase {

    private static final DriveTrain drive = RobotContainer.drive;

    private final PIDController pidZAxis;
    private final double targetAngle;

    private double startTime;
    private double allowedTime;

    // Максимальная угловая скорость (примерно, град/с)
    private static final double MAX_TURN_SPEED = 90.0; // подбирай по факту

    // --- переменные для плавности ---
    private double currentZ = 0.0;
    private double lastTime = 0.0;
    private static final double Z_RAMP = 1.5; // скорость изменения сигнала (ед/сек)

    // --- напряжение аккумулятора ---
    private static final double NOMINAL_VOLTAGE = 12.0;

    public TurnToAngle(double targetAngle, double epsilonYaw) {
        this.targetAngle = targetAngle;
        addRequirements(drive);

        pidZAxis = new PIDController(0.01, 0.0, 0.00000);
        pidZAxis.setTolerance(epsilonYaw);
        pidZAxis.enableContinuousInput(-180.0, 180.0);
    }

    @Override
    public void initialize() {
        drive.resetEncoders();
        drive.resetEncoders();
        drive.liftReset();
        pidZAxis.reset();
        

        double angleError = Math.abs(targetAngle - drive.getYaw());
        allowedTime = 1.0 + (angleError / MAX_TURN_SPEED) * 2.5;

        startTime = Timer.getFPGATimestamp();
        lastTime = startTime;
        currentZ = 0.0;
    }

    @Override
    public void execute() {
        double now = Timer.getFPGATimestamp();
        double dt = now - lastTime;
        lastTime = now;

        double currentYaw = drive.getYaw();

        // === базовый PID ===
        double targetOutput = MathUtil.clamp(
                pidZAxis.calculate(currentYaw, targetAngle),
                -0.3, 0.3
        );

        // === компенсация батареи ===
        double batteryVoltage = RobotController.getBatteryVoltage();
        double batteryComp = NOMINAL_VOLTAGE / Math.max(9.0, batteryVoltage); // защита от деления на ноль
        double compensatedOutput = targetOutput * batteryComp;

        // === ramp для плавности ===
        currentZ = rampToTarget(currentZ, compensatedOutput, Z_RAMP, dt);

        // === применение ===
        drive.holonomicDrive(0.0, 0.0, currentZ);

        System.out.printf(
                "Yaw=%.1f | Target=%.1f | Out=%.3f | Ramp=%.3f | V=%.2fV | Comp=%.2f%n",
                currentYaw, targetAngle, targetOutput, currentZ, batteryVoltage, batteryComp
        );
    }

    @Override
    public void end(boolean interrupted) {
        // плавное торможение к нулю
        drive.holonomicDrive(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        boolean atTarget = pidZAxis.atSetpoint();

        double elapsed = Timer.getFPGATimestamp() - startTime;
        boolean timeExceeded = elapsed > allowedTime;

        System.out.println("elapsed: " + elapsed + " / " + allowedTime);

        return atTarget || timeExceeded;
    }

    // === Вспомогательная функция для плавности ===
    private double rampToTarget(double current, double target, double rate, double dt) {
        double diff = target - current;
        double maxStep = rate * dt;
        if (Math.abs(diff) <= maxStep) {
            return target;
        }
        return current + Math.copySign(maxStep, diff);
    }
}
