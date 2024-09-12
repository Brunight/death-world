package io.papermc.deathworld.Helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

public class PlayerHelper {
    public static void setPlayerNickname(Player player, Component component) {
        player.playerListName(component);
        player.displayName(component);
    }

    public static void setDeathCountIntoPlayerNickname(Player player, Integer count) {
        Component newNickname = Component.text(player.getName(), NamedTextColor.GOLD);

        if (count > 0) {
            newNickname = newNickname.append(Component.text(" [" + count + "]", NamedTextColor.RED));
        }

        setPlayerNickname(player, newNickname);
    }


    public static void resetPlayer(Player player) {
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.clearActivePotionEffects();
        // Clear all advancements for the player
        Bukkit.getServer().advancementIterator().forEachRemaining(adv -> {
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            for (String criteria : progress.getAwardedCriteria())
                progress.revokeCriteria(criteria);
        });
    }

    public static void teleportAllPlayersToWorld(World world) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.teleport(world.getSpawnLocation());
        });
    }
}
