package io.papermc.deathworld.listeners;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.PortalType;
import org.bukkit.event.entity.EntityPortalEvent;
import io.papermc.deathworld.DeathWorldPlugin;
import io.papermc.deathworld.helpers.ServerHelper;

public class EntityPortalListener implements Listener {

  private final DeathWorldPlugin plugin;

  public EntityPortalListener(DeathWorldPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onEntityPortal(EntityPortalEvent event) {
    Entity entity = event.getEntity();
    World worldEntityIsTeleportingFrom = entity.getWorld();
    World currentWorld = plugin.worldManager.getCurrentWorld();
    World currentWorldNether = plugin.worldManager.getCurrentWorldNether();
    World currentWorldTheEnd = plugin.worldManager.getCurrentWorldTheEnd();

    if (worldEntityIsTeleportingFrom.equals(currentWorld)) {
      if (event.getPortalType().equals(PortalType.ENDER)) {
        event.getTo().setWorld(currentWorldTheEnd);
        return;
      }

      event.getTo().setWorld(currentWorldNether);
    } else if (worldEntityIsTeleportingFrom.equals(currentWorldNether)) {
      event.getTo().setWorld(currentWorld);
    } else if (worldEntityIsTeleportingFrom.equals(currentWorldTheEnd)) {
      if (event.getPortalType().equals(PortalType.END_GATEWAY)) {
        event.getTo().setWorld(currentWorldTheEnd);
        return;
      }
      event.getTo().setWorld(currentWorld);
    }
  }
}