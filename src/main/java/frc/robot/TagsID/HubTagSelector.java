package frc.robot.TagsID;

import java.util.Set;

import edu.wpi.first.wpilibj.DriverStation;

public class HubTagSelector {

    public enum HubSide {
        LEFT,
        CENTER,
        RIGHT
    }

    public static Set<Integer> getTags(
        HubSide side,
        DriverStation.Alliance alliance
    ) {
        if (alliance == DriverStation.Alliance.Blue) {
            switch (side) {
                case CENTER:
                    return Set.of(25, 26);
                case LEFT:
                    return Set.of(18, 27);
                case RIGHT:
                    return Set.of(21, 24);
            }
        } else {
            switch (side) {
                case CENTER:
                    return Set.of(9, 10);
                case LEFT:
                    return Set.of(11, 2);
                case RIGHT:
                    return Set.of(8, 5);
            }
        }

        return Set.of();
    }
}
