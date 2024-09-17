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

        if (deadPlayer.getWorld().getName().equals("lobby") || playerResetCause != null) {
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
                Bukkit.getOnlinePlayers().forEach(p -> {
                    PlayerHelper.resetPlayer(p);
                    p.teleport(lobby);
                });
                if (autoGenerateNewWorld) {
                    this.plugin.worldManager.createNewGameplayWorld();
                }
            } else if (mode.equals(DeathWorldMode.KILL_ALL)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.equals(playerResetCause)) {
                        continue;
                    }

                    player.setHealth(0);
                }
            }
            playerResetCause = null;
        }, 60);
    }
}
