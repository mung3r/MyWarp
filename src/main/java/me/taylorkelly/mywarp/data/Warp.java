package me.taylorkelly.mywarp.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.FeeBundle;
import me.taylorkelly.mywarp.safety.TeleportManager;
import me.taylorkelly.mywarp.safety.TeleportManager.TeleportStatus;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.ComparisonChain;

/**
 * A warp.
 */
public class Warp implements Comparable<Warp> {

    private static final double GRAVITY_CONSTANT = 0.8;

    /**
     * The type of a warp.
     */
    public static enum Type {
        /***
         * A private warp.
         */
        PRIVATE(ChatColor.RED, LimitBundle.Limit.PRIVATE),
        /**
         * A public warp.
         */
        PUBLIC(ChatColor.GREEN, LimitBundle.Limit.PUBLIC);

        /**
         * This types color-representation used when displaying warp-names of
         * this type.
         */
        private final ChatColor color;
        /**
         * The corresponding {@link LimitBundle.limit}.
         */
        private final LimitBundle.Limit limit;

        /**
         * Initializes this type.
         * 
         * @param color
         *            the color representation
         * @param limitBundle
         *            the corresponding limit
         */
        private Type(ChatColor color, LimitBundle.Limit limit) {
            this.color = color;
            this.limit = limit;
        }

        /**
         * Gets this type's color-representation.
         * 
         * @return the color
         */
        public ChatColor getColor() {
            return color;
        }

        /**
         * Gets the limit that corresponds with this type.
         * 
         * @return the limit
         */
        public LimitBundle.Limit getLimit() {
            return limit;
        }
    }

    /**
     * A custom comparator that orders warps by there popularity: popular warps
     * come first, unpopular last.
     */
    public static class PopularityComparator implements Comparator<Warp> {

        @Override
        public int compare(Warp w1, Warp w2) {
            // Warps with a higher popularity score are preferred over warps
            // with lower score. If the score is equal, newer warps are
            // preferred over older warps. If both warps were created at the
            // same millisecond, the alphabetically first is preferred.
            return ComparisonChain.start().compare(w2.getPopularityScore(), w1.getPopularityScore())
                    .compare(w2.creationDate.getTime(), w1.creationDate.getTime()).compare(w1.name, w2.name)
                    .result();
        }
    }

    private final String name;

    private volatile UUID creatorId;
    private volatile Type type;

    private volatile double x;
    private volatile double y;
    private volatile double z;
    private volatile float yaw;
    private volatile float pitch;
    private volatile UUID worldId;

    private final Date creationDate;
    private volatile int visits;
    private volatile String welcomeMessage;
    private final Set<UUID> invitedPlayerIds = new HashSet<UUID>();
    private final Set<String> invitedGroups = new HashSet<String>();

    /**
     * Creates a new Warp with the given values.
     * 
     * @param name
     *            the name
     * @param creatorId
     *            the UUID of the player who created this warp
     * @param type
     *            the type
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @param z
     *            the z-coordinate
     * @param yaw
     *            the location's yaw
     * @param pitch
     *            the location's pitch
     * @param worldId
     *            the UUID of the location's world
     * @param creationDate
     *            the date this warp was created
     * @param visits
     *            the visit count
     * @param welcomeMessage
     *            the welcome message
     * @param inivtedPlayerIds
     *            a collection that contains all invited player UUIDs
     * @param invitedGroups
     *            a collection that includes all invited group names
     */
    public Warp(String name, UUID creatorId, Type type, double x, double y, double z, float yaw, float pitch,
            UUID worldId, Date creationDate, int visits, String welcomeMessage,
            Collection<UUID> inivtedPlayerIds, Collection<String> invitedGroups) {
        this.name = name;
        this.creatorId = creatorId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.worldId = worldId;
        this.creationDate = creationDate;
        this.visits = visits;
        this.welcomeMessage = welcomeMessage;
        this.invitedPlayerIds.addAll(inivtedPlayerIds);
        this.invitedGroups.addAll(invitedGroups);
    }

    /**
     * Creates a new warp of the given name, the given type and creator at the
     * creators current location.
     * 
     * @param name
     *            the name
     * @param creator
     *            the creator
     * @param type
     *            the type
     */
    protected Warp(String name, Player creator, Type type) {
        this.name = name;
        this.creatorId = creator.getUniqueId();
        this.type = type;

        Location loc = creator.getLocation();

        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        this.worldId = loc.getWorld().getUID();

        this.visits = 0;
        this.creationDate = new Date();
        this.welcomeMessage = MyWarp
                .inst()
                .getLocalizationManager()
                .getString("warp.default-welcome-message",
                        MyWarp.inst().getSettings().getLocalizationDefaultLocale());
    }

    /**
     * Sets this warp as the player's compass target.
     * 
     * @param player
     *            the player
     */
    public void asCompassTarget(Player player) {
        Location loc = createLocation();
        if (loc != null) {
            player.setCompassTarget(loc);
        }
    }

    /**
     * Returns whether this warp is viewable by the given command-sender.
     * 
     * @param sender
     *            the command-sender
     * @return true if the command-sender can view this warp
     */
    public boolean isViewable(CommandSender sender) {
        if (sender instanceof Entity) {
            return isUsable((Entity) sender);
        }
        return MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.admin.accessall");
    }

    /**
     * Returns whether this warp is usable by the given entity.
     * 
     * @param entity
     *            the entity
     * @return true if the given entity can use this warp
     */
    public boolean isUsable(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (MyWarp.inst().getSettings().isControlWorldAccess()) {
                World loadedWorld = getWorld();
                if (loadedWorld != null
                        && !MyWarp.inst().getPermissionsManager().canAccessWorld(player, loadedWorld)) {
                    return false;

                }
            }
            if (MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.admin.accessall")) {
                return true;
            }
            if (isCreator(player)) {
                return true;
            }
            if (invitedPlayerIds.contains(player.getUniqueId())) {
                return true;
            }
            for (String groupId : invitedGroups) {
                if (MyWarp.inst().getPermissionsManager().playerHasGroup(player, groupId)) {
                    return true;
                }
            }
        }
        return type == Type.PUBLIC;
    }

    /**
     * Returns whether this warp is modifiable by the given command-sender.
     * 
     * @param sender
     *            the command-sender
     * @return true if the given command-sender can modify this warp
     */
    public boolean isModifiable(CommandSender sender) {
        if (MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.admin.modifyall")) {
            return true;
        }
        if (sender instanceof Player && isCreator((Player) sender)) {
            return true;
        }
        return false;
    }

    /**
     * Teleports the given entity to this warp. If this warp's world is not
     * available, this method will log a warning and do nothing.
     * 
     * @param entity
     *            the entity
     * @return the status of teleport
     */
    public TeleportStatus teleport(Entity entity) {
        Location loc = createLocation();

        if (loc == null) {
            MyWarp.logger().warning(
                    "The warp '" + name + "' reffers to a nonexisting world (world-id: '" + worldId
                            + "') and cannot be used.");
            return TeleportStatus.NONE;
        }

        TeleportStatus status = TeleportManager.safeTeleport(entity, loc);

        switch (status) {
        case ORIGINAL_LOC:
        case SAFE_LOC:
            incraseVisists();
        case NONE:
            break;
        }
        return status;
    }

    /**
     * Teleports the given player to this warp and sends the applicable message.
     * 
     * @param player
     * @return the status of the teleport
     */
    public TeleportStatus teleport(Player player) {
        TeleportStatus status = teleport((Entity) player);

        switch (status) {
        case ORIGINAL_LOC:
            if (!welcomeMessage.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + getParsedWelcomeMessage(player));
            }
            break;
        case SAFE_LOC:
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("warp.unsafe-loc.closest-location", player, getName()));
            break;
        case NONE:
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("warp.unsafe-loc.no-teleport", player, getName()));
            break;
        }
        return status;
    }

    /**
     * Teleports the given player to this warp, sends the applicable message and
     * withdraws the applicable fee.
     * 
     * @param player
     *            the player
     * @param fee
     *            the fee that identifies the amount
     * @return the status of this teleport
     */
    public TeleportStatus teleport(Player player, FeeBundle.Fee fee) {
        TeleportStatus status = teleport(player);
        switch (status) {
        case NONE:
            break;
        case ORIGINAL_LOC:
        case SAFE_LOC:
            MyWarp.inst().getPermissionsManager().getFeeBundleManager().getBundle(player)
                    .withdraw(player, fee);
        }
        return status;
    }

    /**
     * Returns whether the given offline player is the creator of this warp.
     * 
     * @param player
     *            the player
     * @return true if the given offline player is the creator
     */
    public boolean isCreator(OfflinePlayer player) {
        return creatorId.equals(player.getUniqueId());
    }

    /**
     * Returns whether the warp has the same type as the given type.
     * 
     * @param type
     *            the type
     * @return true if the given type is the same as this warp's type
     */
    public boolean isType(Type type) {
        return this.type == type;
    }

    /**
     * Returns whether the given offline player is invited to this warp
     * 
     * @param player
     *            the player
     * @return true if the given offline player is invited to this warp
     */
    public boolean isPlayerInvited(OfflinePlayer player) {
        return invitedPlayerIds.contains(player.getUniqueId());
    }

    /**
     * Returns whether the permission-group identified by the given ID is
     * invited to this warp
     * 
     * @param groupId
     *            the ID of the group
     * @return true if group identified by the given ID is invited to this warp
     */
    public boolean isGroupInvited(String groupId) {
        return invitedGroups.contains(groupId);
    }

    /**
     * Invites the permission-group identified by the given ID to this warp.
     * This method will also attempt to update this warp via the active
     * data-connection.
     * 
     * @param groupId
     *            the ID of the group who should be invited
     */
    public void inviteGroup(String groupId) {
        invitedGroups.add(groupId);

        MyWarp.inst().getDataConnection().inviteGroup(this, groupId);
    }

    /**
     * Invites the player identified by the given ID to this warp. This method
     * will also attempt to update this warp via the active data-connection.
     * 
     * @param playerId
     *            the ID of the player who should be invited
     */
    public void invitePlayer(UUID playerId) {
        invitedPlayerIds.add(playerId);

        MyWarp.inst().getDataConnection().invitePlayer(this, playerId);
    }

    /**
     * Uninvites the permission-group identified by the given ID from this warp.
     * This method will also attempt to update this warp via the active
     * data-connection.
     * 
     * @param groupId
     *            the ID of the group who should be uninvited
     */
    public void uninviteGroup(String groupId) {
        invitedGroups.remove(groupId);

        MyWarp.inst().getDataConnection().uninviteGroup(this, groupId);
    }

    /**
     * Uninvites the player identified by the given ID from this warp. This
     * method will also attempt to update this warp via the active
     * data-connection.
     * 
     * @param playerId
     *            the ID of the player who should be uninvited
     */
    public void uninvitePlayer(UUID playerId) {
        invitedPlayerIds.remove(playerId);

        MyWarp.inst().getDataConnection().uninvitePlayer(this, playerId);
    }

    /**
     * Replaces all placeholder in the given string with the values applicable
     * for this warp and the given player.
     * 
     * @param str
     *            the string that contains the placeholder
     * @param forWhom
     *            the player for whom the placeholder should be replaced - can
     *            be null if there is none
     * @return the string with replaced placeholder
     */
    public String replacePlaceholders(String str, Player forWhom) {
        str = StringUtils.replace(str, "%player%", forWhom.getDisplayName());
        return replacePlaceholders(str);
    }

    /**
     * Replaces all placeholder in the given string with the values applicable
     * for this warp.
     * 
     * @param str
     *            the string that contains the placeholder
     * @return the string with replaced placeholder
     */
    public String replacePlaceholders(String str) {
        if (str.contains("%creator%")) {
            String creatorName = getCreator().getName();
            str = StringUtils.replace(str, "%creator%", creatorName);
        }

        str = StringUtils.replace(str, "%warp%", name);
        str = StringUtils.replace(str, "%visits%", Integer.toString(visits));

        str = StringUtils.replace(str, "%loc%",
                "(" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ")");
        World loadedWorld = getWorld();
        str = StringUtils.replace(str, "%world%", loadedWorld != null ? loadedWorld.getName() : "n/a");

        return str;
    }

    /**
     * Creates a {@link Location} from this warps coordinates.
     * 
     * @return the location of this warp. May return null if the stored world
     *         cannot be found on the server.
     */
    @Nullable
    private Location createLocation() {
        World loadedWorld = getWorld();
        if (loadedWorld != null) {
            return new Location(loadedWorld, x, y, z, yaw, pitch);
        }
        return null;
    }

    @Override
    public int compareTo(Warp that) {
        return this.name.compareTo(that.getName());
    }

    /**
     * Gets the ID of this warp's creator. Use {@link #getCreator()} to get the
     * actual player.
     * 
     * @return the ID of the creator of this warp
     */
    public UUID getCreatorId() {
        return creatorId;
    }

    /**
     * Gets this warp's creator.
     * 
     * @return the creator of this warp
     */
    public OfflinePlayer getCreator() {
        return MyWarp.server().getOfflinePlayer(creatorId);
    }

    /**
     * Gets an unmodifiable set containing the IDs of all permission-groups
     * invited to this warp.
     * 
     * @return a set with all IDs of groups invited to this warp
     */
    public Set<String> getInvitedGroups() {
        return Collections.unmodifiableSet(invitedGroups);
    }

    /**
     * Gets an unmodifiable set containing the IDs of all players invited to
     * this warp.
     * 
     * @return a set with all IDs of players who are invited to this warp
     */
    public Set<UUID> getInvitedPlayerIds() {
        return Collections.unmodifiableSet(invitedPlayerIds);
    }

    /**
     * Gets this warp's name
     * 
     * @return the name of this warp
     */
    public String getName() {
        return name;
    }

    /**
     * Gets this warp's pitch
     * 
     * @return the pitch of this warp
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Gets this warp's type
     * 
     * @return the type of this warp
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        // date is mutable, so we return a copy
        return new Date(creationDate.getTime());
    }

    /**
     * Gets this warp's visits number
     * 
     * @return the number of times this warp has been visited
     */
    public int getVisits() {
        return visits;
    }

    /**
     * Gets this warp's welcome message. Use
     * {@link #getParsedWelcomeMessage(Player)} to get the welcome message
     * without any variables.
     * 
     * @return the raw, unparsed welcome message of this warp
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * Gets this warp's welcome message with replaced variables. Use
     * {@link #getWelcomeMessage()} to get the raw welcome message.
     * 
     * @param forWhom
     *            the player for who this welcome message should be parsed.
     * @return the welcome message with parsed values.
     */
    public String getParsedWelcomeMessage(Player forWhom) {
        return replacePlaceholders(welcomeMessage, forWhom);
    }

    /**
     * Gets the ID of the world this warp is located on. Use {@link #getWorld()}
     * to get the world.
     * 
     * @return the ID of the world this warp is located on
     */
    public UUID getWorldId() {
        return worldId;
    }

    /**
     * Gets the loaded world where this warp is located.
     * 
     * @return the world of this warp. May return null if there is no world
     *         identified by the stored world-id.
     */
    @Nullable
    public World getWorld() {
        return MyWarp.server().getWorld(worldId);
    }

    /**
     * Gets this warp's X-coordinate.
     * 
     * @return the X-coordinate of this warp.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets this warp's Y-coordinate.
     * 
     * @return the Y-coordinate of this warp.
     */
    public double getY() {
        return y;
    }

    /**
     * Gets this warp's yaw.
     * 
     * @return the yaw of this warp.
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Gets this warp's Z-coordinate.
     * 
     * @return the Z-coordinate of this warp.
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the creator of this warp to the one identified by the given ID. This
     * method will also attempt to update this warp via the active
     * data-connection.
     * 
     * @param creatorId
     *            the ID of the new creator
     */
    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;

        MyWarp.inst().getDataConnection().updateCreator(this);

        if (MyWarp.inst().isMarkerSetup()) {
            MyWarp.inst().getMarkers().updateMarker(this);
        }
    }

    /**
     * Sets the location of this warp to the given location. This method will
     * also attempt to update this warp via the active data-connection.
     * 
     * @param loc
     *            the new location
     */
    public void setLocation(Location loc) {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
        worldId = loc.getWorld().getUID();

        MyWarp.inst().getDataConnection().updateLocation(this);

        if (MyWarp.inst().isMarkerSetup()) {
            MyWarp.inst().getMarkers().updateMarker(this);
        }
    }

    /**
     * Sets the type of this warp to the given one. This method will also
     * attempt to update this warp via the active data-connection.
     * 
     * @param type
     *            the new type
     */
    public void setType(Type type) {
        this.type = type;

        MyWarp.inst().getMarkers().updateMarker(this);

        if (MyWarp.inst().isMarkerSetup()) {
            MyWarp.inst().getMarkers().handleTypeChange(this);
        }
    }

    /**
     * Increments the visit-number of this warp by one. This method will also
     * attempt to update this warp via the active data-connection.
     */
    private void incraseVisists() {
        visits++;

        MyWarp.inst().getDataConnection().updateVisits(this);

        if (MyWarp.inst().isMarkerSetup()) {
            MyWarp.inst().getMarkers().updateMarker(this);
        }
    }

    /**
     * Sets the welcome-message of this warp to the given one. This method will
     * also attempt to update this warp via the active data-connection.
     * 
     * @param welcomeMessage
     */
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;

        MyWarp.inst().getDataConnection().updateWelcomeMessage(this);
    }

    /**
     * Gets this warp's popularity score. The score is influenced by the number
     * of visits a warp received since it was created, while newer warps receive
     * a better score than older warps.
     * 
     * @return the popularity score of this warp
     */
    private double getPopularityScore() {
        // a basic implementation of the hacker news ranking algorithm detailed
        // at http://amix.dk/blog/post/19574: Older warps receive lower scores
        // due to the influence of the gravity constant.
        double daysExisting = (System.currentTimeMillis() - creationDate.getTime()) / 86400000.0;
        return visits / Math.pow(daysExisting, GRAVITY_CONSTANT);
    }

    /**
     * Gets the average visits number per day, from the point this warp was
     * created until now.
     * 
     * @return the average number of visits per day
     */
    public double getVisitsPerDay() {
        // this method might not be 100% exact (considering leap seconds), but
        // within the current Java API there are no alternatives
        long daysSinceCreation = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()
                - creationDate.getTime());
        if (daysSinceCreation <= 0) {
            return visits;
        }
        return visits / daysSinceCreation;
    }

    @Override
    public String toString() {
        return "Warp [name=" + name + ", creatorId=" + creatorId + ", type=" + type + ", x=" + x + ", y=" + y
                + ", z=" + z + ", yaw=" + yaw + ", pitch=" + pitch + ", worldId=" + worldId + "]";
    }
}
