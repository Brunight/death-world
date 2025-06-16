package io.papermc.deathworld.listeners;

import io.papermc.deathworld.DeathWorldPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerPortalListener implements Listener {
    private final DeathWorldPlugin plugin;

    public PlayerPortalListener(DeathWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World worldPlayerIsTeleportingFrom = player.getWorld();
        World currentWorld = plugin.worldManager.getCurrentWorld();
        World currentWorldNether = plugin.worldManager.getCurrentWorldNether();
        World currentWorldTheEnd = plugin.worldManager.getCurrentWorldTheEnd();

        if (worldPlayerIsTeleportingFrom.equals(currentWorld)) {
            if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
                // If the player is using an end portal, teleport them to the end world
                event.getTo().setWorld(currentWorldTheEnd);
                return;
            }

            // If the player is using a nether portal, teleport them to the nether world
            event.getTo().setWorld(currentWorldNether);
        } else if (worldPlayerIsTeleportingFrom.equals(currentWorldNether) || worldPlayerIsTeleportingFrom.equals(currentWorldTheEnd)) {
            // If the player is using a nether portal or end portal from the nether or end world, teleport them back to the main world
            event.getTo().setWorld(currentWorld);
        }
    }
}
