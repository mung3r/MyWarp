package me.taylorkelly.mywarp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.utils.MatchList;
import me.taylorkelly.mywarp.utils.WarpLogger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class WarpList {
    private Server server;
    private HashMap<String, Warp> warpMap;
    private HashMap<String, Warp> welcomeMessage;

    public WarpList(Server server) {
        this.server = server;

        welcomeMessage = new HashMap<String, Warp>();
        warpMap = MyWarp.connectionManager.getMap();
        WarpLogger.info(getSize() + " warps loaded");
    }

    public void addWarp(String name, Warp warp) {
        warpMap.put(name, warp);
        MyWarp.connectionManager.addWarp(warp);

        if (MyWarp.markers != null) {
            MyWarp.markers.addWarp(warp);
        }
    }

    public void addWarpPrivate(String name, Player player) {
        Warp warp = new Warp(name, player, false);
        addWarp(name, warp);
    }

    public void addWarpPublic(String name, Player player) {
        Warp warp = new Warp(name, player);
        addWarp(name, warp);
    }

    public void deleteWarp(Warp warp) {
        warpMap.remove(warp.name);
        MyWarp.connectionManager.deleteWarp(warp);

        if (MyWarp.markers != null) {
            MyWarp.markers.deleteWarp(warp);
        }
    }

    public MatchList getMatches(String name, Player player,
            Comparator<Warp> comperator) {
        TreeSet<Warp> exactMatches = new TreeSet<Warp>(comperator);
        TreeSet<Warp> matches = new TreeSet<Warp>(comperator);

        for (Warp warp : warpMap.values()) {
            if (player != null && !warp.playerCanWarp(player)) {
                continue;
            }
            if (WarpSettings.worldAccess && player != null
                    && !playerCanAccessWorld(player, warp.world)) {
                continue;
            }
            if (warp.name.equalsIgnoreCase(name)) {
                exactMatches.add(warp);
            } else if (warp.name.toLowerCase().contains(name.toLowerCase())) {
                matches.add(warp);
            }
        }
        if (exactMatches.size() > 1) {
            Iterator<Warp> iterator = exactMatches.iterator();
            while (iterator.hasNext()) {
                Warp warp = iterator.next();
                if (!warp.name.equals(name)) {
                    matches.add(warp);
                    iterator.remove();
                }
            }
        }
        return new MatchList(exactMatches, matches);
    }

    public String getMatchingCreator(Player player, String creator) {
        ArrayList<String> matches = new ArrayList<String>();

        for (Warp warp : warpMap.values()) {
            if (player != null && !warp.playerCanWarp(player)) {
                continue;
            }
            if (WarpSettings.worldAccess && player != null
                    && !playerCanAccessWorld(player, warp.world)) {
                continue;
            }
            if (warp.creator.equalsIgnoreCase(creator)) {
                return creator;
            }
            if (warp.creator.toLowerCase().contains(creator.toLowerCase())
                    && !matches.contains(warp.creator)) {
                matches.add(warp.creator);
            }
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return "";
    }

    public double getMaxWarps(Player player, String creator) {
        int count = 0;
        for (Warp warp : warpMap.values()) {
            if ((player != null && !warp.playerCanWarp(player))
                    || (creator != null && !warp.playerIsCreator(creator))) {
                continue;
            }
            if (WarpSettings.worldAccess && player != null
                    && !playerCanAccessWorld(player, warp.world)) {
                continue;
            }
            count++;
        }
        return count;
    }

    public TreeSet<Warp> getPublicWarps() {
        TreeSet<Warp> ret = new TreeSet<Warp>();

        for (Warp warp : warpMap.values()) {
            if (warp.publicAll) {
                ret.add(warp);
            }
        }
        return ret;
    }

    public int getSize() {
        return warpMap.size();
    }

    public TreeSet<Warp> getSortedWarps(Player player, String creator,
            int start, int size) {
        TreeSet<Warp> ret = new TreeSet<Warp>();
        List<String> sortedNames = new ArrayList<String>(warpMap.keySet());
        Collections.sort(sortedNames, String.CASE_INSENSITIVE_ORDER);

        int currentCount = 0;
        for (String name : sortedNames) {
            if (ret.size() == size) {
                return ret;
            }
            Warp warp = warpMap.get(name);

            if ((player != null && !warp.playerCanWarp(player))
                    || (creator != null && !warp.playerIsCreator(creator))) {
                continue;
            }
            if (WarpSettings.worldAccess && player != null
                    && !playerCanAccessWorld(player, warp.world)) {
                continue;
            }
            if (currentCount >= start) {
                ret.add(warp);
            } else {
                currentCount++;
            }
        }
        return ret;
    }

    public Warp getWarp(String name) {
        return warpMap.get(name);
    }

    public void give(Warp warp, String giveeName) {
        warp.setCreator(giveeName);
        MyWarp.connectionManager.updateCreator(warp);

        if (MyWarp.markers != null) {
            MyWarp.markers.updateWarp(warp);
        }
    }

    public void inviteGroup(Warp warp, String inviteeName) {
        warp.inviteGroup(inviteeName);
        MyWarp.connectionManager.updateGroupPermissions(warp);
    }

    public void invitePlayer(Warp warp, String inviteeName) {
        warp.invite(inviteeName);
        MyWarp.connectionManager.updatePermissions(warp);
    }

    public void notWaiting(Player player) {
        welcomeMessage.remove(player.getName());
    }

    private int numPrivateWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpMap.values()) {
            boolean privateAll = !warp.publicAll;
            String creator = warp.creator;
            if (creator.equals(player.getName()) && privateAll)
                size++;
        }
        return size;
    }

    private int numPublicWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpMap.values()) {
            boolean publicAll = warp.publicAll;
            String creator = warp.creator;
            if (creator.equals(player.getName()) && publicAll)
                size++;
        }
        return size;
    }

    private int numWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpMap.values()) {
            String creator = warp.creator;
            if (creator.equals(player.getName()))
                size++;
        }
        return size;
    }

    public boolean playerCanAccessWorld(Player player, String worldName) {
        if (player.getWorld().getName().equals(worldName)
                && MyWarp.getWarpPermissions().canWarpInsideWorld(player)) {
            return true;
        }
        if (MyWarp.getWarpPermissions().canWarpToWorld(player, worldName)) {
            return true;
        }
        return false;
    }

    public boolean playerCanBuildPrivateWarp(Player player) {
        if (MyWarp.getWarpPermissions().disobeyPrivateLimit(player)) {
            return true;
        }
        return numPrivateWarpsPlayer(player) < MyWarp.getWarpPermissions()
                .maxPrivateWarps(player);
    }

    public boolean playerCanBuildPublicWarp(Player player) {
        if (MyWarp.getWarpPermissions().disobeyPublicLimit(player)) {
            return true;
        }
        return numPublicWarpsPlayer(player) < MyWarp.getWarpPermissions()
                .maxPublicWarps(player);
    }

    public boolean playerCanBuildWarp(Player player) {
        if (MyWarp.getWarpPermissions().disobeyTotalLimit(player)) {
            return true;
        }
        return numWarpsPlayer(player) < MyWarp.getWarpPermissions()
                .maxTotalWarps(player);
    }

    public void point(Warp warp, Player player) {
        player.setCompassTarget(warp.getLocation(server));
    }

    public void privatize(Warp warp) {
        warp.publicAll = false;
        MyWarp.connectionManager.publicizeWarp(warp, false);

        if (MyWarp.markers != null) {
            MyWarp.markers.deleteWarp(warp);
        }
    }

    public void publicize(Warp warp) {
        warp.publicAll = true;
        MyWarp.connectionManager.publicizeWarp(warp, true);

        if (MyWarp.markers != null) {
            MyWarp.markers.addWarp(warp);
        }
    }

    public void setWelcomeMessage(Player player, String message) {
        if (welcomeMessage.containsKey(player.getName())) {
            Warp warp = welcomeMessage.get(player.getName());
            warp.welcomeMessage = message;
            MyWarp.connectionManager.updateWelcomeMessage(warp);
            player.sendMessage(LanguageManager
                    .getString("warp.welcome.received"));
            player.sendMessage(message);
        }
    }

    public void uninviteGroup(Warp warp, String inviteeName) {
        warp.uninviteGroup(inviteeName);
        MyWarp.connectionManager.updateGroupPermissions(warp);
    }

    public void uninvitePlayer(Warp warp, String inviteeName) {
        warp.uninvite(inviteeName);
        MyWarp.connectionManager.updatePermissions(warp);
    }

    public void updateLocation(Warp warp, Player player) {
        warp.setLocation(player.getLocation());
        MyWarp.connectionManager.updateLocation(warp);

        if (MyWarp.markers != null) {
            MyWarp.markers.updateWarp(warp);
        }
    }

    public boolean waitingForWelcome(Player player) {
        return welcomeMessage.containsKey(player.getName());
    }

    public boolean warpExists(String warp) {
        return warpMap.containsKey(warp);
    }

    public TreeSet<Warp> warpsInvitedTo(Player player) {
        TreeSet<Warp> results = new TreeSet<Warp>();

        for (Warp warp : warpMap.values()) {
            if (player != null && !warp.playerCanWarp(player)) {
                continue;
            }
            if (WarpSettings.worldAccess && player != null
                    && !playerCanAccessWorld(player, warp.world)) {
                continue;
            }
            results.add(warp);
        }
        return results;
    }

    public void warpTo(Warp warp, Player player) {
        if (warp.warp(player, server)) {
            warp.visits++;
            MyWarp.connectionManager.updateVisits(warp);
            player.sendMessage(ChatColor.AQUA
                    + warp.getSpecificWelcomeMessage(player));
        }
    }

    public void welcomeMessage(Warp warp, Player player) {
        welcomeMessage.put(player.getName(), warp);
    }
}
