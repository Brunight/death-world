package io.papermc.deathworld.listeners;

import io.papermc.deathworld.DeathWorldPlugin;
import io.papermc.deathworld.enums.DeathWorldMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                        case "killall", "world" -> {
                            this.plugin.setMode(DeathWorldMode.getMode(newMode));

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
                }
            }
        }
        return false;
    }
}