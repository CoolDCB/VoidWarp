package me.dave.voidwarp.data;

import me.dave.voidwarp.ConfigManager;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface VoidModes {
    CompletableFuture<WarpData> getWarpData(Player player, ConfigManager.WorldData worldData);
}