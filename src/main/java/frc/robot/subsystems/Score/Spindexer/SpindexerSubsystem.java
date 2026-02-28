package frc.robot.subsystems.Score.Spindexer;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class SpindexerSubsystem extends SubsystemBase {

  private SparkMax SpinMotor = new SparkMax(Constants.SpindexerConstants.SPINNER_ID, MotorType.kBrushed);
  private SparkMaxConfig cfg = new SparkMaxConfig();

  public SpindexerSubsystem() {
    cfg
    .idleMode(IdleMode.kBrake)
    .smartCurrentLimit(40);

    SpinMotor.configure(
      cfg, 
      ResetMode.kResetSafeParameters, 
      PersistMode.kPersistParameters);

  }

public void spining() {
   SpinMotor.set(Constants.SpindexerConstants.SPIN_POWER);
    
}
    public void stop() {
      SpinMotor.stopMotor();
    }
  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
