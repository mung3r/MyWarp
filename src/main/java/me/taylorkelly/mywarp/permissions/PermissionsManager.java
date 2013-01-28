package me.taylorkelly.mywarp.permissions;

import java.util.LinkedHashMap;
import java.util.Map;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.WarpLogger;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages PermissionHandlers and individual permissions
 * 
 */
public class PermissionsManager implements PermissionsHandler {
    private enum PermHandler {
        VAULT, PERMISSIONSEX, BPERMISSIONS2, GROUPMANAGER, SUPERPERMS, NONE
    }

    private static PermHandler permplugin = PermHandler.NONE;
    private transient PermissionsHandler handler = new NullHandler();
    private final transient Plugin plugin;
    private static PluginManager pm;

    public PermissionsManager(final Plugin plugin) {
        this.plugin = plugin;
        PermissionsManager.pm = plugin.getServer().getPluginManager();
        checkPermissions();
        registerPermissions();
    }
    
    /**
     * Returns if the given command sender has the given permission. Using Superperms only.
     * 
     * @param executor
     *            the command sender
     * @param node
     *            the permission node
     * @return true if the command sender has the permission, false if not
     */
    public boolean hasPermission(final CommandSender executor, final String node) {
        return executor.hasPermission(node);
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return handler.playerHasGroup(player, group);
    }

    // Only register permissions here that cannot be registered in plugin.yml!!!
    private void registerPermissions() {
        // mywarp.limit permissions
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            pm.addPermission(new org.bukkit.permissions.Permission(
                    "mywarp.limit." + warpLimit.name,
                    "Gives acess to the number of warps defined for '"
                            + warpLimit.name + "' in the config",
                    PermissionDefault.FALSE));
        }

        // mywarp.cooldown permissions
        for (Time warpCooldown : WarpSettings.warpCooldowns) {
            pm.addPermission(new org.bukkit.permissions.Permission(
                    "mywarp.cooldown." + warpCooldown.name,
                    "User is affected by the cooldowns defined for '"
                            + warpCooldown.name + "' in the config",
                    PermissionDefault.FALSE));
        }

        // mywarp.warmup permissions
        for (Time warpWarmup : WarpSettings.warpWarmups) {
            pm.addPermission(new org.bukkit.permissions.Permission(
                    "mywarp.warmup." + warpWarmup.name,
                    "User is affected by the warmups defined for '"
                            + warpWarmup.name + "' in the config",
                    PermissionDefault.FALSE));
        }

        // mywarp.warp.world permissions
        Map<String, Boolean> worldMap = new LinkedHashMap<String, Boolean>();
        for (World world : plugin.getServer().getWorlds()) {
            pm.addPermission(new org.bukkit.permissions.Permission(
                    "mywarp.warp.world." + world.getName(),
                    "User may warp to worlds in world '" + world.getName()
                            + "'", PermissionDefault.OP));
            worldMap.put("mywarp.warp.world." + world.getName(), true);
        }
        worldMap.put("mywarp.warp.world.currentworld", true);

        pm.addPermission(new org.bukkit.permissions.Permission(
                "mywarp.warp.world.*", "User may warp to all worlds",
                PermissionDefault.OP, worldMap));
    }

    private void checkPermissions() {
        final PluginManager pluginManager = plugin.getServer()
                .getPluginManager();

        try {
            RegisteredServiceProvider<Permission> permissionProvider = Bukkit
                    .getServicesManager().getRegistration(
                            net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                if (!(handler instanceof VaultHandler)) {
                    permplugin = PermHandler.VAULT;
                    WarpLogger.info("Using Vault for group support");
                    handler = new VaultHandler(permissionProvider.getProvider());
                }
                return;
            }
        } catch (NoClassDefFoundError e) {
            // Do nothing
        }

        final Plugin permExPlugin = pluginManager.getPlugin("PermissionsEx");
        if (permExPlugin != null && permExPlugin.isEnabled()) {
            if (!(handler instanceof PermissionsExHandler)) {
                permplugin = PermHandler.PERMISSIONSEX;
                String version = permExPlugin.getDescription().getVersion();
                WarpLogger.info("Using PermissionsEx v" + version
                        + " for group support");
                handler = new PermissionsExHandler();
            }
            return;
        }

        final Plugin bPermPlugin = pluginManager.getPlugin("bPermissions");
        if (bPermPlugin != null && bPermPlugin.isEnabled()) {
            if (bPermPlugin.getDescription().getVersion().charAt(0) == '2') {
                if (!(handler instanceof BPermissions2Handler)) {
                    permplugin = PermHandler.BPERMISSIONS2;
                    String version = bPermPlugin.getDescription().getVersion();
                    WarpLogger.info("Using bPermissions v" + version
                            + " for group support");
                    handler = new BPermissions2Handler();
                }
            }
            return;
        }

        final Plugin GMplugin = pluginManager.getPlugin("GroupManager");
        if (GMplugin != null && GMplugin.isEnabled()) {
            if (!(handler instanceof GroupManagerHandler)) {
                permplugin = PermHandler.GROUPMANAGER;
                String version = GMplugin.getDescription().getVersion();
                WarpLogger.info("Using GroupManager v" + version
                        + " for group support");
                handler = new GroupManagerHandler(GMplugin);
            }
            return;
        }

        if (permplugin == PermHandler.NONE) {
            if (!(handler instanceof SuperpermsHandler)) {
                WarpLogger.info("Using SuperPerms for group support");
                handler = new SuperpermsHandler();
            }
        }
    }
}
