package io.papermc.deathworld;

import io.papermc.deathworld.enums.DeathWorldMode;
import io.papermc.deathworld.helpers.LogHelper;
import io.papermc.deathworld.listeners.CommandListener;
import io.papermc.deathworld.listeners.PlayerDeathListener;
import io.papermc.deathworld.listeners.PlayerJoinListener;
import io.papermc.deathworld.managers.DeathCountManager;
import io.papermc.deathworld.managers.WorldManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class DeathWorldPlugin extends JavaPlugin {
    public FileConfiguration mainConfig;
    public LogHelper deathLogHelper;
    public DeathCountManager deathCountManager;
    public WorldManager worldManager;
    private DeathWorldMode mode = DeathWorldMode.DEFAULT;

    @Override
    public void onEnable() {
        deathLogHelper = new LogHelper(new File(getDataFolder(), "death_log.txt"));
        deathCountManager = new DeathCountManager(this);
        mainConfig = getConfig();
        mainConfig.addDefault("mode", DeathWorldMode.DEFAULT.toString());
        mainConfig.setComments("mode", List.of(
                "Death World mode.",
                "'world' will create a new world on player death;",
                "'killall' will kill everyone, but current world is kept.",
                "Defaults to 'default'."));
        mainConfig.addDefault("autoGenerateNewWorld", true);
        mainConfig.setComments("autoGenerateNewWorld", List.of(
                "Should automatically generate a new world on player death.",
                "If set to false, an admin needs to run the command '/dw start' to create a new world.",
                "Only works with 'mode' = 'default'.",
                "Defaults to 'true'."));
        mainConfig.addDefault("currentWorld", "lobby");
        mainConfig.setComments("currentWorld", List.of(
                "Tracks the world name which has a gameplay running.",
                "This field should not be directly edited."));
        mainConfig.options().parseComments(true);
        mainConfig.options().copyDefaults(true);
        saveConfig();

        String modeInConfig = Objects.requireNonNullElse(mainConfig.getString("mode"), "default");
        this.mode = DeathWorldMode.getMode(modeInConfig);

        this.getCommand("dw").setExecutor(new CommandListener(this));
        worldManager = new WorldManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
    }

    public DeathWorldMode getMode() {
        return mode;
    }

    public void setMode(DeathWorldMode mode) {
        if (this.mode.equals(mode)) {
            return;
        }
        this.mode = mode;
        this.mainConfig.set("mode", mode.toString());
        this.saveConfig();
    }
}