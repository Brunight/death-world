package io.papermc.deathworld.listeners;

import io.papermc.deathworld.DeathWorldPlugin;
import io.papermc.deathworld.enums.DeathWorldMode;
import io.papermc.deathworld.helpers.PlayerHelper;
import io.papermc.deathworld.helpers.ServerHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final DeathWorldPlugin plugin;

    public PlayerJoinListener(DeathWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Integer deathCount = this.plugin.deathCountManager.getPlayerDeaths(player);
        PlayerHelper.setDeathCountIntoPlayerNickname(player, deathCount);

        World currentWorld = this.plugin.worldManager.getCurrentWorld();
        World currentWorldNether = this.plugin.worldManager.getCurrentWorldNether();
        World currentWorldTheEnd = this.plugin.worldManager.getCurrentWorldTheEnd();

        // Check if the player is already in the current world
        if (
                player.getWorld().equals(currentWorld) ||
                        player.getWorld().equals(currentWorldNether) ||
                        player.getWorld().equals(currentWorldTheEnd)
        ) {
            if (this.plugin.getMode() == DeathWorldMode.KILL_ALL) {
                if (this.plugin.playersToKillOnLoginManager.isPlayerInKillOnLoginList(player)) {
                    player.sendMessage(Component.text("Someone died while you were offline.").color(NamedTextColor.GOLD));
                    ServerHelper.say(player.getName() + " was offline when a death occurred and died as well.");
                    player.setHealth(0);
                }
            }

            return;
        }

        // Player is in an incorrect world. Fix respawn location, clear data and teleport to current world.

        player.teleport(currentWorld.getSpawnLocation());
        PlayerHelper.resetPlayer(player);

        // Reset the player's spawn point to the current world's spawn location
        player.setRespawnLocation(currentWorld.getSpawnLocation(), true);

        this.plugin.getSLF4JLogger().info(
                "Player " + player.getName() + " has been teleported to the current world: " + currentWorld.getName());
        player.sendMessage(
                Component.text("A new world was created while you were offline.").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("You will now be teleported to it.").color(NamedTextColor.GOLD));
    }
}
