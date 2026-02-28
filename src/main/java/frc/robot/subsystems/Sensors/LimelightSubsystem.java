package frc.robot.subsystems.Sensors;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class LimelightSubsystem extends SubsystemBase {

  private final NetworkTable table;
  private final NetworkTableEntry tx;
  private final NetworkTableEntry ty;
  private final NetworkTableEntry ta;
  private final NetworkTableEntry tv;

  private final String name;

  public LimelightSubsystem(String tableName, String name) {
    this.name = name;

    table = NetworkTableInstance.getDefault().getTable(tableName);
    tx = table.getEntry("tx");
    ty = table.getEntry("ty");
    ta = table.getEntry("ta");
    tv = table.getEntry("tv");
  }

  @Override
  public void periodic() {
    updateDashboard();
  }

  public boolean hasTarget() {
    return tv.getDouble(0) == 1.0;
  }

  public double getTx() {
    return tx.getDouble(0.0);
  }

  public double getTy() {
    return ty.getDouble(0.0);
  }

  public double getArea() {
    return ta.getDouble(0.0);
  }


  private void updateDashboard() {
    SmartDashboard.putBoolean(name + " Has Target", hasTarget());
    SmartDashboard.putNumber(name + " tx", getTx());
    SmartDashboard.putNumber(name + " ty", getTy());
    SmartDashboard.putNumber(name + " area", getArea());
  }
}
