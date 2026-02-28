package frc.robot.TagsID;

import java.util.Set;

import edu.wpi.first.wpilibj.DriverStation;

public class TowerTagSelector{
    
    public enum TowerSide {
        CENTER,
        RIGHT

    }
 public static Set<Integer> getTags(
        TowerSide side,
        DriverStation.Alliance alliance
    ){
        if (alliance == DriverStation.Alliance.Blue) {
            switch (side) {
                case CENTER:
                    return Set.of(31);
                case RIGHT:
                    return Set.of(32);
            }
        } else {
            switch (side) {
                case CENTER:
                    return Set.of(15);
                case RIGHT:
                    return Set.of(16);
            }
        }

        return Set.of();
    } 

}
