package at.tewan.androidmapper.beatmap.enums;

/**
 * @author Stefan Heinz
 *
 * Enumeration of all default cut directions).
 * Enumeration values include the direction in degrees (Clockwise from the right)
 */
public enum CutDirection {

    UP(270),
    DOWN(90),
    LEFT(180),
    RIGHT(0),
    UP_LEFT(225),
    UP_RIGHT(315),
    DOWN_LEFT(135),
    DOWN_RIGHT(45),
    ANY(360);


    private float degrees;

    CutDirection(float degrees) {
        this.degrees = degrees;
    }

    public float getDegrees() {
        return degrees;
    }

    public float getRadians() {
        return (float) Math.toRadians(getDegrees());
    }

    /**
     *
     * @param degrees
     * @return The according cut direction (snapped to 45 degrees) ANY is returned as fallback
     */
    public static CutDirection getDirection(float degrees) {

        for (CutDirection dir : values()) {
            if (dir.getDegrees() == degrees)
                return dir;
        }

        return ANY;
    }

    public static CutDirection getType(int type) {
        for (CutDirection dir : values()) {
            if (dir.ordinal() == type)
                return dir;
        }

        return ANY;
    }
}
