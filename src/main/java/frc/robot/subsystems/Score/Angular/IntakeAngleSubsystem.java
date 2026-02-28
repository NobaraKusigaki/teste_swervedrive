package frc.robot.subsystems.Score.Angular;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleSubsystem extends SubsystemBase {

    private final SparkMax motor =
        new SparkMax(Constants.IntakeConstants.ANGLE_MOTOR_ID, MotorType.kBrushless);

    private final DutyCycleEncoder absEncoder =
        new DutyCycleEncoder(Constants.IntakeConstants.ANGLE_ENCODER_ID);

    private double zeroOffsetDeg = 0.0;

    public IntakeAngleSubsystem() {

        SparkMaxConfig cfg = new SparkMaxConfig();
        cfg.idleMode(IdleMode.kBrake)
           .smartCurrentLimit(40);
        motor.configure(
            cfg,
            ResetMode.kResetSafeParameters, 
            PersistMode.kPersistParameters);
       
    }

    public double getAngleDeg() {
        double raw = (1.0 - absEncoder.get()) * 360.0;
        double angle = raw + zeroOffsetDeg;
        return ((angle % 360.0) + 360.0) % 360.0;
    }

    public void loadZero() {
        zeroOffsetDeg =
            Preferences.getDouble("IntakeAngleZero", 0.0);
    }

    public void recalibrateZero() {
        zeroOffsetDeg = -((1.0 - absEncoder.get()) * 360.0);
        Preferences.setDouble("IntakeAngleZero", zeroOffsetDeg);
    }

    public void setPower(double power) {
        motor.set(power);
    }

    public void stop() {
        motor.stopMotor();
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("Intake/AngleDeg", getAngleDeg());
        SmartDashboard.putNumber("Intake/ZeroOffset", zeroOffsetDeg);
    }
}