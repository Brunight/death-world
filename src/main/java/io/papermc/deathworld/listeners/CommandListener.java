package io.papermc.deathworld.listeners;

import io.papermc.deathworld.DeathWorldPlugin;
import io.papermc.deathworld.enums.DeathWorldMode;
import io.papermc.deathworld.helpers.ServerHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class CommandListener implements CommandExecutor {
    private final DeathWorldPlugin plugin;

    public CommandListener(DeathWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dw")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("start")) {
                    this.plugin.worldManager.createNewGameplayWorld();
                    return true;
                } else if (args[0].equalsIgnoreCase("lobby")) {
                    if (sender instanceof Player player) {
                        player.teleport(this.plugin.worldManager.getLobbyWorld().getSpawnLocation());
                        player.sendMessage("You have been teleported to the lobby.");
                    } else {
                        sender.sendMessage("Only players can use this command.");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("setmode")) {
                    String newMode = args[1];
                    return switch (newMode) {
                        case "killall", "default", "none" -> {
                            DeathWorldMode newModeEnum = DeathWorldMode.getMode(newMode);
                            this.plugin.setMode(newModeEnum);
                            ServerHelper.say("Death World mode was set to '" + newModeEnum.toString() + "'!");

                            yield true;
                        }
                        default -> false;
                    };
                } else if (args[0].equalsIgnoreCase("setautogeneratenewworld")) {
                    String configArg = args[1];
                    if (configArg.equalsIgnoreCase("true") || configArg.equalsIgnoreCase("false")) {
                        Boolean newValue = configArg.equalsIgnoreCase("true");
                        this.plugin.mainConfig.set("autoGenerateNewWorld", newValue);
                        this.plugin.saveConfig();

                        return true;
                    }

                    return false;
                } else if (args[0].equalsIgnoreCase("softkill")) {
                    try {
                        Player target = Objects.requireNonNull(Bukkit.getPlayer(args[1]));
                        target.setMetadata("softkill", new FixedMetadataValue(plugin, true));
                        target.sendMessage(Component.text("You were softkilled by an admin. This death will not be counted or tracked.").color(NamedTextColor.GOLD));
                        target.setHealth(0);
                        sender.sendMessage(Component.text("You softkilled " + target.getName()).color(NamedTextColor.GOLD));
                    } catch (Exception e) {
                        sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.GOLD));
                        return false;
                    }

                    return true;
                }
            }
        }
        return false;
    }
}