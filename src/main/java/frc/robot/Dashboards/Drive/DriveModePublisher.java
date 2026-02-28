package frc.robot.Dashboards.Drive;

import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DriveModePublisher {

  private final IntegerPublisher aimModePub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AimMode")
          .publish();

  private final IntegerPublisher alignModePub =
      NetworkTableInstance.getDefault()
          .getIntegerTopic("/Modes/AlignMode")
          .publish();

  public void publishAim(int mode) {
    aimModePub.set(mode);
  }

  public void publishAlign(int mode) {
    alignModePub.set(mode);
  }
}
