// package frc.robot.commands.driveCommands;

// import edu.wpi.first.wpilibj2.command.CommandBase;
// import edu.wpi.first.wpiutil.math.MathUtil;
// import frc.robot.RobotContainer;
// import frc.robot.subsystems.Camera;
// import frc.robot.subsystems.DriveTrain;
// import edu.wpi.first.wpilibj.controller.PIDController;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// import edu.wpi.first.wpilibj.Timer;

// public class MazeNavigator extends CommandBase {

//     private static final DriveTrain drive = RobotContainer.drive;
//     private static final Camera camera = RobotContainer.camera;
//     private final PIDController pidZAxis;
//     private final double targetAngle;

//     private double forwardPower, strafePower;
//     private boolean isStrafingRight = false;
//     private boolean isStrafingLeft = false;

//     // --- Состояние восстановления ---
//     static int recoveryStep = 0;
//     double fg, g;
//     private double recoveryEndTime = 0;
//     private boolean allowDynamicAgain = true;

//     // --- Переменные состояния ---
//     private String lastDirection = "";
//     private String stableDir = "";
//     private int stableCount = 0;
//     private int repeatCount = 0;
//     private boolean dynamicMode = false;
//     private boolean recoveryMode = false;

//     private double forwardStartTime = 0;
//     private boolean forwardStarted = false;
//     private final double LR_BLOCK_TIME = 2.0; // время, на которое lrNorm = 0
//     boolean odnorazka = false;

//     // --- Память направления в динамическом режиме ---
//     private String dynamicMemoryDir = "";
//     private String currentMode = "NORMAL"; // Для отладки режима

//     public MazeNavigator(double targetAngle, double epsilonYaw) {
//         this.targetAngle = targetAngle;
//         addRequirements(drive);

//         pidZAxis = new PIDController(0.05, 0.0, 0.0000);
//         pidZAxis.setTolerance(epsilonYaw);
//         pidZAxis.enableContinuousInput(-180.0, 180.0);
//     }

//     @Override
//     public void initialize() {
//         drive.resetEncoders();
//         drive.resetYaw();
//         recoveryEndTime = 0;
//         allowDynamicAgain = true;
//         repeatCount = 0;
//         dynamicMode = false;
//         recoveryMode = false;
//         lastDirection = "";
//         stableDir = "";
//         stableCount = 0;
//         dynamicMemoryDir = "";
//         recoveryStep = 0;
//         currentMode = "NORMAL";
//         System.out.println("[INIT] MazeNavigator started. Mode=NORMAL");
//     }

//     @Override
//     public void execute() {
//         double front = drive.frontSector();
//         double left = drive.leftSector();
//         double right = drive.rightSector();
//         double back = drive.backSector();
        

//         double frontLeft = drive.frontLeftSector();
//         double frontRight = drive.frontRightSector();
//         double backLeft = drive.backLeftSector();
//         double backRight = drive.backRightSector();

//         double leftIR = drive.getleftLight();
//         double rightIR = drive.getrightLight();


//         if (Double.isNaN(front)) front = 0;
//         if (Double.isNaN(back)) back = 0;
//         if (Double.isNaN(left)) left = 0;
//         if (Double.isNaN(right)) right = 0;

//         double dangerDist = 300;
//         double slowStartFront = 500;
//         double idealWallDist = 300;
//         double rangeFB = 600.0;
//         double rangeLR = 400.0;
//         double diffFB = front - back;
//         double diffLR = right - left;
//         double fbNorm = MathUtil.clamp(diffFB / rangeFB, -1.0, 1.0);
//         double lrNorm = MathUtil.clamp(diffLR / rangeLR, -1.0, 1.0);
        
//         boolean cornerDetected = false;
//         String cornerDir = "";

//         double cornerThreshold = 400;  // расстояние для активации обхода угла
//         double cornerDiff = 200;       // насколько диагональ должна быть "свободнее"

//         if (!allowDynamicAgain) {
//             allowDynamicAgain = true;
//         }
        
        
//         if (front < cornerThreshold) {
//             if (frontLeft - front > cornerDiff && right < 500) {
//                 // Левый диагональный проход → движение вперёд-вправо
//                 cornerDetected = true;
//                 cornerDir = "FR";
//             } else if (frontRight - front > cornerDiff && left < 500) {
//                 // Правый диагональный проход → движение вперёд-влево
//                 cornerDetected = true;
//                 cornerDir = "FL";
//             }
//         }
//         if (back < cornerThreshold) {
//             if (backLeft - back > cornerDiff && right < 500) {
//                 // Левый диагональный проход → движение вперёд-вправо
//                 cornerDetected = true;
//                 cornerDir = "BR";
//             } else if (backRight - back > cornerDiff && left < 500) {
//                 // Правый диагональный проход → движение вперёд-влево
//                 cornerDetected = true;
//                 cornerDir = "BL";
//             }
//         }
        
        
//         if (cornerDetected && !dynamicMode && !recoveryMode) {
//             double glideSpeed = 0.3;
//             forwardPower = cornerDir.equals("FR") ? glideSpeed : glideSpeed;
//             forwardPower = cornerDir.equals("BR") ? -glideSpeed : -glideSpeed;
//             strafePower = cornerDir.equals("FR") ? glideSpeed : -glideSpeed;
//             strafePower = cornerDir.equals("BR") ? glideSpeed : -glideSpeed;
        
        
//             currentMode = "CORNER_GLIDE";
//             SmartDashboard.putString("CornerDir", cornerDir);
//             SmartDashboard.putBoolean("CornerDetected", true);
        
//             // можно задать кратковременную память направления
//             dynamicMemoryDir = cornerDir;
//         } else {
//             SmartDashboard.putBoolean("CornerDetected", false);
//         }

//         double elapsed = Timer.getFPGATimestamp() - forwardStartTime;
//         if (forwardStarted && elapsed < LR_BLOCK_TIME) {
//             lrNorm = 0.0; // временно отключаем боковое выравнивание
//             strafePower = 0.0;
//             SmartDashboard.putBoolean("LRBlockActive", true);
//         } else {
//             SmartDashboard.putBoolean("LRBlockActive", false);
//         }
        
//         if (Math.abs(fbNorm) < 0.05) fbNorm = 0;
//         if (Math.abs(lrNorm) < 0.05) lrNorm = 0;

//         if (leftIR < 7.5 || right < 7.5)
//         {
//             fg = -1;
//             g = -0.2;
//         } else {
//             fg = 1;
//             g = 0.0;
//         }

//         forwardPower = 0.35 * fg;
//         strafePower = 0.0 * lrNorm;
    

//         // === Боковые флаги ===
//         if (!isStrafingLeft && !isStrafingRight) {
//             if (front < 400) {
//                 if (right > left) isStrafingRight = true;
//                 else isStrafingLeft = true;
//             }
//         }

//         if (isStrafingRight) {
//             if (right > idealWallDist) strafePower = Math.max(strafePower, 0.25);
//             else { strafePower = 0.0; isStrafingRight = false; }
//         }

//         if (isStrafingLeft) {
//             if (left > idealWallDist) strafePower = Math.min(strafePower, -0.25);
//             else { strafePower = 0.0; isStrafingLeft = false; }
//         }
        

//         if (front < slowStartFront) {
//             double scale = MathUtil.clamp((front - dangerDist) / (slowStartFront - dangerDist), 0.1, 1.0);
//             forwardPower *= scale;
//         }

//         if (front < dangerDist && left < dangerDist && right < dangerDist) {
//             forwardPower = -0.3;
//             strafePower = 0.0;
//             isStrafingLeft = false;
//             isStrafingRight = false;
//         }

//         // === Определение текущего направления ===
//         String currentDir;
//         if (Math.abs(forwardPower) > Math.abs(strafePower)) {
//             currentDir = forwardPower > 0 ? "F" : "B";
//         } else {
//             currentDir = strafePower > 0 ? "R" : "L";
//         }

//         // === Проверка повторов ===
//         if (currentDir.equals(stableDir)) stableCount++;
//         else { stableCount = 0; stableDir = currentDir; }

//         if (stableCount > 5 && !currentDir.equals(lastDirection)) {
//             if ((lastDirection.equals("F") && currentDir.equals("B")) ||
//                 (lastDirection.equals("B") && currentDir.equals("F")) ||
//                 (lastDirection.equals("L") && currentDir.equals("R")) ||
//                 (lastDirection.equals("R") && currentDir.equals("L"))) {
//                 repeatCount++;
//             } else repeatCount = 0;

//             lastDirection = currentDir;
//             stableCount = 0;
//         }
//     // --- Автоматический сброс если повторов слишком много ---
//     if (repeatCount >= 4) {
//         System.out.println("[SAFE RESET] repeatCount >= 7 → сброс в NORMAL");
//         repeatCount = 0;
//         stableCount = 0;
//         lastDirection = "";
//         stableDir = "";
//         // dynamicMode = false;
//         // recoveryMode = false;
//         dynamicMemoryDir = "B";
//         currentMode = "NORMAL";
//     }

//         // === Включаем динамический режим ===
//         // === Включаем динамический режим ===
// if (repeatCount >= 1 && !dynamicMode && !recoveryMode) {

//     // --- Проверка: спереди закрыто, качается вбок → отъезжаем назад ---
//     if (front < 400 && back > 470 && backLeft > 480) {
//         dynamicMode = true;
//         dynamicMemoryDir = "B";  // принудительно назад
//         repeatCount = 0;
//         System.out.println("[AUTO-BACK] Робот заблокирован спереди, уходим назад!");
//     } 
//     else {
//         // --- Обычная логика выбора динамического направления ---
//         dynamicMode = true;
//         repeatCount = 0;

//         double maxSide = Math.max(Math.max(front, back), Math.max(left, right));
//         if (maxSide == front) dynamicMemoryDir = "F";
//         else if (maxSide == back) dynamicMemoryDir = "B";
//         else if (maxSide == left) dynamicMemoryDir = "L";
//         else dynamicMemoryDir = "R";

//         System.out.println("[DYNAMIC] Включён динамический режим → " + dynamicMemoryDir);
//     }

//     currentMode = "DYNAMIC";
// }

//         // if (repeatCount >= 3 && !dynamicMode && !recoveryMode) {
//         //     dynamicMode = true;
//         //     repeatCount = 0;

//         //     double maxSide = Math.max(Math.max(front, back), Math.max(left, right));
//         //     if (maxSide == front) dynamicMemoryDir = "F";
//         //     else if (maxSide == back) dynamicMemoryDir = "B";
//         //     else if (maxSide == left) dynamicMemoryDir = "L";
//         //     else dynamicMemoryDir = "R";

//         //     System.out.println("[DYNAMIC] Включён динамический режим → " + dynamicMemoryDir);
//         //     currentMode = "DYNAMIC";
//         // }

//         // === Динамический режим ===
//         // === Динамический режим ===
// if (dynamicMode && !recoveryMode) {

//     boolean dynLeft = false;
//     boolean dynRight = false;
//     boolean dynForward = false;
//     boolean dynBackward = false;

//     switch (dynamicMemoryDir) {
//         case "F":
//             forwardPower = 0.4;
//             strafePower = 0.0;
//             dynForward = true;
//             break;

//         case "B":
//             forwardPower = -0.4;
//             strafePower = 0.0;
//             dynBackward = true;
//             // if (left > 600 && frontLeft > 700 && !odnorazka) {
//             //     recoveryMode = true;
//             //     recoveryStep = 1;
//             //     //dynamicMemoryDir = "L";
//             //     System.out.println("[AUTO-BACK] Робот заблокирован спереди, уходим назад!");
//             // } 
//             break;

//         case "L":
//             strafePower = -0.4;
//             forwardPower = g;
//             dynLeft = true;
//             // if (front < 400 && back > 470 && backLeft > 480) {
//             //     dynamicMode = true;
//             //     dynamicMemoryDir = "B";  // принудительно назад
//             //     repeatCount = 0;
//             //     System.out.println("[AUTO-BACK] Робот заблокирован спереди, уходим назад!");
//             // } 
            
//             break;

//         case "R":
//             strafePower = 0.4;
//             forwardPower = g;
//             dynRight = true;
//             // if (front < 400 && back > 470 && backLeft > 480) {
//             //     dynamicMode = true;
//             //     dynamicMemoryDir = "B";  // принудительно назад
//             //     repeatCount = 0;
//             //     System.out.println("[AUTO-BACK] Робот заблокирован спереди, уходим назад!");
//             // } 
//             break;
//     }

//     // === Отладка флажков динамического движения ===
//     SmartDashboard.putBoolean("Dynamic_Left_Active", dynLeft);
//     SmartDashboard.putBoolean("Dynamic_Right_Active", dynRight);
//     SmartDashboard.putBoolean("Dynamic_Forward_Active", dynForward);
//     SmartDashboard.putBoolean("Dynamic_Backward_Active", dynBackward);

//     if ((dynamicMemoryDir.equals("B") && back < 270) ||
//         (dynamicMemoryDir.equals("F") && front < 400) ||
//         (dynamicMemoryDir.equals("L") && left < 300) ||
//         (dynamicMemoryDir.equals("R") && right < 300)) {

//         dynamicMode = false;
//         recoveryMode = true;
//         currentMode = "RECOVERY";
//         System.out.println("[DYNAMIC→RECOVERY] Вышел из динамики. Переход в восстановление.");
//     }
// }



//         // === Режим восстановления ===
//         if (recoveryMode) {
//             if (recoveryStep == 1 || recoveryStep == 2 ||
//                 dynamicMemoryDir.equals("L") || dynamicMemoryDir.equals("R")) {
//                 forwardPower = 0.0;
//             }

//             if (dynamicMemoryDir.equals("B")) {
//                 if (recoveryStep == 0) {
//                     if (left > right && left > 350) { recoveryStep = 1; System.out.println("[RECOVERY] Двигаемся влево"); }
//                     else if (right > left && right > 350) { recoveryStep = 2; System.out.println("[RECOVERY] Двигаемся вправо"); }
//                     else {
//                         recoveryMode = false;   
//                         dynamicMode = false;
//                         dynamicMemoryDir = "";
//                         currentMode = "NORMAL";
//                         stableCount = 0;
//                         lastDirection = "";
//                         stableDir = "";
//                         recoveryEndTime = Timer.getFPGATimestamp();
//                         System.out.println("[RECOVERY END] Нет боковых — возврат в NORMAL");
//                     }
//                 }

//                 if (recoveryStep == 1) {
//                     strafePower = -0.35;
//                     if (left < 350) {
//                         recoveryStep = 0;
//                         recoveryMode = false;
//                         dynamicMemoryDir = "";
//                         currentMode = "NORMAL";
//                         System.out.println("[RECOVERY END] Закончил движение влево → NORMAL");
//                         System.out.println("LEFT LEFT LEFT LEFT");
//                         odnorazka = true;
//                     }
                
//                 }

//                 if (recoveryStep == 2) {
//                     strafePower = 0.35;
//                     if (right < 350) {
//                         recoveryMode = false;
//                         dynamicMemoryDir = "";
//                         currentMode = "NORMAL";
//                         System.out.println("[RECOVERY END] Закончил движение вправо → NORMAL");
//                         System.out.println("RIGHT RIGHT RIGHT RIGHT");
//                     }
//                 }
//             }
//         }

//         // === PID стабилизация ===
//         double currentYaw = drive.getYaw();
//         double rotation = MathUtil.clamp(pidZAxis.calculate(currentYaw, targetAngle), -0.3, 0.3);
//         drive.holonomicDrive(strafePower, forwardPower, rotation);

//         // === DEBUG SmartDashboard ===
//         SmartDashboard.putString("Mode", currentMode);
//         SmartDashboard.putNumber("Front", front);
//         SmartDashboard.putNumber("Left", left);
//         SmartDashboard.putNumber("Right", right);
//         SmartDashboard.putNumber("Back", back);
//         SmartDashboard.putString("Dir", stableDir);
//         SmartDashboard.putBoolean("DynamicMode", dynamicMode);
//         SmartDashboard.putBoolean("RecoveryMode", recoveryMode);
//         SmartDashboard.putString("MemoryDir", dynamicMemoryDir);
//         SmartDashboard.putNumber("StrafePower", strafePower);
//         SmartDashboard.putNumber("ForwardPower", forwardPower);
//         SmartDashboard.putNumber("RepeatCount", repeatCount);
//         SmartDashboard.putNumber("RecoveryStep", recoveryStep);
//         SmartDashboard.putBoolean("forwardStarted", forwardStarted);
//     }

//     @Override
//     public void end(boolean interrupted) {
//         drive.holonomicDrive(0.0, 0.0, 0.0);
//         System.out.println("[END] MazeNavigator stopped.");
//     }

//     @Override
//     public boolean isFinished() { 
        
//         return camera.isBlackDetected();
//        // return false;
//      }
// }
