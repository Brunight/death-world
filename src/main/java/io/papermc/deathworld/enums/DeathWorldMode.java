package io.papermc.deathworld.enums;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public enum DeathWorldMode {
    DEFAULT, KILL_ALL;

    public static DeathWorldMode getMode(String str) {
        switch (str) {
            case "killall" -> {
                return KILL_ALL;
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
            default -> {
                return "default";
            }
        }
    }
}
