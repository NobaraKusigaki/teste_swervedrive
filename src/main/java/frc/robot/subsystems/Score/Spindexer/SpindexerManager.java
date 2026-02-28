package frc.robot.subsystems.Score.Spindexer;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SpindexerManager extends SubsystemBase {

    public enum SpindexerState {
        IDLE,
        SPINNING,
        DISABLED
    }

    private final SpindexerSubsystem spindexer;
    private SpindexerState state = SpindexerState.IDLE;

    public SpindexerManager(SpindexerSubsystem spindexer) {
        this.spindexer = spindexer;
        SmartDashboard.putString("Spindexer/State", state.name());
    }

    public void toggleSpin() {
        if (state == SpindexerState.SPINNING) {
            setState(SpindexerState.IDLE);
        } else {
            setState(SpindexerState.SPINNING);
        }
    }

    public void stop() {
        setState(SpindexerState.IDLE);
    }

    public void disable(String reason) {
        setState(SpindexerState.DISABLED);
        SmartDashboard.putString("Spindexer/DisabledReason", reason);
    }

    private void setState(SpindexerState newState) {

        if (state == SpindexerState.DISABLED && newState != SpindexerState.DISABLED)
            return;

        state = newState;
        SmartDashboard.putString("Spindexer/State", state.name());
    }

    @Override
    public void periodic() {

        switch (state) {

            case SPINNING:
                spindexer.spining();
                break;

            case DISABLED:
            case IDLE:
            default:
                spindexer.stop();
                break;
        }
    }

    public SpindexerState getState() {
        return state;
    }
}