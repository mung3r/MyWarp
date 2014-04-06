package me.taylorkelly.mywarp.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.safety.SafeTeleport;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Warp implements Comparable<Warp> {

    public static enum Type {
        PRIVATE("private", '-', ChatColor.RED), PUBLIC("public", '+', ChatColor.GREEN);

        private final String permissionSuffix;
        private final ChatColor color;
        private final char symbol;

        Type(String permissionSuffix, char symbol, ChatColor color) {
            this.permissionSuffix = permissionSuffix;
            this.symbol = symbol;
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }

        public char getSymbol() {
            // TODO symbol has no real use, so it might as well be removed...
            return symbol;
        }

        public String getPermissionSuffix() {
            return permissionSuffix;
        }
    }

    public static class PopularityComparator implements Comparator<Warp> {
        @Override
        public int compare(Warp w1, Warp w2) {
            return w1.getVisits() != w2.getVisits() ? (w1.getVisits() > w2.getVisits() ? -1 : 1) : w1
                    .getName().compareTo(w2.getName());
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

    private volatile int visits;
    private volatile String welcomeMessage;
    private final Set<UUID> invitedPlayerIds = new HashSet<UUID>();
    private final Set<String> invitedGroups = new HashSet<String>();

    public Warp(String name, UUID creatorId, Type type, double x, double y, double z, float yaw, float pitch,
            UUID worldId, int visits, String welcomeMessage, Collection<UUID> inivtedPlayerIds,
            Collection<String> invitedGroups) {
        this.name = name;
        this.creatorId = creatorId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.worldId = worldId;
        this.visits = visits;
        this.welcomeMessage = welcomeMessage;
        this.invitedPlayerIds.addAll(inivtedPlayerIds);
        this.invitedGroups.addAll(invitedGroups);
    }

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

        // XXX move out of constructor?
        this.visits = 0;
        this.welcomeMessage = MyWarp
                .inst()
                .getLocalizationManager()
                .getString("warp.default-welcome-message",
                        MyWarp.inst().getWarpSettings().localizationDefLocale);
    }

    public void asCompassTarget(Player player) {
        Location loc = createLocation();
        if (loc != null) {
            player.setCompassTarget(loc);
        }
    }

    public boolean isViewable(CommandSender sender) {
        if (sender instanceof Entity) {
            return isUsable((Entity) sender);
        }
        return MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.admin.accessall");
    }

    public boolean isUsable(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (MyWarp.inst().getWarpSettings().controlWorldAccess) {
                World loadedWorld = MyWarp.server().getWorld(worldId);
                if (loadedWorld != null
                        && !MyWarp.inst().getPermissionsManager()
                                .canAccessWorld((Player) player, MyWarp.server().getWorld(worldId))) {
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
            for (String group : invitedGroups) {
                if (MyWarp.inst().getPermissionsManager().playerHasGroup(player, group)) {
                    return true;
                }
            }
        }
        return type == Type.PUBLIC;
    }

    public boolean isModifiable(CommandSender sender) {
        if (MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.admin.modifyall")) {
            return true;
        }
        if (sender instanceof Player && isCreator((Player) sender)) {
            return true;
        }
        return false;
    }

    /*
     * TODO Remove crappy fees implementation, allow entitys to be teleported.
     */
    public void teleport(Player player, boolean charge) {
        Location loc = createLocation();

        if (loc == null) {
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("warp.world-non-existing", player, getWorld()));
            return;
        }

        switch (SafeTeleport.safeTeleport(player, loc)) {
        case NONE:
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("warp.unsafe-loc.no-teleport", player, getName()));
            break;
        case ORIGINAL_LOC:
            if (!welcomeMessage.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + getParsedWelcomeMessage(player));
            }
            incraseVisists();

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
            incraseVisists();

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

    public boolean isCreator(OfflinePlayer player) {
        return creatorId == player.getUniqueId();
    }

    public boolean isType(Type type) {
        return this.type == type;
    }

    public boolean isPlayerInvited(OfflinePlayer player) {
        return invitedPlayerIds.contains(player.getUniqueId());
    }

    public boolean isGroupInvited(String group) {
        return invitedGroups.contains(group);
    }

    public void inviteGroup(String group) {
        invitedGroups.add(group);

        MyWarp.inst().getConnectionManager().updateInvitedGroups(this);
    }

    public void invitePlayer(UUID playerId) {
        invitedPlayerIds.add(playerId);

        MyWarp.inst().getConnectionManager().updateInvitedPlayers(this);
    }

    public void uninviteGroup(String group) {
        invitedGroups.remove(group);

        MyWarp.inst().getConnectionManager().updateInvitedGroups(this);
    }

    public void uninvitePlayer(UUID playerId) {
        invitedPlayerIds.remove(playerId);

        MyWarp.inst().getConnectionManager().updateInvitedPlayers(this);
    }

    public String replaceWarpMacros(String str, @Nullable Player forWhom) {
        if (forWhom != null) {
            str = StringUtils.replace(str, "%player%", forWhom.getDisplayName());
        }

        if (str.contains("%creator%")) {
            String creatorName = MyWarp.server().getOfflinePlayer(creatorId).getName();
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

    public UUID getCreatorId() {
        return creatorId;
    }

    public OfflinePlayer getCreator() {
        return MyWarp.server().getOfflinePlayer(creatorId);
    }

    public Set<String> getInvitedGroups() {
        return Collections.unmodifiableSet(invitedGroups);
    }

    public Set<UUID> getInvitedPlayerIds() {
        return Collections.unmodifiableSet(invitedPlayerIds);
    }

    public String getName() {
        return name;
    }

    public float getPitch() {
        return pitch;
    }

    public Type getType() {
        return type;
    }

    public int getVisits() {
        return visits;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getParsedWelcomeMessage(Player forWhom) {
        return replaceWarpMacros(welcomeMessage, forWhom);
    }

    public UUID getWorldId() {
        return worldId;
    }

    @Nullable
    public World getWorld() {
        return MyWarp.server().getWorld(worldId);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public float getYaw() {
        return yaw;
    }

    public double getZ() {
        return z;
    }

    public void setCreatorId(UUID creator) {
        this.creatorId = creator;

        MyWarp.inst().getConnectionManager().updateCreator(this);
    }

    public void setLocation(Location loc) {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
        worldId = loc.getWorld().getUID();

        MyWarp.inst().getConnectionManager().updateInvitedPlayers(this);

        if (MyWarp.inst().getWarpSettings().dynmapEnabled && type == Type.PUBLIC) {
            MyWarp.inst().getMarkers().updateWarp(this);
        }
    }

    public void setType(Type type) {
        this.type = type;

        MyWarp.inst().getConnectionManager().updateType(this);
    }

    private void incraseVisists() {
        visits++;

        MyWarp.inst().getConnectionManager().updateVisits(this);
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;

        MyWarp.inst().getConnectionManager().updateWelcomeMessage(this);
    }

    @Override
    public String toString() {
        return "Warp [name=" + name + ", creatorId=" + creatorId + ", type=" + type + ", x=" + x + ", y=" + y
                + ", z=" + z + ", yaw=" + yaw + ", pitch=" + pitch + ", worldId=" + worldId + "]";
    }
}
