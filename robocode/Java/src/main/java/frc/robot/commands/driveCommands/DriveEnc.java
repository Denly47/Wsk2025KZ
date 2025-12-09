package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class DriveEnc extends CommandBase {

    private static final DriveTrain drive = RobotContainer.drive;

    private final double leftSetpoint;
    private final double rightSetpoint;
    private final double yawSetpoint;

    private final PIDController leftPid;
    private final PIDController rightPid;
    private final PIDController yawPid;

    private double startTime;
    private double allowedTime;

    private static final double MAX_SPEED = 400.0; // мм/с — подбери под робота

    // === Плавный старт ===
    private double currentLeft = 0.0;
    private double currentRight = 0.0;
    private double currentYaw = 0.0;
    private double lastTime = 0.0;

    private static final double DRIVE_RAMP = 0.9; // макс. изменение скорости/сек
    private static final double YAW_RAMP = 1.2;   // макс. изменение yaw/сек

    // === Номинальное напряжение аккумулятора ===
    private static final double NOMINAL_VOLTAGE = 12.0;

    public DriveEnc(double leftSetpoint, double rightSetpoint,
                    double epsilonLeft, double epsilonRight,
                    double yawSetpoint, double epsilonYaw) {
        this.leftSetpoint = leftSetpoint;
        this.rightSetpoint = rightSetpoint;
        this.yawSetpoint = yawSetpoint;

        addRequirements(drive);

        leftPid = new PIDController(0.0018, 0.0, 0.0);
        leftPid.setTolerance(epsilonLeft);

        rightPid = new PIDController(0.0018, 0.0, 0.0);
        rightPid.setTolerance(epsilonRight);

        yawPid = new PIDController(0.07, 0.0, 0.0);
        yawPid.setTolerance(epsilonYaw);
        yawPid.enableContinuousInput(-180.0, 180.0);
    }

    @Override
    public void initialize() {
        drive.resetEncoders();
        leftPid.reset();
        rightPid.reset();
        yawPid.reset();

        double maxDist = Math.max(Math.abs(leftSetpoint), Math.abs(rightSetpoint));
        allowedTime = 1.0 + (maxDist / MAX_SPEED) * 2.5;
        startTime = Timer.getFPGATimestamp();
        lastTime = startTime;

        currentLeft = 0.0;
        currentRight = 0.0;
        currentYaw = 0.0;
    }

    @Override
    public void execute() {
        double now = Timer.getFPGATimestamp();
        double dt = now - lastTime;
        lastTime = now;

        // === Считывание напряжения аккумулятора ===
        double batteryVoltage = RobotController.getBatteryVoltage();
        double batteryComp = NOMINAL_VOLTAGE / Math.max(9.0, batteryVoltage); // защита от деления на малое

        double leftOutput = MathUtil.clamp(
                leftPid.calculate(drive.getLeftEncoderDistance(), leftSetpoint),
                -0.7, 0.7
        );

        double rightOutput = MathUtil.clamp(
                rightPid.calculate(drive.getRightEncoderDistance(), rightSetpoint),
                -0.7, 0.7
        );

        double yawCorrection = MathUtil.clamp(
                yawPid.calculate(drive.getYaw(), yawSetpoint),
                -0.4, 0.4
        );

        // === Минимальная мощность ===
        double minPower = 0.05;
        if (Math.abs(leftOutput) > 0.01)
            leftOutput += Math.copySign(minPower, leftOutput);
        if (Math.abs(rightOutput) > 0.01)
            rightOutput += Math.copySign(minPower, rightOutput);

        // === Плавная рампа ===
        currentLeft = rampToTarget(currentLeft, leftOutput, DRIVE_RAMP, dt);
        currentRight = rampToTarget(currentRight, rightOutput, DRIVE_RAMP, dt);
        currentYaw = rampToTarget(currentYaw, yawCorrection, YAW_RAMP, dt);

        // === Коррекция под напряжение ===
        double leftComp = currentLeft * batteryComp;
        double rightComp = currentRight * batteryComp;
        double yawComp = currentYaw * batteryComp * 0.8; // yaw чуть мягче

        // === Применение ===
        drive.setTankSpeed(rightComp, leftComp, yawComp);

        // === Диагностика ===
        System.out.printf("Voltage=%.2fV  Comp=%.2f  L=%.2f  R=%.2f%n",
                batteryVoltage, batteryComp, leftComp, rightComp);
    }

    @Override
    public void end(boolean interrupted) {
        drive.setTankSpeed(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        boolean doneLeft = leftPid.atSetpoint();
        boolean doneRight = rightPid.atSetpoint();
        boolean doneYaw = yawPid.atSetpoint();

        double leftError = Math.abs(leftSetpoint - drive.getLeftEncoderDistance());
        double rightError = Math.abs(rightSetpoint - drive.getRightEncoderDistance());
        double remainingDist = Math.max(leftError, rightError);

        double elapsed = Timer.getFPGATimestamp() - startTime;
        boolean timeExceeded = elapsed > allowedTime;

        System.out.println("remaining: " + remainingDist + " мм");
        System.out.println("elapsed: " + elapsed + " / " + allowedTime);

        return (doneLeft && doneRight && doneYaw) || timeExceeded;
    }

    // ==================
    // Плавная рампа
    // ==================
    private double rampToTarget(double current, double target, double rate, double dt) {
        double diff = target - current;
        double maxStep = rate * dt;
        if (Math.abs(diff) <= maxStep) return target;
        return current + Math.copySign(maxStep, diff);
    }
}
