package io.papermc.deathworld.managers;

import io.papermc.deathworld.DeathWorldPlugin;
import io.papermc.deathworld.helpers.ServerHelper;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class WorldManager {
    final private DeathWorldPlugin plugin;

    private World currentWorld;
    private World lobbyWorld;

    public WorldManager(DeathWorldPlugin plugin) {
        this.plugin = plugin;

        setupLobby();

        String currentWorldName = this.plugin.mainConfig.getString("currentWorld");
        currentWorld = loadWorld(currentWorldName);
        if (currentWorld == null) {
            currentWorld = lobbyWorld;
            plugin.mainConfig.set("currentWorld", "lobby");
            plugin.saveConfig();
        }
    }

    private World loadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // World not loaded; attempt to load it
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                world = Bukkit.createWorld(new WorldCreator(worldName));
            } else {
                return null;
            }
        }
        return world;
    }

    private void setupLobby() {
        lobbyWorld = loadWorld("lobby");
        if (lobbyWorld != null) {
            return;
        }

        this.plugin.getSLF4JLogger().info("Lobby world not found! Creating a new lobby world...");
        WorldCreator creator = new WorldCreator("lobby");
        creator.generator(new VoidWorldGenerator());
        creator.type(WorldType.FLAT);
        World world = Bukkit.createWorld(creator);
        if (world == null) {
            plugin.getSLF4JLogger().error("Something went wrong while creating the world.");
            return;
        }

        world.setSpawnLocation(0, 65, 0);

        world.setDifficulty(Difficulty.PEACEFUL);

        int centerX = 0;
        int centerZ = 0;
        int platformSize = 8; // Half of 16

        for (int x = -platformSize; x <= platformSize; x++) {
            for (int z = -platformSize; z <= platformSize; z++) {
                Block block = world.getBlockAt(centerX + x, 64, centerZ + z); // Y = 64 is a standard height
                block.setType(Material.BEDROCK);
            }
        }

        lobbyWorld = world;
        this.plugin.getSLF4JLogger().info("Lobby created successfully.");
    }

    public World getLobbyWorld() {
        return this.lobbyWorld;
    }

    public World getCurrentWorld() {
        return this.currentWorld;
    }

    public void createNewGameplayWorld() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Generate the new world name
                String newWorldName = "world_" + System.currentTimeMillis();
                ServerHelper.say("Creating new world...");

                // Create the new worlds (Overworld, Nether, and End) on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (currentWorld != null && !currentWorld.equals(lobbyWorld)) {
                            // Unload and delete the Nether
                            World oldNetherWorld = loadWorld(currentWorld.getName() + "_nether");
                            if (oldNetherWorld != null) {
                                unloadAndDeleteWorld(oldNetherWorld);
                            }

                            // Unload and delete the End
                            World oldEndWorld = loadWorld(currentWorld.getName() + "_the_end");
                            if (oldEndWorld != null) {
                                unloadAndDeleteWorld(oldEndWorld);
                            }

                            // Unload and delete the Overworld
                            unloadAndDeleteWorld(currentWorld);
                        }

                        Difficulty difficulty = Bukkit.getWorlds().getFirst().getDifficulty();

                        // Create Overworld
                        World overworld = Bukkit
                                .createWorld(new WorldCreator(newWorldName).environment(World.Environment.NORMAL)
                                        .keepSpawnLoaded(TriState.FALSE));
                        if (overworld == null) {
                            plugin.getSLF4JLogger().error("Failed to create the Overworld.");
                            return;
                        }
                        overworld.setDifficulty(difficulty);

                        // Create Nether
                        World netherWorld = Bukkit.createWorld(
                                new WorldCreator(newWorldName + "_nether").environment(World.Environment.NETHER)
                                        .keepSpawnLoaded(TriState.FALSE));
                        if (netherWorld == null) {
                            plugin.getSLF4JLogger().error("Failed to create the Nether world.");
                            return;
                        }
                        netherWorld.setDifficulty(difficulty);

                        // Create The End
                        World endWorld = Bukkit.createWorld(
                                new WorldCreator(newWorldName + "_the_end").environment(World.Environment.THE_END)
                                        .keepSpawnLoaded(TriState.FALSE));
                        if (endWorld == null) {
                            plugin.getSLF4JLogger().error("Failed to create The End world.");
                            return;
                        }
                        endWorld.setDifficulty(difficulty);
                        // Update the current world reference to the new Overworld
                        currentWorld = overworld;
                        plugin.mainConfig.set("currentWorld", newWorldName);
                        plugin.saveConfig();

                        // Teleport all players to the new Overworld after creation
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.teleport(currentWorld.getSpawnLocation());
                                    // Reset the player's spawn point to the new world
                                    player.setRespawnLocation(currentWorld.getSpawnLocation(), true);
                                }
                                ServerHelper.say("World created. Teleporting online players...");
                                plugin.getSLF4JLogger()
                                        .info("All players have been teleported to the new Overworld: " + newWorldName);
                            }
                        }.runTask(plugin); // Run on the main thread
                    }
                }.runTask(plugin); // Run the world creation on the main thread
            }
        }.runTaskAsynchronously(plugin); // Run the overall task asynchronously
    }

    private void unloadAndDeleteWorld(World world) {
        if (world == null) {
            return;
        }

        // Ensure no players are in the world before unloading
        for (Player player : world.getPlayers()) {
            player.teleport(lobbyWorld.getSpawnLocation());
        }

        // Unload the world and save chunks
        boolean unloaded = Bukkit.unloadWorld(world, true);
        if (unloaded) {
            this.plugin.getSLF4JLogger().info("Successfully unloaded world: " + world.getName());

            // Schedule the deletion of the world folder
            new BukkitRunnable() {
                @Override
                public void run() {
                    File worldFolder = world.getWorldFolder();
                    try {
                        deleteWorldFolder(worldFolder);
                        plugin.getSLF4JLogger()
                                .info("World folder " + world.getName() + " has been successfully deleted.");
                    } catch (IOException e) {
                        plugin.getSLF4JLogger()
                                .error("Failed to delete world folder " + world.getName() + ": " + e.getMessage());
                    }
                }
            }.runTaskAsynchronously(plugin); // Run deletion asynchronously to avoid blocking the main thread
        } else {
            this.plugin.getSLF4JLogger().error("Failed to unload world: " + world.getName());
        }
    }

    private void deleteWorldFolder(File path) throws IOException {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteWorldFolder(file); // Recursively delete directories
                    } else {
                        if (!file.delete()) {
                            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!path.delete()) {
                throw new IOException("Failed to delete folder: " + path.getAbsolutePath());
            }
        }
    }

    // Custom chunk generator for a void world
    public static class VoidWorldGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world); // Return an empty chunk (void)
        }
    }
}
