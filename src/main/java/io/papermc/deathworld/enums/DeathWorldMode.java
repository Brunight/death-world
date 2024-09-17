package io.papermc.deathworld.enums;

public enum DeathWorldMode {
    DEFAULT, KILL_ALL;

    public static String modeToString(DeathWorldMode mode) {
        switch (mode) {
            case DEFAULT -> {
                return "default";
            }
            case KILL_ALL -> {
                return "killall";
            }
            default -> {
                return null;
            }
        }
    }

    public static DeathWorldMode getMode(String str) {
        switch (str) {
            case "default" -> {
                return DEFAULT;
            }
            case "killall" -> {
                return KILL_ALL;
            }
            default -> {
                return null;
            }
        }
    }
}
