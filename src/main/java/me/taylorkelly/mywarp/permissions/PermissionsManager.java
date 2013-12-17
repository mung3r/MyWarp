package me.taylorkelly.mywarp.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.economy.WarpFees;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages PermissionHandlers and individual permissions
 */
public class PermissionsManager implements PermissionsHandler {

    /**
     * A set that contains all registered permissions
     */
    private final Set<Permission> registeredPermissions = new HashSet<Permission>();

    /**
     * The permissions handler in use
     */
    private final PermissionsHandler handler;

    /**
     * Setups the permissions-manager.
     */
    public PermissionsManager() {
        // setup the permissions handler
        handler = setupHandler();
    }

    /**
     * Gets the cooldown (as {@link Time}) affective for this player. Returns the
     * default-cooldown if the player does not have any of the of the specific
     * cooldowns
     * 
     * @param player
     *            the player
     * @return the cooldown affective from this player
     */
    public Time getCooldown(Player player) {
        for (Time cooldown : MyWarp.inst().getWarpSettings().timersCooldowns) {
            if (hasPermission(player, "mywarp.cooldown." + cooldown.getName())) {
                return cooldown;
            }
        }
        return MyWarp.inst().getWarpSettings().timersDefaultCooldown;
    }

    /**
     * Gets the {@link WarpFees} affective for this player. Returns the
     * default-fees if the player does not have any of the of the specific fees
     * 
     * @param sender
     *            the sender
     * @return the fees affective for this sender
     */
    public WarpFees getEconomyPrices(CommandSender sender) {
        for (WarpFees warpFees : MyWarp.inst().getWarpSettings().economyFees) {
            if (hasPermission(sender, "mywarp.economy." + warpFees.getName())) {
                return warpFees;
            }
        }
        return MyWarp.inst().getWarpSettings().economyDefaultFees;
    }

    /**
     * Gets the warmup (as {@link Time}) affective for this player. Returns the
     * default-warmup if the player does not have any of the of the specific
     * warmups
     * 
     * @param player
     *            the player
     * @return the warmup affective for this player
     */
    public Time getWarmup(Player player) {
        for (Time warmup : MyWarp.inst().getWarpSettings().timersWarmups) {
            if (hasPermission(player, "mywarp.warmup." + warmup.getName())) {
                return warmup;
            }
        }
        return MyWarp.inst().getWarpSettings().timersDefaultWarmup;
    }

    /**
     * Gets the {@link WarpLimit} affective for this player. Returns the default
     * limit if the player does not have any of the specific limits
     * 
     * @param player
     *            the player
     * @return the limit affective for this player
     */
    public WarpLimit getWarpLimit(Player player) {
        for (WarpLimit warpLimit : MyWarp.inst().getWarpSettings().limitsWarpLimits) {
            if (!warpLimit.isEffectiveWorld(player.getWorld().getName())) {
                continue;
            }
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit;
            }
        }
        return MyWarp.inst().getWarpSettings().limitsDefaultWarpLimit;
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
    public boolean hasPermission(final CommandSender sender, final String node) {
        return sender.hasPermission(node);
    }

    /**
     * Checks if the given player may access warps in the world of the given
     * name from is current location
     * 
     * @param player
     *            the player
     * @param worldName
     *            the name of the world
     * @return true if the player may use warps in this world, false if not
     */
    public boolean playerCanAccessWorld(Player player, String worldName) {
        if (player.getWorld().getName().equals(worldName)
                && hasPermission(player, "mywarp.warp.world.currentworld")) {
            return true;
        }
        if (hasPermission(player, "mywarp.warp.world." + worldName)) {
            return true;
        }
        return false;
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
    private void registerPermission(Permission perm) {
        MyWarp.server().getPluginManager().addPermission(perm);
        registeredPermissions.add(perm);
    }

    /**
     * Registers permissions on the server. This method should only be used for
     * permissions that must be registered dynamically.
     */
    public void registerPermissions() {
        // mywarp.limit permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().limitsWarpLimits) {
            registerPermission(new Permission("mywarp.limit." + container.getName(),
                    "Gives acess to the number of warps defined for '" + container.getName()
                            + "' in the config", PermissionDefault.FALSE));
        }
        // per world overrides
//        Map<String, Boolean> totalLimits = new HashMap<String, Boolean>();
//        Map<String, Boolean> privateLimits = new HashMap<String, Boolean>();
//        Map<String, Boolean> publicLimits = new HashMap<String, Boolean>();
        for (World world : MyWarp.server().getWorlds()) {
            //Map<String, Boolean> perWorldLimits = new HashMap<String, Boolean>();
            String perm = "mywarp.limit.disobey." + world.getName();

            // mywarp.limit.disobey.[WORLDNAME].total
            Permission totalPerm = new Permission(perm + ".total", "User may disobey the total-warp limit in '"
                    + world.getName() + "'");
            totalPerm.addParent("mywarp.limit.disobey.total", true);
            totalPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
            registerPermission(totalPerm);
            //totalLimits.put(perm + ".total", true);
            //perWorldLimits.put(perm + ".total", true);

            // mywarp.limit.disobey.[WORLDNAME].private
            Permission privatePerm = new Permission(perm + ".private",
                    "User may disobey the private-warp limit in '" + world.getName() + "'");
            totalPerm.addParent("mywarp.limit.disobey.private", true);
            totalPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
            registerPermission(privatePerm);
//            privateLimits.put(perm + ".private", true);
//            perWorldLimits.put(perm + ".private", true);

            // mywarp.limit.disobey.[WORLDNAME].public
            Permission publicPerm = new Permission(perm + ".public", "User may disobey the public-warp limit in '"
                    + world.getName() + "'");
            totalPerm.addParent("mywarp.limit.disobey.public", true);
            totalPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
            registerPermission(publicPerm);
//            publicLimits.put(perm + ".public", true);
//            perWorldLimits.put(perm + ".public", true);

            // mywarp.limit.disobey.[WORLDNAME].*
//            registerPermission(new Permission("mywarp.limit.disobey." + world.getName() + ".*",
//                    "User may disobey all limits in '" + world.getName() + "'", perWorldLimits));
        }
//        registerPermission(new Permission("mywarp.limit.disobey.total",
//                "User may disobey the total-warp limit in all worlds", totalLimits));
//        registerPermission(new Permission("mywarp.limit.disobey.private",
//                "User may disobey the private-warp limit in all worlds", privateLimits));
//        registerPermission(new Permission("mywarp.limit.disobey.public",
//                "User may disobey the public-warp limit in all worlds", publicLimits));

        // mywarp.cooldown permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().timersCooldowns) {
            registerPermission(new Permission("mywarp.cooldown." + container.getName(),
                    "User is affected by the cooldowns defined for '" + container.getName()
                            + "' in the config", PermissionDefault.FALSE));
        }

        // mywarp.warmup permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().timersWarmups) {
            registerPermission(new Permission(
                    "mywarp.warmup." + container.getName(),
                    "User is affected by the warmups defined for '" + container.getName() + "' in the config",
                    PermissionDefault.FALSE));
        }

        // mywarp.warp.world permissions
        Map<String, Boolean> worldAccess = new HashMap<String, Boolean>();
        for (World world : MyWarp.server().getWorlds()) {
            registerPermission(new Permission("mywarp.warp.world." + world.getName(),
                    "User may warp to worlds in world '" + world.getName() + "'"));
            worldAccess.put("mywarp.warp.world." + world.getName(), true);
        }
        worldAccess.put("mywarp.warp.world.currentworld", true);

        registerPermission(new Permission("mywarp.warp.world.*", "User may warp to all worlds", worldAccess));

        // mywarp.economy permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().economyFees) {
            registerPermission(new Permission("mywarp.economy." + container.getName(),
                    "User is affected by the fees defined for '" + container.getName() + "' in the config",
                    PermissionDefault.FALSE));
        }
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
        PermissionsHandler handler = null;

        // check for Vault first!
        try {
            RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = Bukkit
                    .getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                MyWarp.logger().info("Using Vault for group support");
                handler = new VaultHandler(permissionProvider.getProvider());
                return handler;
            }
        } catch (NoClassDefFoundError e) {
            // Do nothing
        }

        Plugin checkPlugin;

        checkPlugin = MyWarp.server().getPluginManager().getPlugin("bPermissions");
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            // we support bPermissions2 only
            if (checkPlugin.getDescription().getVersion().charAt(0) == '2') {
                MyWarp.logger().info(
                        "Using bPermissions v" + checkPlugin.getDescription().getVersion()
                                + " for group support");
                handler = new BPermissions2Handler();
                return handler;
            }
        }

        checkPlugin = MyWarp.server().getPluginManager().getPlugin("GroupManager");
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            handler = new GroupManagerHandler(checkPlugin);

            MyWarp.logger()
                    .info("Using GroupManager v" + checkPlugin.getDescription().getVersion()
                            + " for group support");
            return handler;
        }

        checkPlugin = MyWarp.server().getPluginManager().getPlugin("PermissionsEx");
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            handler = new PermissionsExHandler();

            MyWarp.logger().info(
                    "Using PermissionsEx v" + checkPlugin.getDescription().getVersion()
                            + " for group support");
            return handler;
        }

        MyWarp.logger().info(
                "No supported permissions plugin found, using Superperms fallback for group support");
        return new SuperpermsHandler();
    }

    /**
     * Unregisters permissions from the server. This method mirrors
     * {@link #registerPermissions()} and unregisters all permissions registered
     * before.
     */
    public void unregisterPermissions() {
        for (Permission perm : registeredPermissions) {
            MyWarp.server().getPluginManager().removePermission(perm);
        }
        registeredPermissions.clear();
    }
}
