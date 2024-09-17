package io.papermc.deathworld.listeners;

import io.papermc.deathworld.DeathWorldPlugin;
import io.papermc.deathworld.enums.DeathWorldMode;
import io.papermc.deathworld.helpers.PlayerHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.time.Duration;

public class PlayerDeathListener implements Listener {
    private final DeathWorldPlugin plugin;
    private Player playerResetCause;

    public PlayerDeathListener(DeathWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getPlayer();
        final DeathWorldMode mode = this.plugin.getMode();
        final boolean autoGenerateNewWorld = this.plugin.mainConfig.getBoolean("autoGenerateNewWorld");

        // Fix broken bed (null respawn point) for killall mode
        if (mode.equals(DeathWorldMode.KILL_ALL)) {
            World currentWorld = this.plugin.worldManager.getCurrentWorld();
            PlayerHelper.fixPlayerRespawnPoint(deadPlayer, currentWorld);
        }

        if (!shouldDeathCount(deadPlayer)) {
            event.deathMessage(null);

            return;
        }

        playerResetCause = deadPlayer;

        Integer newDeathCount = this.plugin.deathCountManager.addPlayerDeath(playerResetCause);
        PlayerHelper.setDeathCountIntoPlayerNickname(playerResetCause, newDeathCount);

        String deathLog = playerResetCause.getName() + " died: "
                + PlainTextComponentSerializer.plainText().serialize(event.deathMessage());
        this.plugin.deathLogHelper.log(deathLog);

        Location lobby = this.plugin.worldManager.getLobbyWorld().getSpawnLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(event.deathMessage().color(NamedTextColor.RED));
            if (mode.equals(DeathWorldMode.DEFAULT)) {
                player.setRespawnLocation(lobby, true);
                if (player.equals(playerResetCause)) {
                    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                        playerResetCause.spigot().respawn();
                    }, 10);
                }
            }
            final Component title = Component.text(playerResetCause.getName() + " died", NamedTextColor.RED);
            final Component subtitle = event.deathMessage().color(NamedTextColor.WHITE);
            final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000),
                    Duration.ofMillis(500));

            player.showTitle(Title.title(title, subtitle, times));
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (mode.equals(DeathWorldMode.DEFAULT)) {
                if (autoGenerateNewWorld) {
                    this.plugin.worldManager.createNewGameplayWorld();
                }
            } else if (mode.equals(DeathWorldMode.KILL_ALL)) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (p.equals(playerResetCause)) {
                        return;
                    }

                    p.setHealth(0);
                });

                this.plugin.playersToKillOnLoginManager.addOfflinePlayersToKillOnLoginList();
            }
            playerResetCause = null;
        }, 60);
    }

    private Boolean shouldDeathCount(Player p) {
        if (p.getWorld().getName().equals("lobby") || playerResetCause != null) {
            return false;
        }

        if (this.plugin.playersToKillOnLoginManager.removePlayerFromKillOnLoginList(p)) {
            return false;
        }

        boolean hasSoftkillMetadata = false;

        for (MetadataValue meta : p.getMetadata("softkill")) {
            if (meta.getOwningPlugin() == plugin) {
                hasSoftkillMetadata = meta.asBoolean();
            }
        }

        if (hasSoftkillMetadata) {
            p.removeMetadata("softkill", plugin);

            return false;
        }

        return true;
    }
}
