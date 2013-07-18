package me.taylorkelly.mywarp.permissions;

import java.util.LinkedHashMap;
import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.economy.WarpFees;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.ValuePermissionContainer;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages PermissionHandlers and individual permissions
 */
public class PermissionsManager implements PermissionsHandler {

    /**
     * The permissions handle in use
     */
    private final PermissionsHandler handler;

    /**
     * Setups the permissions-manager.
     */
    public PermissionsManager() {
        // setup the permissions handler
        handler = setupHandler();

        // register dynamic permissions
        registerPermissions();
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

    @Override
    public boolean playerHasGroup(final Player player, final String group) {
        return handler.playerHasGroup(player, group);
    }

    /**
     * Registers permissions on the server. This method should only be used for
     * permissions that must be registered dynamically.
     */
    private void registerPermissions() {
        // mywarp.limit permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().warpLimits) {
            MyWarp.server()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.limit." + container.getName(),
                                    "Gives acess to the number of warps defined for '" + container.getName()
                                            + "' in the config", PermissionDefault.FALSE));
        }

        // mywarp.cooldown permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().warpCooldowns) {
            MyWarp.server()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.cooldown." + container.getName(),
                                    "User is affected by the cooldowns defined for '" + container.getName()
                                            + "' in the config", PermissionDefault.FALSE));
        }

        // mywarp.warmup permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().warpWarmups) {
            MyWarp.server()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.warmup." + container.getName(),
                                    "User is affected by the warmups defined for '" + container.getName()
                                            + "' in the config", PermissionDefault.FALSE));
        }

        // mywarp.warp.world permissions
        Map<String, Boolean> worldMap = new LinkedHashMap<String, Boolean>();
        for (World world : MyWarp.server().getWorlds()) {
            MyWarp.server()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.warp.world." + world.getName(),
                                    "User may warp to worlds in world '" + world.getName() + "'",
                                    PermissionDefault.OP));
            worldMap.put("mywarp.warp.world." + world.getName(), true);
        }
        worldMap.put("mywarp.warp.world.currentworld", true);

        MyWarp.server()
                .getPluginManager()
                .addPermission(
                        new org.bukkit.permissions.Permission("mywarp.warp.world.*",
                                "User may warp to all worlds", PermissionDefault.OP, worldMap));

        // mywarp.economy permissions
        for (ValuePermissionContainer container : MyWarp.inst().getWarpSettings().warpFees) {
            MyWarp.server()
                    .getPluginManager()
                    .addPermission(
                            new org.bukkit.permissions.Permission("mywarp.economy." + container.getName(),
                                    "User is affected by the fees defined for '" + container.getName()
                                            + "' in the config", PermissionDefault.FALSE));
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
            RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager()
                    .getRegistration(net.milkbowl.vault.permission.Permission.class);
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

    /**
     * Gets the cooldown ({@see Time}) affective for this player. Returns the
     * default-cooldown if the player does not have any of the of the specific
     * cooldowns
     * 
     * @param player
     *            the player
     * @return the cooldown affective from this player
     */
    public Time getCooldown(Player player) {
        for (Time cooldown : MyWarp.inst().getWarpSettings().warpCooldowns) {
            if (hasPermission(player, "mywarp.cooldown." + cooldown.getName())) {
                return cooldown;
            }
        }
        return MyWarp.inst().getWarpSettings().defaultCooldown;
    }

    /**
     * Gets the warmup ({@see Time}) affective for this player. Returns the
     * default-warmup if the player does not have any of the of the specific
     * warmups
     * 
     * @param player
     *            the player
     * @return the warmup affective for this player
     */
    public Time getWarmup(Player player) {
        for (Time warmup : MyWarp.inst().getWarpSettings().warpWarmups) {
            if (hasPermission(player, "mywarp.warmup." + warmup.getName())) {
                return warmup;
            }
        }
        return MyWarp.inst().getWarpSettings().defaultWarmup;
    }

    /**
     * Gets the private limit affective for this player. Returns the default
     * private-limit if the player does not have any of the specific limits
     * 
     * @param player
     *            the player
     * @return the private-limit affective for this player
     */
    public int maxPrivateWarps(Player player) {
        for (WarpLimit warpLimit : MyWarp.inst().getWarpSettings().warpLimits) {
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit.getMaxPrivate();
            }
        }
        return MyWarp.inst().getWarpSettings().defaultLimit.getMaxPrivate();
    }

    /**
     * Gets the public limit affective for this player. Returns the default
     * public-limit if the player does not have any of the specific limits
     * 
     * @param player
     *            the player
     * @return the public-limit affective for this player
     */
    public int maxPublicWarps(Player player) {
        for (WarpLimit warpLimit : MyWarp.inst().getWarpSettings().warpLimits) {
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit.getMaxPublic();
            }
        }
        return MyWarp.inst().getWarpSettings().defaultLimit.getMaxPublic();
    }

    /**
     * Gets the total limit affective for this player. Returns the default
     * total-limit if the player does not have any of the specific limits
     * 
     * @param player
     *            the player
     * @return the total-limit affective for this player
     */
    public int maxTotalWarps(Player player) {
        for (WarpLimit warpLimit : MyWarp.inst().getWarpSettings().warpLimits) {
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit.getMaxTotal();
            }
        }
        return MyWarp.inst().getWarpSettings().defaultLimit.getMaxTotal();
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
        for (WarpFees warpFees : MyWarp.inst().getWarpSettings().warpFees) {
            if (hasPermission(sender, "mywarp.economy." + warpFees.getName())) {
                return warpFees;
            }
        }
        return MyWarp.inst().getWarpSettings().defaultWarpFees;
    }
}
