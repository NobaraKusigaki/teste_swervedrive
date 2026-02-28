package frc.robot.Dashboards.RobotStress;

public class RobotStressData {
    public final double batteryVoltage;
    public final double totalCurrent;
    public final double drivetrainCurrent;
    public final double stressScore;
    public final String stressLevel;

    public RobotStressData(
            double batteryVoltage,
            double totalCurrent,
            double drivetrainCurrent,
            double stressScore,
            String stressLevel
    ) {
        this.batteryVoltage = batteryVoltage;
        this.totalCurrent = totalCurrent;
        this.drivetrainCurrent = drivetrainCurrent;
        this.stressScore = stressScore;
        this.stressLevel = stressLevel;
    }
}
