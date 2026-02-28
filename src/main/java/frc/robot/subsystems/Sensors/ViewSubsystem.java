package frc.robot.subsystems.Sensors;

import java.util.Set;
import java.util.Optional;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.TagsID.HubTagSelector;
import frc.robot.TagsID.TowerTagSelector;

public class ViewSubsystem extends SubsystemBase {

  // ================= TAG FILTER =================

  private Set<Integer> allowedFrontTags = Set.of(); // Torre
  private Set<Integer> allowedBackTags  = Set.of(); // Hub + Outpost

  // ================= LIMELIGHTS =================

  private final NetworkTable limeFront =
      NetworkTableInstance.getDefault().getTable("limelight-front");

  private final NetworkTable limeBack =
      NetworkTableInstance.getDefault().getTable("limelight-back");

  // ===================== TAG SELECTION =====================

  public void selectHub(HubTagSelector.HubSide side) {
    Optional<DriverStation.Alliance> alliance =
        DriverStation.getAlliance();

    if (alliance.isPresent()) {
      allowedBackTags =
          HubTagSelector.getTags(side, alliance.get());
    }
  }

  public void selectOutpost(HubTagSelector.HubSide side) {
    Optional<DriverStation.Alliance> alliance =
        DriverStation.getAlliance();

    if (alliance.isPresent()) {
      allowedBackTags =
          HubTagSelector.getTags(side, alliance.get());
    }
  }

  public void selectTower(TowerTagSelector.TowerSide side) {
    Optional<DriverStation.Alliance> alliance =
        DriverStation.getAlliance();

    if (alliance.isPresent()) {
      allowedFrontTags =
          TowerTagSelector.getTags(side, alliance.get());
    }
  }

  public boolean hasFrontTarget() {
    return limeFront.getEntry("tv").getDouble(0) == 1;
  }

  public int getFrontTagId() {
    if (!hasFrontTarget()) return -1;
    return (int) limeFront.getEntry("tid").getDouble(-1);
  }

  public boolean isFrontTagAllowed() {
    return allowedFrontTags.contains(getFrontTagId());
  }

  public boolean hasValidFrontTarget() {
    return hasFrontTarget() && isFrontTagAllowed();
  }

  public double getFrontTxRad() {
    return Units.degreesToRadians(
        limeFront.getEntry("tx").getDouble(0.0));
  }

  public double getFrontDistanceToTag() {

    if (!hasValidFrontTarget())
      return Double.MAX_VALUE;

    double tyDegrees =
        limeFront.getEntry("ty").getDouble(0.0);

    double tyRadians =
        Units.degreesToRadians(tyDegrees);

    double angle =
        Constants.LimelightConstants.LIMELIGHT_ANGLE + tyRadians;

    if (Math.abs(Math.tan(angle)) < 1e-3)
      return Double.MAX_VALUE;

    return
        (Constants.LimelightConstants.TAG_HEIGHT
        - Constants.LimelightConstants.LIMELIGHT_HEIGHT)
        / Math.tan(angle);
  }

  // ===================== BACK (HUB + OUTPOST) =====================

  public boolean hasBackTarget() {
    return limeBack.getEntry("tv").getDouble(0) == 1;
  }

  public int getBackTagId() {
    if (!hasBackTarget()) return -1;
    return (int) limeBack.getEntry("tid").getDouble(-1);
  }

  public boolean isBackTagAllowed() {
    return allowedBackTags.contains(getBackTagId());
  }

  public boolean hasValidBackTarget() {
    return hasBackTarget() && isBackTagAllowed();
  }

  public double getBackTxRad() {
    return Units.degreesToRadians(
        limeBack.getEntry("tx").getDouble(0.0));
  }

  public double getBackDistanceToTag() {

    // if (!hasValidBackTarget())
    //   return Double.MAX_VALUE;

    double tyDegrees =
        limeBack.getEntry("ty").getDouble(0.0);

    double tyRadians =
        Units.degreesToRadians(tyDegrees);

    double angle =
        Constants.LimelightConstants.LIMELIGHT_ANGLE + tyRadians;

    if (Math.abs(Math.tan(angle)) < 1e-3)
      return Double.MAX_VALUE;

    return
        (Constants.LimelightConstants.TAG_HEIGHT
        - Constants.LimelightConstants.LIMELIGHT_HEIGHT)
        / Math.tan(angle);
  }
}
