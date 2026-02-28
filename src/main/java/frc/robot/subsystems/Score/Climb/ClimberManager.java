// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.Score.Climb;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ClimberManager extends SubsystemBase {
public enum ClimbState {
  IDLE,
  MANUAL,
  AUTOMATIC
}

private final ClimberSubsystem climber;
private final PIDController pidController;

private ClimbState state = ClimbState.IDLE;
private boolean autoActive =  false;

private double minPos = 0;
private double maxPos = 0;
private double targetPos = 0;

  public ClimberManager() {
    climber = new ClimberSubsystem();
    pidController = new PIDController(
      Constants.ClimbConstants.CLIMBER_kP, 
      Constants.ClimbConstants.CLIMBER_kI, 
      Constants.ClimbConstants.CLIMBER_kD);

      pidController.setTolerance(Constants.ClimbConstants.CLIMBER_TOLERANCE);

      minPos = Preferences.getDouble(Constants.ClimbConstants.PREF_MIN_KEY, 0);
      maxPos = Preferences.getDouble(Constants.ClimbConstants.PREF_MAX_KEY, 0);
  }
public void setManual(){
  state = ClimbState.MANUAL;
  autoActive = false;
}

  public void setClimbManual(double power){
    if(state != ClimbState.MANUAL){
      setManual();
    }
     power = Math.max(
            Math.min(power, Constants.ClimbConstants.CLIMBER_MAX_OUTPUT),
            -Constants.ClimbConstants.CLIMBER_MAX_OUTPUT);

    climber.setClimbPower(power);
  }

public void setStopManualClimb(){
  if(state == ClimbState.MANUAL){
    climber.stopClimb();
    state = ClimbState.IDLE;
    autoActive = false;
  } else {
    climber.stopClimb();
  }
}

public void calibrateMin() {
    minPos = climber.getPosition();
    Preferences.setDouble(Constants.ClimbConstants.PREF_MIN_KEY, minPos);
}

public void calibrateMax() {
    maxPos = climber.getPosition();
    Preferences.setDouble(Constants.ClimbConstants.PREF_MAX_KEY, maxPos);
}

public void goToMin() {
    state = ClimbState.AUTOMATIC;
    autoActive = true;
    targetPos = minPos;
}

public void goToMax() {
    state = ClimbState.AUTOMATIC;
    autoActive = true;
    targetPos = maxPos;
}

private void runAutomatic() {
    if (!autoActive) return;

    double current = climber.getPosition();
    double output = pidController.calculate(current, targetPos);

    output = Math.max(
        Math.min(output, Constants.ClimbConstants.CLIMBER_MAX_OUTPUT),
        -Constants.ClimbConstants.CLIMBER_MAX_OUTPUT
    );

    climber.setClimbPower(output);

    if (pidController.atSetpoint()) {
        climber.stopClimb();
        autoActive = false;
        state = ClimbState.IDLE;
    }
}

 @Override
public void periodic() {

    switch (state) {
        case AUTOMATIC:
            runAutomatic();
            break;

        case MANUAL:
        case IDLE:
        default:
            break;
    }
}
}
