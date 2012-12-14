package me.taylorkelly.mywarp.data;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.utils.WarpLogger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class WarpList {
    private Server server;
    private HashMap<String, Warp> warpList;
    private HashMap<String, Warp> welcomeMessage;

    public WarpList(Server server) {
        welcomeMessage = new HashMap<String, Warp>();
        this.server = server;
        warpList = MyWarp.connectionManager.getMap();
        WarpLogger.info(getSize() + " warps loaded");
    }

    public void addWarp(String name, Warp warp) {
        warpList.put(name, warp);
        MyWarp.connectionManager.addWarp(warp);
    }

    public void addWarpPrivate(String name, Player player) {
        Warp warp = new Warp(name, player, false);
        addWarp(name, warp);
    }

    public void addWarpPublic(String name, Player player) {
        Warp warp = new Warp(name, player);
        addWarp(name, warp);
    }

    public void blindAdd(Warp warp) {
        warpList.put(warp.name, warp);
    }

    public void deleteWarp(String name) {
        Warp warp = warpList.get(name);
        warpList.remove(name);
        MyWarp.connectionManager.deleteWarp(warp);
    }

    public String getMatche(String name, Player player) {
        MatchList matches = this.getMatches(name, player);
        return matches.getMatch(name);
    }

    public MatchList getMatches(String name, Player player) {
        ArrayList<Warp> exactMatches = new ArrayList<Warp>();
        ArrayList<Warp> matches = new ArrayList<Warp>();

        List<String> names = new ArrayList<String>(warpList.keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        for (String currName : names) {
            Warp warp = warpList.get(currName);
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
            for (int i = 0; i < exactMatches.size(); i++) {
                Warp warp = exactMatches.get(i);
                if (!warp.name.equals(name)) {
                    exactMatches.remove(warp);
                    matches.add(0, warp);
                    i--;
                }
            }
        }
        return new MatchList(exactMatches, matches);
    }

    public String getMatchingCreator(Player player, String creator) {
        ArrayList<String> matches = new ArrayList<String>();
        List<String> names = new ArrayList<String>(warpList.keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        for (String currName : names) {
            Warp warp = warpList.get(currName);
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

    public double getMaxWarps(Player player) {
        return getMaxWarpsPerCreator(player, null);
    }

    public double getMaxWarpsPerCreator(Player player, String creator) {
        int count = 0;
        for (Warp warp : warpList.values()) {
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

    public int getSize() {
        return warpList.size();
    }

    public ArrayList<Warp> getSortedWarps(Player player, int start, int size) {
        return getSortedWarpsPerCreator(player, null, start, size);
    }

    public ArrayList<Warp> getSortedWarpsPerCreator(Player player,
            String creator, int start, int size) {
        ArrayList<Warp> ret = new ArrayList<Warp>();
        List<String> names = new ArrayList<String>(warpList.keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        int currentCount = 0;
        for (String name : names) {
            if (ret.size() == size) {
                return ret;
            }
            Warp warp = warpList.get(name);
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
        return warpList.get(name);
    }

    public void give(String name, String giveeName) {
        Warp warp = warpList.get(name);
        warp.setCreator(giveeName);
        MyWarp.connectionManager.updateCreator(warp);
    }

    public void inviteGroup(String name, String inviteeName) {
        Warp warp = warpList.get(name);
        warp.inviteGroup(inviteeName);
        MyWarp.connectionManager.updateGroupPermissions(warp);
    }

    public void invitePlayer(String name, String inviteeName) {
        Warp warp = warpList.get(name);
        warp.invite(inviteeName);
        MyWarp.connectionManager.updatePermissions(warp);
    }

    public void notWaiting(Player player) {
        welcomeMessage.remove(player.getName());
    }

    private int numPrivateWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpList.values()) {
            boolean privateAll = !warp.publicAll;
            String creator = warp.creator;
            if (creator.equals(player.getName()) && privateAll)
                size++;
        }
        return size;
    }

    private int numPublicWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpList.values()) {
            boolean publicAll = warp.publicAll;
            String creator = warp.creator;
            if (creator.equals(player.getName()) && publicAll)
                size++;
        }
        return size;
    }

    private int numWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpList.values()) {
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

    public void point(String name, Player player) {
        Warp warp = warpList.get(name);
        player.setCompassTarget(warp.getLocation(server));
    }

    public void privatize(String name) {
        Warp warp = warpList.get(name);
        warp.publicAll = false;
        MyWarp.connectionManager.publicizeWarp(warp, false);
    }

    public void publicize(String name) {
        Warp warp = warpList.get(name);
        warp.publicAll = true;
        MyWarp.connectionManager.publicizeWarp(warp, true);
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

    public void uninviteGroup(String name, String inviteeName) {
        Warp warp = warpList.get(name);
        warp.uninviteGroup(inviteeName);
        MyWarp.connectionManager.updateGroupPermissions(warp);
    }

    public void uninvitePlayer(String name, String inviteeName) {
        Warp warp = warpList.get(name);
        warp.uninvite(inviteeName);
        MyWarp.connectionManager.updatePermissions(warp);
    }

    public void updateLocation(String name, Player player) {
        Warp warp = warpList.get(name);
        warp.setLocation(player.getLocation());
        MyWarp.connectionManager.updateLocation(warp);
    }

    public boolean waitingForWelcome(Player player) {
        return welcomeMessage.containsKey(player.getName());
    }

    public boolean warpExists(String name) {
        return warpList.containsKey(name);
    }

    public ArrayList<Warp> warpsInvitedTo(Player player) {
        ArrayList<Warp> results = new ArrayList<Warp>();

        List<String> names = new ArrayList<String>(warpList.keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        for (String name : names) {
            Warp warp = warpList.get(name);
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

    public void warpTo(String name, Player player) {
        Warp warp = warpList.get(name);
        if (warp.warp(player, server)) {
            warp.visits++;
            MyWarp.connectionManager.updateVisits(warp);
            player.sendMessage(ChatColor.AQUA
                    + warp.getSpecificWelcomeMessage(player));
        }
    }

    public void welcomeMessage(String name, Player player) {
        Warp warp = warpList.get(name);
        welcomeMessage.put(player.getName(), warp);
    }
}
