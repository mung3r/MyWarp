package me.taylorkelly.mywarp.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.safety.SafeTeleport;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class represents a usable warp. Note that not all values are changeable
 * or even accessible.
 */
public class Warp implements Comparable<Warp> {

    private static final char LIST_SEPERATOR = ',';

    private final int index;
    private final String name;
    private String creator;
    private boolean publicAll;
    private String world;
    private double x;
    private int y;
    private double z;
    private int yaw;
    private int pitch;
    private int visits;
    private String welcomeMessage;

    private final transient ArrayList<String> permissions;
    private final transient ArrayList<String> groupPermissions;

    private static int nextIndex = 1;

    /**
     * Creates a new warp using all provided values
     * 
     * @param index
     *            the internal index
     * @param name
     *            the name
     * @param creator
     *            the creator's name
     * @param world
     *            the world's name
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @param z
     *            the z-coordinate
     * @param yaw
     *            the yaw
     * @param pitch
     *            the pitch
     * @param publicAll
     *            true if the warp should be public, false if it should be
     *            private
     * @param permissions
     *            a list of invited players, joined by a predefined character
     * @param groupPermissions
     *            a list of invited groups, joined by a predefined character
     * @param welcomeMessage
     *            the welcome message
     * @param visits
     *            the visits
     */
    public Warp(int index, String name, String creator, String world, double x, int y, double z, int yaw,
            int pitch, boolean publicAll, String permissions, String groupPermissions, String welcomeMessage,
            int visits) {
        this.index = index;
        this.name = name;
        this.creator = creator;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.publicAll = publicAll;
        this.permissions = processList(permissions);
        this.groupPermissions = processList(groupPermissions);
        this.welcomeMessage = welcomeMessage;
        this.visits = visits;
        if (index > nextIndex) {
            nextIndex = index;
        }
        nextIndex++;
    }

    /**
     * Creates a new public warp at the position of the given player
     * 
     * @param name
     *            the name of this warp
     * @param creator
     *            the player at whose position the warp will be created
     */
    public Warp(String name, Player creator) {
        this(name, creator, true);
    }

    /**
     * Creates a new warp at the position of the given player
     * 
     * @param name
     *            the name of this warp
     * @param creator
     *            the player at whose position the warp will be created
     * @param publicAll
     *            true if the warp should be public, false if it should be
     *            private
     */
    public Warp(String name, Player creator, boolean publicAll) {
        this.index = nextIndex;
        nextIndex++;
        this.name = name;
        this.creator = creator.getName();
        this.world = creator.getWorld().getName();
        this.x = creator.getLocation().getX();
        this.y = creator.getLocation().getBlockY();
        this.z = creator.getLocation().getZ();
        this.yaw = Math.round(creator.getLocation().getYaw()) % 360;
        this.pitch = Math.round(creator.getLocation().getPitch()) % 360;
        this.publicAll = publicAll;
        this.permissions = new ArrayList<String>();
        this.groupPermissions = new ArrayList<String>();
        this.welcomeMessage = MyWarp
                .inst()
                .getLocalizationManager()
                .getString("warp.default-welcome-message",
                        MyWarp.inst().getWarpSettings().localizationDefLocale);
        this.visits = 0;
    }

    @Override
    public int compareTo(Warp warp) {
        return this.getName().compareTo(warp.getName());
    }

    /**
     * Gets the creator's name
     * 
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Gets the internal index of this warp
     * 
     * @return the warp's index number
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the location of this warp. This actually constructs a new
     * {@link Location} - may return null if the world of this warp does not
     * exist anymore.
     * 
     * @return the warp's location
     */
    public Location getLocation() {
        World currWorld = null;
        // fallback
        if (getWorld().equals("0")) {
            currWorld = MyWarp.server().getWorlds().get(0);
        } else {
            currWorld = MyWarp.server().getWorld(getWorld());
        }
        if (currWorld == null) {
            return null;
        } else {
            Location location = new Location(currWorld, getX(), getY(), getZ(), getYaw(), getPitch());
            return location;
        }
    }

    /**
     * Gets the name of the warp
     * 
     * @return the warp's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the welcome message of this warp. This method will not attempt to
     * replace any values found.
     * 
     * @return the warp's welcome message
     */
    public String getRawWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * Gets the welcome message of this warp in its specific form for the given
     * player. Unlike {@link #getRawWelcomeMessage()}, all values will be
     * replaced.
     * 
     * @param player
     *            the player
     * @return the warp's welcome message with replaced values
     */
    public String getSpecificWelcomeMessage(Player player) {
        return StringUtils.replace(replaceWarpMacros(welcomeMessage), "%player%", player.getName());
    }

    /**
     * Gets the visit number of this warp
     * 
     * @return the warp's visits
     */
    public int getVisits() {
        return visits;
    }

    /**
     * Gets the name of the world this warp is located in
     * 
     * @return the world name
     */
    public String getWorld() {
        return world;
    }

    /**
     * Gets the x-coordinate of this warp
     * 
     * @return the warp's x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this warp
     * 
     * @return the warp's y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the z-coordinate of this warp
     * 
     * @return the warp's z-coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the yaw of this warp
     * 
     * @return the warp's yaw
     */
    public int getYaw() {
        return yaw;
    }

    /**
     * Gets the pitch of this warp
     * 
     * @return the warp's pitch
     */
    public int getPitch() {
        return pitch;
    }

    public String permissionsString() {
        return StringUtils.join(permissions, LIST_SEPERATOR);
    }

    /**
     * Gets all invited groups, joined by a predefined character
     * 
     * @return all groups invited
     */
    public String groupPermissionsString() {
        return StringUtils.join(groupPermissions, LIST_SEPERATOR);
    }

    /**
     * Invites the player of the given name to the warp
     * 
     * @param group
     *            the player's name
     */
    public void invite(String player) {
        permissions.add(player);
        MyWarp.inst().getConnectionManager().updatePermissions(this);
    }

    /**
     * Invites the group of the given name to the warp
     * 
     * @param group
     *            the group's name
     */
    public void inviteGroup(String group) {
        groupPermissions.add(group);
        MyWarp.inst().getConnectionManager().updateGroupPermissions(this);
    }

    /**
     * Checks if this warp is public
     * 
     * @return true if the warp is public, false if it is private
     */
    public boolean isPublicAll() {
        return publicAll;
    }

    /**
     * Checks if the given command-sender can modify this warp
     * 
     * @param sender
     *            the command-sender
     * @return true if the command-sender can modify this warp, false if not
     */
    public boolean isModifiable(CommandSender sender) {
        if (MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.admin.modifyall")) {
            return true;
        }
        if (isCreator(sender)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given command-sender could use this warp. 'Usage' should be
     * understood hypothetically: If the sender would be an entity, could he
     * access this warp?
     * 
     * @param sender
     *            the command-sender
     * @return true if the command-sender can use this warp, false if not
     */
    public boolean isUsable(CommandSender sender) {
        if (sender instanceof Player && MyWarp.inst().getWarpSettings().controlWorldAccess
                && !MyWarp.inst().getPermissionsManager().playerCanAccessWorld((Player) sender, world)) {
            return false;
        }
        if (MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.admin.accessall")) {
            return true;
        }
        if (isCreator(sender)) {
            return true;
        }
        if (isInvited(sender)) {
            return true;
        }
        if (isGroupInvited(sender)) {
            return true;
        }
        return publicAll;
    }

    /**
     * Checks if the given command-sender is invited to the warp
     * 
     * @param sender
     *            the command-sender
     * @return true if the command-sender is this warp's creator, false if not
     */
    public boolean isCreator(CommandSender sender) {
        return sender instanceof Player && isCreator(sender.getName());
    }

    /**
     * Checks if the given player-name is the creator of this warp
     * 
     * @param name
     *            the player's name
     * @return true if the player is this warp's creator, false if not
     */
    public boolean isCreator(String name) {
        return creator.equals(name);
    }

    /**
     * Checks if the group of the given name is invited to this warp.
     * 
     * @return true if invited, false if not.
     */
    public boolean isGroupInvited(String group) {
        return groupPermissions.contains(group);
    }

    /**
     * Checks if the given command-sender is invited due to one of his
     * permission-groups.
     * 
     * @return true if invited, false if not.
     */
    public boolean isGroupInvited(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return false;
        }
        // can have multiple groups so we need to check this way
        for (String group : groupPermissions) {
            if (MyWarp.inst().getPermissionsManager().playerHasGroup((Player) sender, group)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given player is invited to this warp
     * 
     * @param player
     *            the player's name
     * @return true if the player is invited, false if not
     */
    public boolean isInvited(CommandSender sender) {
        return sender instanceof Player && isInvited(sender.getName());
    }

    /**
     * Checks if the given player is invited to this warp
     * 
     * @param player
     *            the player's name
     * @return true if the player is invited, false if not
     */
    public boolean isInvited(String name) {
        return permissions.contains(name);
    }

    /**
     * Converts a given string into an array list by splitting it around a
     * predefined character.
     * 
     * @param string
     *            the string
     * @return an array list with the splitted string
     */
    private ArrayList<String> processList(String string) {
        return new ArrayList<String>(Arrays.asList(StringUtils.split(string, LIST_SEPERATOR)));
    }

    /**
     * Sets the creator of this warp to the given one
     * 
     * @param giveeName
     *            the new creator of this warp
     */
    public void setCreator(String giveeName) {
        this.creator = giveeName;

        MyWarp.inst().getConnectionManager().updateCreator(this);
        if (MyWarp.inst().getWarpSettings().dynmapEnabled) {
            MyWarp.inst().getMarkers().updateWarp(this);
        }
    }

    /**
     * sets the location of this warp to the given one
     * 
     * @param location
     *            the new location
     */
    public void setLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getBlockY();
        this.z = location.getZ();
        this.yaw = Math.round(location.getYaw()) % 360;
        this.pitch = Math.round(location.getPitch()) % 360;

        MyWarp.inst().getConnectionManager().updateLocation(this);

        if (MyWarp.inst().getWarpSettings().dynmapEnabled) {
            MyWarp.inst().getMarkers().updateWarp(this);
        }
    }

    /**
     * Changes this warps visibility (public, private)
     * 
     * @param publicAll
     *            true for public, false for private
     */
    public void setPublicAll(boolean publicAll) {
        this.publicAll = publicAll;

        MyWarp.inst().getConnectionManager().publicizeWarp(this, publicAll);

        if (MyWarp.inst().getWarpSettings().dynmapEnabled) {
            if (publicAll) {
                MyWarp.inst().getMarkers().addWarp(this);
            } else {
                MyWarp.inst().getMarkers().deleteWarp(this);
            }
        }
    }

    /**
     * Sets the welcome message of this warp
     * 
     * @param welcomeMessage
     *            the new welcome message
     */
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        MyWarp.inst().getConnectionManager().updateWelcomeMessage(this);
    }

    @Override
    public String toString() {
        return "Warp{" + "name=" + name + " creator=" + creator + "publicAll=" + publicAll + "}";
    }

    /**
     * Uninvites the player of the given name from this warp
     * 
     * @param inviteeName
     *            the player's name
     */
    public void uninvite(String inviteeName) {
        permissions.remove(inviteeName);
        MyWarp.inst().getConnectionManager().updatePermissions(this);
    }

    /**
     * Uninvites the group of the given name from this warp
     * 
     * @param group
     *            the group's name
     */
    public void uninviteGroup(String group) {
        groupPermissions.remove(group);
        MyWarp.inst().getConnectionManager().updateGroupPermissions(this);
    }

    /**
     * Counts up the visits-counter by one
     */
    private void visit() {
        visits++;
        MyWarp.inst().getConnectionManager().updateVisits(this);
    }

    /**
     * Attempts to teleport the given player to this warp. Will send an error
     * message to the player, if the warp's world does not exist. The teleport
     * itself is handled via
     * {@link me.taylorkelly.mywarp.safety.SafeTeleport#safeTeleport(Player, Location, String)}
     * 
     * TODO Remove crappy implementation for warp-fees!
     * 
     * @param player
     *            the player
     * @param charge
     *            whether the player should be charged with the corresponding
     *            warp-fee
     */
    public void warp(Player player, boolean charge) {
        Location location = getLocation();
        if (location == null) {
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("warp.world-non-existing", player, getWorld()));
        } else {

            switch (SafeTeleport.safeTeleport(player, location)) {
            case NONE:
                player.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("warp.unsafe-loc.no-teleport", player, getName()));
                break;
            case ORIGINAL_LOC:
                if (!welcomeMessage.isEmpty()) {
                    player.sendMessage(ChatColor.AQUA + getSpecificWelcomeMessage(player));
                }
                visit();

                if (MyWarp.inst().getWarpSettings().economyEnabled && charge) {
                    MyWarp.inst()
                            .getEconomyLink()
                            .withdrawSender(
                                    player,
                                    MyWarp.inst().getPermissionsManager().getEconomyPrices(player)
                                            .getFee(Fee.WARP_TO));
                }
                break;
            case SAFE_LOC:
                player.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("warp.unsafe-loc.closest-location", player, getName()));
                visit();

                if (MyWarp.inst().getWarpSettings().economyEnabled && charge) {
                    MyWarp.inst()
                            .getEconomyLink()
                            .withdrawSender(
                                    player,
                                    MyWarp.inst().getPermissionsManager().getEconomyPrices(player)
                                            .getFee(Fee.WARP_TO));
                }
                break;
            }
        }
    }

    /**
     * Gets a list with all player names who are invited to this warp
     * 
     * @return a List with all invited players
     */
    public List<String> getAllInvitedPlayers() {
        return permissions;
    }

    /**
     * Gets a list with all group names that are invited to this warp
     * 
     * @return a list with all invited groups
     */
    public List<String> getAllInvitedGroups() {
        return groupPermissions;
    }

    /**
     * Replaces all macros (e.g. %creator%, %name%) in the given string with the
     * corresponding value of this warp.
     * 
     * @param str
     *            the original string
     * @return the String with replaced macros
     */
    public String replaceWarpMacros(String str) {
        str = StringUtils.replace(str, "%creator%", creator);
        str = StringUtils.replace(str, "%warp%", name);
        str = StringUtils.replace(str, "%visits%", Integer.toString(visits));
        str = StringUtils.replace(str, "%world%", world);
        str = StringUtils.replace(str, "%loc%", "(" + Math.round(x) + ", " + y + ", " + Math.round(z) + ")");

        return str;
    }
}
