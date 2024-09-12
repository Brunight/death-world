package io.papermc.deathworld;

import io.papermc.deathworld.Helpers.LogHelper;
import io.papermc.deathworld.Listeners.CommandListener;
import io.papermc.deathworld.Listeners.PlayerDeathListener;
import io.papermc.deathworld.Listeners.PlayerJoinListener;
import io.papermc.deathworld.Managers.DeathCountManager;
import io.papermc.deathworld.Managers.WorldManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class DeathWorldPlugin extends JavaPlugin {
    public FileConfiguration mainConfig;
    public LogHelper deathLogHelper;
    public DeathCountManager deathCountManager;
    public WorldManager worldManager;

    @Override
    public void onEnable() {
        deathLogHelper = new LogHelper(new File(getDataFolder(), "death_log.txt"));
        deathCountManager = new DeathCountManager(this);
        mainConfig = getConfig();
        mainConfig.addDefault("mode", "world");
        mainConfig.setComments("mode", List.of(
                "Death World mode.",
                "'world' will create a new world on player death;",
                "'killall' will kill everyone, but current world is kept.",
                "Defaults to 'world'."
        ));
        mainConfig.addDefault("autoGenerateNewWorld", true);
        mainConfig.setComments("autoGenerateNewWorld", List.of(
                "Should automatically generate a new world on player death.",
                "If set to false, an admin needs to run the command '/dw start' to create a new world.",
                "Only works with 'mode' = 'world'.",
                "Defaults to 'true'."
        ));
        mainConfig.addDefault("currentWorld", "lobby");
        mainConfig.setComments("currentWorld", List.of(
                "Tracks the world name which has a gameplay running.",
                "This field should not be directly changed."
        ));
        mainConfig.options().parseComments(true);
        mainConfig.options().copyDefaults(true);
        saveConfig();

        this.getCommand("dw").setExecutor(new CommandListener(this));
        worldManager = new WorldManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
    }
}