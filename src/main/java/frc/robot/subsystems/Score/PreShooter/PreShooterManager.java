package frc.robot.subsystems.Score.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.subsystems.Score.Shooter.ShooterManager;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class PreShooterManager extends SubsystemBase {

    public enum PreShooterState {
        IDLE,
        ARMED,
        AUTO_FEEDING,
        DISABLED
    }

    public enum ControlMode {
        MANUAL,
        AUTO_DISTANCE
    }

    private PreShooterState state = PreShooterState.IDLE;
    private ControlMode mode = ControlMode.MANUAL;
    private boolean autoMode = false; // true durante auto — ignora requisito de alinhamento

    private final PreShooterSubsystem preShooter;
    private final ViewSubsystem vision;
    private final ShooterManager shooterManager;

    public PreShooterManager(PreShooterSubsystem preShooter, ViewSubsystem vision, ShooterManager shooterManager) {
        this.preShooter    = preShooter;
        this.vision        = vision;
        this.shooterManager = shooterManager;
    }

    public void toggleMode() {
        mode  = (mode == ControlMode.MANUAL) ? ControlMode.AUTO_DISTANCE : ControlMode.MANUAL;
        state = PreShooterState.IDLE;
        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }

    public ControlMode getMode() { return mode; }

    public void toggleManualFeed() {
        if (mode != ControlMode.MANUAL) return;
        state = (state == PreShooterState.ARMED) ? PreShooterState.IDLE : PreShooterState.ARMED;
    }

    // chamado pelo NamedCommand no auto
    public void enableAuto() {
        mode     = ControlMode.AUTO_DISTANCE;
        autoMode = true;
        state    = PreShooterState.ARMED; // alimenta assim que shooter estiver no speed
    }

    public void stop() {
        state    = PreShooterState.IDLE;
        autoMode = false;
        preShooter.stop();
    }

    public PreShooterState getState() { return state; }

    @Override
    public void periodic() {

        if (state == PreShooterState.DISABLED) {
            preShooter.stop();
            return;
        }

        if (mode == ControlMode.AUTO_DISTANCE) {

            boolean shooterReady = shooterManager.isAtSpeed();

            if (autoMode) {
                // no auto só espera o shooter estar no speed, sem exigir alinhamento
                if (shooterReady) {
                    state = PreShooterState.AUTO_FEEDING;
                } else {
                    state = PreShooterState.ARMED; // aguarda, não volta para IDLE
                }
            } else {
                // teleop — exige alinhamento completo
                boolean tagValid      = vision.hasValidFrontTarget();
                boolean aligned       = Math.abs(vision.getFrontTxRad()) < Math.toRadians(1.2);
                boolean validDistance = vision.getFrontDistanceToTag() != Double.MAX_VALUE;

                if (tagValid && aligned && shooterReady && validDistance) {
                    state = PreShooterState.AUTO_FEEDING;
                } else {
                    state = PreShooterState.IDLE;
                }
            }
        }

        switch (state) {
            case ARMED:
            case AUTO_FEEDING:
                preShooter.feed();
                break;
            case DISABLED:
            case IDLE:
            default:
                preShooter.stop();
                break;
        }

        SmartDashboard.putString("PreShooter/State", state.name());
        SmartDashboard.putString("PreShooter/Mode",  mode.name());
    }
}