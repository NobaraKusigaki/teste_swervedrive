package frc.robot.commands.vision;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class AlignWithPieceCommand extends Command {

  private final SwerveSubsystem swerve;
  private final ViewSubsystem vision;

  public AlignWithPieceCommand(
      SwerveSubsystem swerve,
      ViewSubsystem vision) {

    this.swerve = swerve;
    this.vision = vision;
    addRequirements(swerve);
  }

  private boolean active = false;

  public boolean isActive() {
    return active;
  }

  @Override
  public void initialize() {
    swerve.getHeadingPID().reset(0);
    active = true;
  }

  @Override
  public void execute() {

    // Use the AI pipeline target, not the AprilTag pipeline.
    // hasBackTarget() requires an allowed AprilTag — which is never present
    // during piece alignment — so checking hasAiTarget() is correct here.
    if (!vision.hasAiTarget()) {
      swerve.stop();
      return;
    }

    double rot =
        swerve.getHeadingPID().calculate(
            vision.getAiTxRad(),   // was: getBackTxRad() — wrong pipeline
            0.0
        );

    // getAiDistance() returns the Python-estimated distance to the game piece.
    // Previously getBackDistanceToTag() was used here, which requires a valid
    // AprilTag target and returns Double.MAX_VALUE when there is none, making
    // the forward speed calculation blow up to MAX_SPEED.
    double aiDist = vision.getAiDistance();
    double forward = Double.isFinite(aiDist)
        ? Constants.K_AUTO_PIECE_FORWARD * (Constants.TA_TARGET - aiDist)
        : 0.0;

    forward = MathUtil.clamp(
        forward,
        -Constants.MAX_SPEED,
        Constants.MAX_SPEED
    );

    swerve.drive(
        new Translation2d(forward, 0),
        rot,
        false
    );
  }

  @Override
  public void end(boolean interrupted) {
    active = false;
    swerve.stop();
  }
}