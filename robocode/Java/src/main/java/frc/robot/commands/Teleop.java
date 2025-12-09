package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.RobotContainer;
import frc.robot.gamepad.OI;
import frc.robot.subsystems.DriveTrain;

public class Teleop extends CommandBase {
    private static final DriveTrain driveTrain = RobotContainer.drive;
    private static final OI oi = RobotContainer.oi;

    // джойстик
    double inputLeftY = 0;
    double inputLeftX = 0;
    double inputRightX = 0;

    // кнопки
    boolean up = false;
    boolean down = false;
    double elevator = 0;

    boolean Xbutton;
    boolean Ybutton;
    boolean Abutton;
    boolean Bbutton;

    // флажки для серво
    private boolean clawOpen = false;
    private boolean armUp = false;
    private boolean servoYActive = false;

    // чтобы отслеживать одиночное нажатие (edge detection)
    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevA = false;

    public Teleop() {
        addRequirements(driveTrain);
    }

    @Override
    public void initialize() {
        driveTrain.resetYaw();
    }

    @Override
    public void execute() {
        // --- чтение с геймпада ---
        inputLeftX  = -oi.getLeftDriveX();  // движение влево/вправо
        inputLeftY  = -oi.getLeftDriveY();  // движение вперёд/назад
        inputRightX = -oi.getRightDriveX(); // поворот

        up   = oi.getDriveRightBumper();
        down = oi.getDriveLeftBumper();

        Xbutton = oi.getDriveXButton();
        Ybutton = oi.getDriveYButton();
        Abutton = oi.getDriveAButton();
        Bbutton = oi.getDriveBButton();

        // --- лифт ---
        if (up && driveTrain.getElevatorMax()) {
            elevator = 0.5;
        } else if (down && driveTrain.getElevatorMin()) {
            elevator = -0.5;
        } else {
            elevator = 0;
        }
        driveTrain.setElevatorSpeed(elevator);

        // --- переключение Claw (по X) ---
        if (Xbutton && !prevX) {  // событие нажатия
            clawOpen = !clawOpen;
            if (clawOpen) {
                driveTrain.setClawAngle(240); // открыть
            } else {
                driveTrain.setClawAngle(0);  // закрыть
            }
        }

        // --- переключение Arm (по Y) ---
        if (Ybutton && !prevY) {
            armUp = !armUp;
            if (armUp) {
                driveTrain.setArmAngle(290); // выдвинуть
            } else {
                driveTrain.setArmAngle(80);   // задвинуть
            }
        }

        // --- переключение ServoY (по A) ---
        if (Abutton && !prevA) {
            servoYActive = !servoYActive;
            if (servoYActive) {
                driveTrain.setServoYAngle(190); // поднять
                System.out.print("200DDDD");
            } else {
                driveTrain.setServoYAngle(120);   // опустить
            }
        }

        // обновляем предыдущее состояние кнопок
        prevX = Xbutton;
        prevY = Ybutton;
        prevA = Abutton;

        // --- движение на омни-базе ---
        double scale = 0.9;
        double slow = 0.5;
        driveTrain.holonomicDrive(-inputLeftX * slow,   // x = вперёд/назад
                                  inputLeftY * scale,   // y = влево/вправо
                                  -inputRightX * slow); // z = поворот
    }

    @Override
    public void end(boolean interrupted) {
        driveTrain.holonomicDrive(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
