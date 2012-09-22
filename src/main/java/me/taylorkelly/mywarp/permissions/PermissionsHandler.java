package me.taylorkelly.mywarp.permissions;

import me.taylorkelly.mywarp.WarpSettings;
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

    public PermissionsHandler(final Plugin plugin) {
        this.plugin = plugin;
        checkPermissions();
        registerLimitPermissions();
        registerTimerPermissions();
    }

    public boolean hasPermission(final Player player, final String node) {
        return player.hasPermission(node);
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return handler.playerHasGroup(player, group);
    }

    public void registerLimitPermissions() {
        for (int i = 0; i < WarpSettings.warpLimits.size(); i++) {
            plugin.getServer()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.limit."
                                    + WarpSettings.warpLimits.get(i).getName(),
                                    "Gives acess to the number of warps defined for "
                                            + WarpSettings.warpLimits.get(i).getName()
                                            + " in the config", PermissionDefault.FALSE));
        }
    }

    public void registerTimerPermissions() {
        for (int i = 0; i < WarpSettings.warpCooldowns.size(); i++) {
            plugin.getServer()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.cooldown."
                                    + WarpSettings.warpCooldowns.get(i).name,
                                    "User is affected by the cooldowns defined for "
                                            + WarpSettings.warpCooldowns.get(i).name
                                            + " in the config", PermissionDefault.FALSE));
        }
        for (int i = 0; i < WarpSettings.warpWarmups.size(); i++) {
            plugin.getServer()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.warmup."
                                    + WarpSettings.warpWarmups.get(i).name,
                                    "User is affected by the warmups defined for "
                                            + WarpSettings.warpWarmups.get(i).name
                                            + " in the config", PermissionDefault.FALSE));
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
                    WarpLogger.info("Access Control: Using Vault");
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
                WarpLogger.info("Access Control: Using PermissionsEx v" + version);
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
                    WarpLogger.info("Access Control: Using bPermissions v" + version);
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
                WarpLogger.info("Access Control: Using GroupManager v" + version);
                handler = new GroupManagerHandler(GMplugin);
            }
            return;
        }

        if (permplugin == PermHandler.NONE) {
            if (!(handler instanceof SuperpermsHandler)) {
                WarpLogger.info("Access Control: Using SuperPerms");
                handler = new SuperpermsHandler();
            }
        }
    }
}
