package frc.robot.subsystems.Score.Angular;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Controlador do StreamDeck para o sistema de ângulo do Intake
 * WPILib 2026 - Polling robusto com debounce
 * Muito mais estável que listeners!
 */
public class StreamDeckIntakeAngleController extends SubsystemBase {

    private final IntakeAngleManager intake;
    private final NetworkTable table;

    // Estados anteriores para debounce (evita múltiplos triggers)
    private boolean lastToggle = false;
    private boolean lastZero = false;
    private boolean lastTarget = false;

    private boolean goToTarget = true;
    private int debounceCounter = 0;
    private static final int DEBOUNCE_CYCLES = 2; // Aguarda 2 ciclos antes de processar

    public StreamDeckIntakeAngleController(IntakeAngleManager intake) {
        this.intake = intake;
        table = NetworkTableInstance.getDefault()
                .getTable("StreamDeck/IntakeAngle");

        System.out.println("✅ StreamDeckIntakeAngleController inicializado");
        SmartDashboard.putString("StreamDeck/Intake", "Controlador ativo");
    }

    @Override
    public void periodic() {
        try {
            // Se o robô não está habilitado, não faz nada
            if (!DriverStation.isEnabled()) {
                SmartDashboard.putString("StreamDeck/Status", "⚠️ Robô desabilitado");
                return;
            }

            // Lê os valores atuais da tabela NetworkTables
            boolean currentToggle = table.getEntry("toggleCount").getBoolean(false);
            boolean currentZero = table.getEntry("calibrateZero").getBoolean(false);
            boolean currentTarget = table.getEntry("calibrateTarget").getBoolean(false);

            // ===== TOGGLE BUTTON =====
            // Detecta transição de FALSE para TRUE (borda de subida)
            if (currentToggle && !lastToggle) {
                debounceCounter++;
                
                if (debounceCounter >= DEBOUNCE_CYCLES) {
                    handleToggleButton();
                    debounceCounter = 0;
                }
            } else if (!currentToggle) {
                debounceCounter = 0;
            }
            lastToggle = currentToggle;

            // ===== CALIBRATE ZERO =====
            // Detecta transição de FALSE para TRUE
            if (currentZero && !lastZero) {
                System.out.println("📌 Botão ZERO pressionado");
                handleCalibrateZero();
            }
            lastZero = currentZero;

            // ===== CALIBRATE TARGET =====
            // Detecta transição de FALSE para TRUE
            if (currentTarget && !lastTarget) {
                System.out.println("📌 Botão TARGET pressionado");
                handleCalibrateTarget();
            }
            lastTarget = currentTarget;

            // Atualiza dashboard
            SmartDashboard.putString("StreamDeck/Status", "✅ Ativo");
            SmartDashboard.putBoolean("StreamDeck/Toggle", currentToggle);
            SmartDashboard.putBoolean("StreamDeck/Zero", currentZero);
            SmartDashboard.putBoolean("StreamDeck/Target", currentTarget);
            SmartDashboard.putString("StreamDeck/NextToggle", goToTarget ? "TARGET" : "ZERO");

        } catch (Exception e) {
            System.err.println("❌ Erro em periodic: " + e.getMessage());
            e.printStackTrace();
            SmartDashboard.putString("StreamDeck/Status", "❌ Erro!");
        }
    }

    /**
     * Manipula o botão de toggle (alterna entre Zero e Target)
     */
    private void handleToggleButton() {
        if (!DriverStation.isEnabled()) {
            System.out.println("⚠️ Robô desabilitado, toggle ignorado");
            SmartDashboard.putString("StreamDeck/Status", "⚠️ Desabilitado");
            return;
        }

        try {
            if (goToTarget) {
                System.out.println("📍 TOGGLE: Movendo para TARGET");
                intake.moveToTargetPosition();
                SmartDashboard.putString("StreamDeck/Toggle", "→ TARGET");
                SmartDashboard.putString("Intake/Status", "Movendo para TARGET");
            } else {
                System.out.println("📍 TOGGLE: Movendo para ZERO");
                intake.moveToZeroPosition();
                SmartDashboard.putString("StreamDeck/Toggle", "→ ZERO");
                SmartDashboard.putString("Intake/Status", "Movendo para ZERO");
            }

            goToTarget = !goToTarget;
            SmartDashboard.putString("StreamDeck/NextToggle", goToTarget ? "TARGET" : "ZERO");

        } catch (Exception e) {
            System.err.println("❌ Erro no toggle: " + e.getMessage());
            e.printStackTrace();
            SmartDashboard.putString("StreamDeck/Status", "❌ Erro no toggle");
        }
    }

    /**
     * Manipula a calibração de ZERO
     */
    private void handleCalibrateZero() {
        if (!DriverStation.isEnabled()) {
            System.out.println("⚠️ Robô desabilitado, calibração ignorada");
            return;
        }

        try {
            System.out.println("🔧 CALIBRANDO ZERO...");
            intake.calibrateZero();
            SmartDashboard.putString("StreamDeck/Calibration", "✅ ZERO calibrado");
            SmartDashboard.putString("StreamDeck/Status", "✅ ZERO OK");

        } catch (Exception e) {
            System.err.println("❌ Erro ao calibrar ZERO: " + e.getMessage());
            e.printStackTrace();
            SmartDashboard.putString("StreamDeck/Calibration", "❌ Erro ZERO");
        }
    }

    /**
     * Manipula a calibração de TARGET
     */
    private void handleCalibrateTarget() {
        if (!DriverStation.isEnabled()) {
            System.out.println("⚠️ Robô desabilitado, calibração ignorada");
            return;
        }

        try {
            System.out.println("🔧 CALIBRANDO TARGET...");
            intake.calibrateTargetAngle();
            SmartDashboard.putString("StreamDeck/Calibration", "✅ TARGET calibrado");
            SmartDashboard.putString("StreamDeck/Status", "✅ TARGET OK");

        } catch (Exception e) {
            System.err.println("❌ Erro ao calibrar TARGET: " + e.getMessage());
            e.printStackTrace();
            SmartDashboard.putString("StreamDeck/Calibration", "❌ Erro TARGET");
        }
    }

}