// package frc.robot.subsystems.Score.Rollers;

// import edu.wpi.first.networktables.NetworkTable;
// import edu.wpi.first.networktables.NetworkTableInstance;
// import edu.wpi.first.wpilibj.DriverStation;
// import edu.wpi.first.wpilibj2.command.SubsystemBase;

// public class StreamDeckIntakeRollerController extends SubsystemBase {

//     private final IntakeManager intake;
//     private final NetworkTable table;

//     private boolean lastIntakeToggle = false;
//     private boolean lastOuttakeToggle = false;

//     public StreamDeckIntakeRollerController(IntakeManager intake) {
//         this.intake = intake;

//         table = NetworkTableInstance.getDefault()
//                 .getTable("StreamDeck/IntakeRoller");
//     }

//    @Override
// public void periodic() {

//     if (!DriverStation.isEnabled()) return;

//     boolean intakeToggle =
//         table.getEntry("intakeToggle").getBoolean(false);

//     boolean outtakeToggle =
//         table.getEntry("outtakeToggle").getBoolean(false);

//     if (intakeToggle && !lastIntakeToggle) {
//         intake.toggleIntake();
//     }
//     lastIntakeToggle = intakeToggle;

//     if (outtakeToggle && !lastOuttakeToggle) {
//         intake.toggleOuttake();
//     }
//     lastOuttakeToggle = outtakeToggle;
// }
// }