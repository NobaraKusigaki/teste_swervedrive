// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

// package frc.robot.subsystems.Score.Climb;

// import com.revrobotics.PersistMode;
// import com.revrobotics.RelativeEncoder;
// import com.revrobotics.ResetMode;
// import com.revrobotics.spark.SparkMax;
// import com.revrobotics.spark.SparkLowLevel.MotorType;
// import com.revrobotics.spark.config.SparkMaxConfig;
// import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

// import edu.wpi.first.wpilibj2.command.SubsystemBase;

// public class ClimberSubsystem extends SubsystemBase {
// private final SparkMax climb_motor1 = new SparkMax(14, MotorType.kBrushless); //left 
// private final SparkMax climb_motor2 = new SparkMax(15, MotorType.kBrushless); // right 

// private final SparkMaxConfig cfg1;
// private final SparkMaxConfig cfg2; 

// RelativeEncoder climb_left;
// RelativeEncoder climb_right; 


//   public ClimberSubsystem() {

//     cfg1 = new SparkMaxConfig();
//     cfg1.idleMode(IdleMode.kBrake)
//     .smartCurrentLimit(40);

//     climb_motor1.configure(cfg1, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

//     cfg2 = new SparkMaxConfig();
//     cfg2.idleMode(IdleMode.kBrake)
//     .smartCurrentLimit(40)
//     .inverted(true);

//   @Override
//   public void periodic() {
//     // This method will be called once per scheduler run
//   }
// }
// }