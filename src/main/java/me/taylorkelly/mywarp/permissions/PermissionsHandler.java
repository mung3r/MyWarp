package me.taylorkelly.mywarp.permissions;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.timer.Cooldown;
import me.taylorkelly.mywarp.timer.Warmup;
import me.taylorkelly.mywarp.utils.WarpLogger;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionsHandler implements IPermissionsHandler {
    private enum PermHandler {
        VAULT, PERMISSIONSEX, BPERMISSIONS2, GROUPMANAGER, SUPERPERMS, NONE
    }

    private static PermHandler permplugin = PermHandler.NONE;
    private transient IPermissionsHandler handler = new NullHandler();
    private final transient Plugin plugin;
    private static PluginManager pm;

    public PermissionsHandler(final Plugin plugin) {
        this.plugin = plugin;
        PermissionsHandler.pm = plugin.getServer().getPluginManager();
        checkPermissions();
        registerPermissions();
    }

    public boolean hasPermission(final Player player, final String node) {
        return player.hasPermission(node);
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return handler.playerHasGroup(player, group);
    }

    // Only register permissions here that cannot be registered in plugin.yml!!!
    public void registerPermissions() {
        // warp.limit permissions
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            pm.addPermission(new org.bukkit.permissions.Permission("mywarp.limit."
                    + warpLimit.name,
                    "Gives acess to the number of warps defined for "
                            + warpLimit.name + " in the config",
                    PermissionDefault.FALSE));
        }

        // warp.cooldown permissions
        for (Cooldown warpCooldown : WarpSettings.warpCooldowns) {
            pm.addPermission(new org.bukkit.permissions.Permission("mywarp.cooldown."
                    + warpCooldown.name,
                    "User is affected by the cooldowns defined for " + warpCooldown.name
                            + " in the config", PermissionDefault.FALSE));
        }

        // warp.warmup permissions
        for (Warmup warpWarmup : WarpSettings.warpWarmups) {
            pm.addPermission(new org.bukkit.permissions.Permission("mywarp.warmup."
                    + warpWarmup.name, "User is affected by the warmups defined for "
                    + warpWarmup.name + " in the config", PermissionDefault.FALSE));
        }
    }

    public void checkPermissions() {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();

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
                WarpLogger.info("Using PermissionsEx v" + version);
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
                    WarpLogger.info("Using bPermissions v" + version + " for group support");
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
                WarpLogger.info("Using GroupManager v" + version + " for group support");
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
