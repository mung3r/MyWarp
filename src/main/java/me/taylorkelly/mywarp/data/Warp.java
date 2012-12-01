package me.taylorkelly.mywarp.data;

import java.util.ArrayList;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.safety.SafeTeleport;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Warp {

    public int index;
    public String name;
    public String creator;
    public String world;
    public double x;
    public int y;
    public double z;
    public int yaw;
    public int pitch;
    public int visits;
    public boolean publicAll;
    public String welcomeMessage;
    public ArrayList<String> permissions;
    public ArrayList<String> groupPermissions;
    public static int nextIndex = 1;

    public Warp(int index, String name, String creator, String world, double x, int y,
            double z, int yaw, int pitch, boolean publicAll, String permissions,
            String groupPermissions, String welcomeMessage, int visits) {
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

    public Warp(String name, Player creator) {
        this(name, creator, true);
    }

    public Warp(String name, Location location) {
        this.index = nextIndex;
        nextIndex++;
        this.name = name;
        this.creator = "No Player";
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getBlockY();
        this.z = location.getZ();
        this.yaw = Math.round(location.getYaw()) % 360;
        this.pitch = Math.round(location.getPitch()) % 360;
        this.publicAll = true;
        this.permissions = new ArrayList<String>();
        this.groupPermissions = new ArrayList<String>();
        this.welcomeMessage = LanguageManager.getString("warp.default.welcomeMessage");
        this.visits = 0;
    }

    public Warp(String name, Player creator, boolean b) {
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
        this.publicAll = b;
        this.permissions = new ArrayList<String>();
        this.groupPermissions = new ArrayList<String>();
        this.welcomeMessage = LanguageManager.getString("warp.default.welcomeMessage");
        this.visits = 0;
    }

    private ArrayList<String> processList(String permissions) {
        String[] names = permissions.split(",");
        ArrayList<String> ret = new ArrayList<String>();
        for (String name : names) {
            if (name.equals("")) {
                continue;
            }
            ret.add(name.trim());
        }
        return ret;
    }

    public String permissionsString() {
        StringBuilder ret = new StringBuilder();
        for (String name : permissions) {
            ret.append(name);
            ret.append(",");
        }
        return ret.toString();
    }

    public String groupPermissionsString() {
        StringBuilder ret = new StringBuilder();
        for (String name : groupPermissions) {
            ret.append(name);
            ret.append(",");
        }
        return ret.toString();
    }

    public boolean playerCanWarp(Player player) {
        if (creator.equals(player.getName())) {
            return true;
        }
        if (permissions.contains(player.getName())) {
            return true;
        }

        for (String group : groupPermissions) {
            if (MyWarp.getWarpPermissions().playerHasGroup(player, group)) {
                return true;
            }
        }
        if (MyWarp.getWarpPermissions().canAccessAll(player)) {
            return true;
        }

        return publicAll;
    }

    public boolean warp(Player player, Server server) {
        World currWorld = null;
        if (world.equals("0")) {
            currWorld = server.getWorlds().get(0);
        } else {
            currWorld = server.getWorld(world);
        }
        if (currWorld != null) {
            Location location = new Location(currWorld, x, y, z, yaw, pitch);
            return SafeTeleport.safeTeleport(player, location, name);
        } else {
            player.sendMessage(LanguageManager.getString("error.warpto.noSuchWorld").replaceAll("%world%", world));
            return false;
        }
    }

    public boolean playerIsCreator(String name) {
        if (creator.equals(name)) {
            return true;
        }
        return false;
    }

    public void inviteGroup(String group) {
        groupPermissions.add(group);
    }

    public boolean groupIsInvited(String group) {
        return groupPermissions.contains(group);
    }

    public void invite(String player) {
        permissions.add(player);
    }

    public void uninviteGroup(String group) {
        groupPermissions.remove(group);
    }

    public boolean playerIsInvited(String player) {
        return permissions.contains(player);
    }

    public void uninvite(String inviteeName) {
        permissions.remove(inviteeName);
    }

    public boolean playerCanModify(Player player) {
        if (creator.equals(player.getName())) {
            return true;
        }
        if (MyWarp.getWarpPermissions().canModifyAll(player)) {
            return true;
        }
        return false;
    }

    public void setCreator(String giveeName) {
        this.creator = giveeName;
    }

    public String toString() {
        return name;
    }

    public void setLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getBlockY();
        this.z = location.getZ();
        this.yaw = Math.round(location.getYaw()) % 360;
        this.pitch = Math.round(location.getPitch()) % 360;
    }

    public Location getLocation(Server server) {
        World currWorld = null;
        if (world.equals("0")) {
            currWorld = server.getWorlds().get(0);
        } else {
            currWorld = server.getWorld(world);
        }
        if (currWorld == null) {
            return null;
        } else {
            Location location = new Location(currWorld, x, y, z, yaw, pitch);
            return location;
        }
    }

    public String getSpecificWelcomeMessage(Player player) {
        return welcomeMessage.replaceAll("%player%", player.getName())
                .replaceAll("%warp%", name).replaceAll("%creator%", creator)
                .replaceAll("%visits%", Integer.toString(visits));
    }
}
