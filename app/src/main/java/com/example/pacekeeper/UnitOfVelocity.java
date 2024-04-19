package com.example.pacekeeper;

public enum UnitOfVelocity {
    KM_PER_HOUR,
    MIN_PER_KM;

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
