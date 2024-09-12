package io.papermc.deathworld.Managers;

import io.papermc.deathworld.DeathWorldPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DeathCountManager {
    final String FILENAME = "deaths.yml";
    final private DeathWorldPlugin plugin;
    private File deathsFile;
    private FileConfiguration deathsConfig;
    private HashMap<String, Integer> playerDeathCounts = new HashMap<>();

    public DeathCountManager(DeathWorldPlugin plugin) {
        this.plugin = plugin;

        // Initialize deaths.yml for persistent storage
        deathsFile = new File(plugin.getDataFolder(), FILENAME);
        deathsConfig = YamlConfiguration.loadConfiguration(deathsFile);


        if (deathsFile.exists()) {
            for (String key : deathsConfig.getKeys(false)) {
                int deaths = deathsConfig.getInt(key);
                playerDeathCounts.put(key, deaths);
            }
        }
    }

    public Integer addPlayerDeath(Player player) {
        Integer oldValue = playerDeathCounts.getOrDefault(player.getName(), 0);
        Integer newValue = oldValue + 1;
        playerDeathCounts.put(player.getName(), newValue);

        new BukkitRunnable() {
            @Override
            public void run() {
                deathsConfig.set(player.getName(), newValue);
                try {
                    deathsConfig.save(deathsFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save to '" + FILENAME + "': " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this.plugin);

        return newValue;
    }

    public Integer getPlayerDeaths(Player player) {
        return playerDeathCounts.getOrDefault(player.getName(),0);
    }
}
