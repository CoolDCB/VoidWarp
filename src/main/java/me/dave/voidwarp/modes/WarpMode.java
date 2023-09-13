package me.dave.voidwarp.modes;

import me.dave.voidwarp.VoidWarp;
import me.dave.voidwarp.hook.EssentialsHook;
import me.dave.voidwarp.hook.HuskHomesHook;
import me.dave.voidwarp.hook.WarpSystemHook;
import me.dave.voidwarp.config.ConfigManager;
import me.dave.voidwarp.data.VoidMode;
import me.dave.voidwarp.data.VoidModeData;
import me.dave.voidwarp.data.VoidModeType;
import me.dave.voidwarp.data.WarpData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpMode extends VoidMode<WarpMode.WarpModeData> {

    public WarpMode(WarpModeData data) {
        super(data);
    }

    @Override
    public VoidModeType getVoidModeType() {
        return VoidModeType.WARP;
    }

    @Override
    public CompletableFuture<WarpData> getWarpData(Player player, ConfigManager.WorldData worldData) {
        CompletableFuture<WarpData> completableFuture = new CompletableFuture<>();

        if (data.getWarps().size() == 0) {
            completableFuture.complete(null);
            return completableFuture;
        }

        getClosestWarp(player, data.getWarps()).thenAccept(completableFuture::complete);

        return completableFuture;
    }

    /**
     * Get the closest warp to the player
     *
     * @param player Player to get the closest warp to
     * @param warps List of warp names that will be checked
     */
    private CompletableFuture<WarpData> getClosestWarp(Player player, Collection<String> warps) {
        CompletableFuture<WarpData> completableFuture = new CompletableFuture<>();

        switch (data.getPlugin()) {
            case EssentialsHook.PLUGIN_NAME -> {
                if (VoidWarp.getOrLoadHook(EssentialsHook.PLUGIN_NAME) instanceof EssentialsHook hook) {
                    completableFuture.complete(hook.getClosestWarp(player, warps));
                } else {
                    completableFuture.complete(null);
                }
            }
            case HuskHomesHook.PLUGIN_NAME -> {
                if (VoidWarp.getOrLoadHook(HuskHomesHook.PLUGIN_NAME) instanceof  HuskHomesHook hook) {
                    hook.getClosestWarp(player, warps).thenAccept(completableFuture::complete);
                } else {
                    completableFuture.complete(null);
                }
            }
            case WarpSystemHook.PLUGIN_NAME -> {
                if (VoidWarp.getOrLoadHook(WarpSystemHook.PLUGIN_NAME) instanceof WarpSystemHook hook) {
                    completableFuture.complete(hook.getClosestWarp(player, warps));
                } else {
                    completableFuture.complete(null);
                }
            }
        }

        return completableFuture;
    }

    public static class WarpModeData extends VoidModeData {
        private final String plugin;
        private final Collection<String> warps;

        public WarpModeData(String name, @Nullable String plugin, List<String> warps, boolean whitelist) {
            super(name);

            if (plugin == null) {
                if (VoidWarp.isPluginAvailable(EssentialsHook.PLUGIN_NAME)) {
                    plugin = EssentialsHook.PLUGIN_NAME;
                }
                else if (VoidWarp.isPluginAvailable(HuskHomesHook.PLUGIN_NAME)) {
                    plugin = HuskHomesHook.PLUGIN_NAME;
                }
                else if (VoidWarp.isPluginAvailable(WarpSystemHook.PLUGIN_NAME)) {
                    plugin = WarpSystemHook.PLUGIN_NAME;
                }
                else {
                    throw new IllegalArgumentException("No compatible warp plugins found for Warp Mode");
                }
            }

            this.plugin = plugin;

            switch (plugin) {
                case EssentialsHook.PLUGIN_NAME -> {
                    if (whitelist) {
                        this.warps = warps;
                    } else {
                        Collection<String> warpsCollection = ((EssentialsHook) VoidWarp.getOrLoadHook(EssentialsHook.PLUGIN_NAME)).getWarps();
                        warpsCollection.removeAll(warps);

                        this.warps = warpsCollection;
                    }
                }
                case HuskHomesHook.PLUGIN_NAME -> {
                    if (whitelist) {
                        this.warps = warps;
                    } else {
                        this.warps = new ArrayList<>();
                        ((HuskHomesHook) VoidWarp.getOrLoadHook(HuskHomesHook.PLUGIN_NAME)).getWarps().thenAccept(huskWarps -> {
                            huskWarps.removeAll(warps);
                            this.warps.addAll(huskWarps);
                        });
                    }
                }
                case WarpSystemHook.PLUGIN_NAME -> {
                    if (whitelist) {
                        this.warps = warps;
                    } else {
                        Collection<String> warpsCollection = ((WarpSystemHook) VoidWarp.getOrLoadHook(WarpSystemHook.PLUGIN_NAME)).getWarps();
                        warpsCollection.removeAll(warps);

                        this.warps = warpsCollection;
                    }
                }
                default -> throw new IllegalArgumentException(plugin + " is not a compatible plugin for Warp Mode (plugin names are case sensitive)");
            }
        }

        public String getPlugin() {
            return plugin;
        }

        public Collection<String> getWarps() {
            return warps;
        }
    }
}