package io.papermc.deathworld.managers;

import io.papermc.deathworld.DeathWorldPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayersToKillOnLoginManager {
    final String FILENAME = "playersToKillOnLogin.yml";
    final private DeathWorldPlugin plugin;
    private final File playersToKillOnLoginFile;
    private final FileConfiguration playersToKillOnLoginConfig;
    private final HashSet<String> playersToKillOnLogin = new HashSet<>();

    public PlayersToKillOnLoginManager(DeathWorldPlugin plugin) {
        this.plugin = plugin;

        playersToKillOnLoginFile = new File(plugin.getDataFolder(), FILENAME);
        playersToKillOnLoginConfig = YamlConfiguration.loadConfiguration(playersToKillOnLoginFile);
        playersToKillOnLoginConfig.setComments("players", List.of(
                "Tracks players that will be killed in login.",
                "Offline players will be killed when they log in if someone died while they were offline.",
                "Killall mode exclusive.",
                "This field should not be directly edited."
        ));
        playersToKillOnLoginConfig.options().parseComments(true);


        if (playersToKillOnLoginFile.exists()) {
            playersToKillOnLogin.addAll(playersToKillOnLoginConfig.getStringList("players"));
        } else {
            saveToFile();
        }
    }

    public void addOfflinePlayersToKillOnLoginList() {
        OfflinePlayer[] offlinePlayers = Bukkit.getServer().getOfflinePlayers();

        List<String> loggedOutPlayersNames = new ArrayList<>();

        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (!offlinePlayer.isOnline()) {
                loggedOutPlayersNames.add(offlinePlayer.getName());
            }
        }

        playersToKillOnLogin.addAll(loggedOutPlayersNames);

        saveToFile();
    }

    public boolean isPlayerInKillOnLoginList(Player p) {
        return playersToKillOnLogin.contains(p.getName());
    }

    /**
     * @return Returns 'true' if player was in list, 'false' otherwise.
     */
    public boolean removePlayerFromKillOnLoginList(Player p) {
        if (playersToKillOnLogin.contains(p.getName())) {
            playersToKillOnLogin.remove(p.getName());

            saveToFile();

            return true;
        }

        return false;
    }

    public void clearPlayersToKillOnLoginList() {
        playersToKillOnLogin.clear();

        saveToFile();
    }

    private void saveToFile() {
        new BukkitRunnable() {
            @Override
            public void run() {
                playersToKillOnLoginConfig.set("players", playersToKillOnLogin.toArray());
                try {
                    playersToKillOnLoginConfig.save(playersToKillOnLoginFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save to '" + FILENAME + "': " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this.plugin);
    }
}
