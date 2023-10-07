package org.pipeman.bb;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.pipeman.bb.commands.BankCommand;
import org.pipeman.bb.commands.MineCommand;
import org.pipeman.bb.commands.TeleportCommands;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.games.SpleefGame;
import org.pipeman.bb.games.arena.ArenaGame;
import org.pipeman.bb.games.boat_race.BoatRaceGame;
import org.pipeman.bb.games.breaking_ice.BreakingIceGame;
import org.pipeman.bb.games.cactus.CactusTowerGame;
import org.pipeman.bb.games.cake.CakeGame;
import org.pipeman.bb.games.elytra.ElytraRaceGame;
import org.pipeman.bb.games.get_to_the_top.GetToTheTopGame;
import org.pipeman.bb.games.ice_boat_race.IceRaceGame;
import org.pipeman.bb.games.jnr.JNRGame;
import org.pipeman.bb.games.jungle_arena.JungleArenaGame;
import org.pipeman.bb.games.mine.MineGame;
import org.pipeman.bb.games.mlg.MLGGame;
import org.pipeman.bb.games.pull_ships.PullShipsGame;
import org.pipeman.bb.games.shoot_the_chicken.ShootTheChickenGame;
import org.pipeman.bb.games.shooting_ranges.DarkOakShooting;
import org.pipeman.bb.games.shooting_ranges.JungleShooting;
import org.pipeman.bb.utils.DontMoveActionManager;
import org.pipeman.bb.utils.SignTeleporter;

import java.util.Objects;

public final class Main extends JavaPlugin {
    public static Main INSTANCE;
    private static World gameWorld;

    @Override
    public void onEnable() {
        gameWorld = Objects.requireNonNull(Bukkit.getWorld("world"), "World is null");
        INSTANCE = this;

        registerEventListener(new GameManager());
        registerEventListener(new SignTeleporter());
        registerEventListener(new TeleportCommands());
        registerEventListener(new DontMoveActionManager());

        registerGame(new MLGGame());
        registerGame(new MineGame());
        registerGame(new ArenaGame());
        registerGame(new SpleefGame());
        registerGame(new IceRaceGame());
        registerGame(new BoatRaceGame());
        registerGame(new PullShipsGame());
        registerGame(new ElytraRaceGame());
        registerGame(new JungleShooting());
        registerGame(new JungleArenaGame());
        registerGame(new DarkOakShooting());
        registerGame(new GetToTheTopGame());
        registerGame(new BreakingIceGame());
        registerGame(new CactusTowerGame());
        registerEventListener(new JNRGame());
        registerEventListener(new CakeGame());
        registerGame(new ShootTheChickenGame());

        registerCommand("mine", new MineCommand());
        registerCommand("shop", TeleportCommands.SHOP_EXECUTOR);
        registerCommand("spawn", TeleportCommands.SPAWN_EXECUTOR);
        registerCommand("bank", BankCommand.executor, BankCommand.completer);
        registerCommand("boatgame", (sender, command, label, args) -> {
            BoatRaceGame.resetGame();
            return true;
        });
        registerCommand("leavegame", TeleportCommands.LEAVE_EXECUTOR);

        SignTeleporter.registerSign(
                new Location(Bukkit.getWorld("world"), -198, 65, 155),
                new Location(Bukkit.getWorld("world"), -211, 35, 158, 96, -4),
                player -> GameManager.joinGame(player, GameManager.getGame(GetToTheTopGame.ID))
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerEventListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void registerGame(Game game) {
        GameManager.registerGame(game);
        registerEventListener(game);
    }

    private void registerCommand(String name, CommandExecutor executor) {
        registerCommand(name, executor, null);
    }

    private void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command == null)
            throw new RuntimeException("Failed to register command \"" + name + "\"! Add it to plugin.yml!");

        command.setExecutor(executor);
        if (tabCompleter != null) command.setTabCompleter(tabCompleter);
    }

    public static int scheduleTask(int delay, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(INSTANCE, task, delay);
    }

    public static @NotNull World getGameWorld() {
        return gameWorld;
    }
}
