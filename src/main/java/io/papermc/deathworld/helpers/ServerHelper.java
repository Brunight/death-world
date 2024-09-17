package io.papermc.deathworld.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ServerHelper {
    public static void say(String message) {
        Bukkit.broadcast(Component.text("[Server] " + message).color(NamedTextColor.LIGHT_PURPLE));
    }
}
