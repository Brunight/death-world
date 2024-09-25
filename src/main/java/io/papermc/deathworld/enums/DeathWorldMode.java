package io.papermc.deathworld.enums;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public enum DeathWorldMode {
    DEFAULT, KILL_ALL, NONE;

    public static DeathWorldMode getMode(String str) {
        switch (str) {
            case "killall" -> {
                return KILL_ALL;
            }
            case "none" -> {
                return NONE;
            }
            default -> {
                return DEFAULT;
            }
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case KILL_ALL -> {
                return "killall";
            }
            case NONE -> {
                return "none";
            }
            default -> {
                return "default";
            }
        }
    }
}
