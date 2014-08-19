package me.taylorkelly.mywarp.permissions;

import java.util.HashSet;
import java.util.Set;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.LimitBundle;
import me.taylorkelly.mywarp.economy.FeeBundle;
import me.taylorkelly.mywarp.permissions.valuebundles.MultiworldValueBundleManager;
import me.taylorkelly.mywarp.permissions.valuebundles.SimpleValueBundleManager;
import me.taylorkelly.mywarp.timer.TimeBundle;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages PermissionHandlers and individual permissions
 */
public class PermissionsManager implements PermissionsHandler {

    /**
     * A set that contains all manually registered permissions
     */
    private final Set<Permission> registeredPermissions = new HashSet<Permission>();

    /**
     * The permissions-handler
     */
    private final PermissionsHandler handler;

    /**
     * The fee-bundle-manager
     */
    private final SimpleValueBundleManager<FeeBundle> feeBundleManager;

    /**
     * The time-bundle-manager
     */
    private final SimpleValueBundleManager<TimeBundle> timeBundleManager;

    /**
     * The limit-bundle-manager
     */
    private final MultiworldValueBundleManager<LimitBundle> limitBundleManager;

    /**
     * Setups the permissions-manager.
     */
    public PermissionsManager() {
        // setup the permissions handler
        handler = setupHandler();

        // setup value-bundles
        feeBundleManager = new SimpleValueBundleManager<FeeBundle>(this, MyWarp.inst().getSettings()
                .getEconomyConfiguredFeeBundles(), MyWarp.inst().getSettings().getEconomyDefaultFeeBundle());
        timeBundleManager = new SimpleValueBundleManager<TimeBundle>(this, MyWarp.inst().getSettings()
                .getTimersConfiguredTimeBundles(), MyWarp.inst().getSettings().getTimersDefaultTimeBundle());
        limitBundleManager = new MultiworldValueBundleManager<LimitBundle>(this, MyWarp.inst().getSettings()
                .getLimitsConfiguredLimitBundles(), MyWarp.inst().getSettings().getLimitsDefaultLimitBundle()) {
            @Override
            protected void registerPermissions(PermissionsManager manager) {
                // mywarp.limit.[IDENTIFIER]
                super.registerPermissions(manager);

                // per world overrides
                for (World world : MyWarp.server().getWorlds()) {
                    String perm = "mywarp.limit.disobey." + world.getName();

                    // mywarp.limit.disobey.[WORLDNAME].total
                    Permission totalPerm = new Permission(perm + ".total");
                    totalPerm.addParent("mywarp.limit.disobey.total", true);
                    totalPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
                    manager.registerPermission(totalPerm);

                    // mywarp.limit.disobey.[WORLDNAME].private
                    Permission privatePerm = new Permission(perm + ".private");
                    privatePerm.addParent("mywarp.limit.disobey.private", true);
                    privatePerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
                    manager.registerPermission(privatePerm);

                    // mywarp.limit.disobey.[WORLDNAME].public
                    Permission publicPerm = new Permission(perm + ".public");
                    publicPerm.addParent("mywarp.limit.disobey.public", true);
                    publicPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
                    manager.registerPermission(publicPerm);
                }
            }
        };

        // register mywarp.warp.world permissions
        for (World world : MyWarp.server().getWorlds()) {
            Permission worldPerm = new Permission("mywarp.warp.world." + world.getName());
            worldPerm.addParent("mywarp.warp.world.*", true);
            registerPermission(worldPerm);
        }
    }

    /**
     * Returns whether the given player may access warps in the given world from
     * his current location
     * 
     * @param player
     *            the player
     * @param world
     *            the world
     * @return true if the player may use warps in this world
     */
    public boolean canAccessWorld(Player player, World world) {
        if (player.getWorld().equals(world) && hasPermission(player, "mywarp.warp.world.currentworld")) {
            return true;
        }
        if (hasPermission(player, "mywarp.warp.world." + world.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Gets the fee-bundle-manager that manages {@link FeeBundle}s.
     * 
     * @return the used fee-bundle-manager
     */
    public SimpleValueBundleManager<FeeBundle> getFeeBundleManager() {
        return feeBundleManager;
    }

    /**
     * Gets the time-bundle-manager that manages {@link TimeBundle}s.
     * 
     * @return the used time-bundle-manager
     */
    public SimpleValueBundleManager<TimeBundle> getTimeBundleManager() {
        return timeBundleManager;
    }

    /**
     * Gets the limit-bundle-manager that manages {@link LimitBundle}s.
     * 
     * @return the used limit-bundle-manager
     */
    public MultiworldValueBundleManager<LimitBundle> getLimitBundleManager() {
        return limitBundleManager;
    }

    /**
     * Returns if the given command sender has the given permission. Using
     * Superperms only.
     * 
     * @param sender
     *            the command sender
     * @param node
     *            the permission node
     * @return true if the command sender has the permission, false if not
     */
    public boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    public boolean playerHasGroup(final Player player, final String group) {
        return handler.playerHasGroup(player, group);
    }

    /**
     * Registers the given permission to the server.
     * 
     * @param perm
     *            the permission
     */
    public void registerPermission(Permission perm) {
        MyWarp.server().getPluginManager().addPermission(perm);
        registeredPermissions.add(perm);
    }

    /**
     * Setups a permissionsHandler by searching for supported plugins on the
     * server. Vault will always take preference over other supported plugins
     * for obvious reasons.
     * 
     * This method will never return null, but a superperms handler instead.
     * 
     * @return the created permissionsHander
     */
    private PermissionsHandler setupHandler() {
        // check for Vault first!
        try {
            RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = Bukkit
                    .getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                MyWarp.logger().info("Using Vault for group support");
                return new VaultHandler(permissionProvider.getProvider());
            }
        } catch (NoClassDefFoundError e) {
        }

        Plugin checkPlugin;

        checkPlugin = MyWarp.server().getPluginManager().getPlugin("bPermissions");
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            // we support bPermissions2 only
            if (checkPlugin.getDescription().getVersion().charAt(0) == '2') {
                MyWarp.logger().info(
                        "Using bPermissions v" + checkPlugin.getDescription().getVersion()
                                + " for group support");
                return new BPermissions2Handler();
            }
        }

        checkPlugin = MyWarp.server().getPluginManager().getPlugin("GroupManager");
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            MyWarp.logger()
                    .info("Using GroupManager v" + checkPlugin.getDescription().getVersion()
                            + " for group support");
            return new GroupManagerHandler(checkPlugin);
        }

        checkPlugin = MyWarp.server().getPluginManager().getPlugin("PermissionsEx");
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            MyWarp.logger().info(
                    "Using PermissionsEx v" + checkPlugin.getDescription().getVersion()
                            + " for group support");
            return new PermissionsExHandler();
        }

        MyWarp.logger().info(
                "No supported permissions plugin found, using Superperms fallback for group support");
        return new SuperpermsHandler();
    }

    /**
     * Unregisters all permissions from the server that where manually
     * registered before.
     */
    public void unregisterPermissions() {
        for (Permission perm : registeredPermissions) {
            MyWarp.server().getPluginManager().removePermission(perm);
        }
        registeredPermissions.clear();
    }
}
