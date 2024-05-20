package com.example.pacekeeper;

/**
 * Enumeration for keeping track of which unit of velocity
 * that is currently displayed in the application.
 *
 * @author Samuel
 */
public enum UnitOfVelocity {
    KM_PER_HOUR,
    MIN_PER_KM;

    /**
     * Returns a String representing the enum.
     *
     * @return a more presentable String.
     * @author Samuel
     */
    @Override
    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return " km/h";
            case 1:
                return " min/km";
            default:
                return null;
        }
    }
}
