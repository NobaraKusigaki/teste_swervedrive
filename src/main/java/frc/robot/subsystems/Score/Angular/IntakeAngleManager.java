package frc.robot.subsystems.Score.Angular;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleManager extends SubsystemBase {

    public enum ControlState {
        DISABLED,
        MANUAL,
        AUTOMATIC
    }

    private final IntakeAngleSubsystem intake;
    private final PIDController pid;
    private final ArmFeedforward ff;

    private ControlState currentState = ControlState.DISABLED;
    private double targetAngleDeg = 0.0;
    private boolean autoActive = false;
    private double lastOutput = 0.0;
    private int loopsAtSetpoint = 0;
    private static final int SETPOINT_LOOPS = 5;

    public IntakeAngleManager() {
        intake = new IntakeAngleSubsystem();

        pid = new PIDController(
            Constants.IntakeConstants.ANGLE_KP,
            Constants.IntakeConstants.ANGLE_KI,
            Constants.IntakeConstants.ANGLE_KD
        );
        pid.enableContinuousInput(0.0, 360.0);
        pid.setTolerance(Constants.IntakeConstants.ANGLE_TOLERANCE_DEG);

        ff = new ArmFeedforward(
            Constants.IntakeConstants.ANGLE_KS,
            Constants.IntakeConstants.ANGLE_KG,
            Constants.IntakeConstants.ANGLE_KV,
            Constants.IntakeConstants.ANGLE_KA
        );

        intake.loadZero();
        targetAngleDeg = Preferences.getDouble("IntakeAngleTarget", 0.0);
    }

    /* ================= MANUAL ================= */

    public void setManual() {
        if (currentState != ControlState.MANUAL) {
            currentState = ControlState.MANUAL;
            SmartDashboard.putString("Intake/Mode", "MANUAL");
        }
    }

    public void setManualOutput(double power) {
        if (currentState == ControlState.MANUAL) {
            intake.setPower(power);
            SmartDashboard.putNumber("Intake/ManualPower", power);
        }
    }

    /* ================= AUTOMATIC ================= */

    public void moveToZeroPosition() {
        if (currentState != ControlState.AUTOMATIC || !autoActive) {
            currentState = ControlState.AUTOMATIC;
            autoActive = true;
            targetAngleDeg = 0.0;
            loopsAtSetpoint = 0;
            pid.reset();
            SmartDashboard.putString("Intake/Status", "Movendo para ZERO");
            SmartDashboard.putString("Intake/Mode", "AUTOMATIC");
        }
    }

    public void moveToTargetPosition() {
        if (currentState != ControlState.AUTOMATIC || !autoActive) {
            currentState = ControlState.AUTOMATIC;
            autoActive = true;
            targetAngleDeg = Preferences.getDouble("IntakeAngleTarget", targetAngleDeg);
            loopsAtSetpoint = 0;
            pid.reset();
            SmartDashboard.putString("Intake/Status", "Movendo para TARGET");
            SmartDashboard.putString("Intake/Mode", "AUTOMATIC");
        }
    }

    /* ================= PERIODIC ================= */

    @Override
    public void periodic() {
        switch (currentState) {
            case AUTOMATIC:
                moveAutomatic();
                break;
            case MANUAL:
            case DISABLED:
            default:
                lastOutput = 0.0;
                loopsAtSetpoint = 0;
                break;
        }

        if (!autoActive && currentState == ControlState.AUTOMATIC) {
            currentState = ControlState.DISABLED;
        }
    }

    private void moveAutomatic() {
        if (!autoActive) {
            intake.stop();
            return;
        }

        double currentAngle = intake.getAngleDeg();
        double setpoint = targetAngleDeg;

        double pidOutput = pid.calculate(currentAngle, setpoint);
        double error = pid.getPositionError();
        double ffOutput = ff.calculate(Math.toRadians(currentAngle), 0.0);

        double totalOutput = pidOutput + ffOutput;

        totalOutput = Math.max(
            -Constants.IntakeConstants.ANGLE_MAX_OUTPUT,
            Math.min(totalOutput, Constants.IntakeConstants.ANGLE_MAX_OUTPUT)
        );

        if (Math.abs(error) < 5.0) {
            totalOutput *= 0.3;
        } else if (Math.abs(error) < 10.0) {
            totalOutput *= 0.6;
        }

        double rampRate = 0.02;
        totalOutput = lastOutput + Math.max(
            -rampRate,
            Math.min(totalOutput - lastOutput, rampRate)
        );

        lastOutput = totalOutput;
        intake.setPower(totalOutput);

        SmartDashboard.putNumber("Intake/CurrentAngle", currentAngle);
        SmartDashboard.putNumber("Intake/TargetAngle", setpoint);
        SmartDashboard.putNumber("Intake/ErrorDeg", error);
        SmartDashboard.putNumber("Intake/PIDOutput", pidOutput);
        SmartDashboard.putNumber("Intake/FFOutput", ffOutput);
        SmartDashboard.putNumber("Intake/TotalOutput", totalOutput);

        if (pid.atSetpoint()) {
            loopsAtSetpoint++;
            if (loopsAtSetpoint >= SETPOINT_LOOPS) {
                intake.stop();
                autoActive = false;
                currentState = ControlState.DISABLED;
                loopsAtSetpoint = 0;
                SmartDashboard.putString("Intake/Status", "Posição atingida");
            }
        } else {
            loopsAtSetpoint = 0;
        }
    }

    /* ================= CONTROLS ================= */

    public void stop() {
        intake.stop();
        autoActive = false;
        currentState = ControlState.DISABLED;
        loopsAtSetpoint = 0;
        lastOutput = 0.0;
        SmartDashboard.putString("Intake/Status", "Parado");
        SmartDashboard.putString("Intake/Mode", "DISABLED");
    }

    public void calibrateZero() {
        intake.recalibrateZero();
        SmartDashboard.putString("Intake/Calibration", "ZERO salvo!");
    }

    public void calibrateTargetAngle() {
        double currentAngle = intake.getAngleDeg();
        targetAngleDeg = currentAngle;
        Preferences.setDouble("IntakeAngleTarget", targetAngleDeg);
        SmartDashboard.putString("Intake/Calibration", "TARGET salvo!");
        SmartDashboard.putNumber("Intake/TargetAngleSaved", targetAngleDeg);
    }

    /* ================= GETTERS ================= */

    public ControlState getCurrentState() {
        return currentState;
    }

    public double getCurrentAngle() {
        return intake.getAngleDeg();
    }

    public double getTargetAngle() {
        return targetAngleDeg;
    }

    public boolean isAutoActive() {
        return autoActive;
    }
}