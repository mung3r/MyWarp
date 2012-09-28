package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SafeTeleport {

    public static boolean safeTeleport(Player player, Location l, String name) {
        if (isHalfBlock(l.getBlock().getType())) {
            l.add(0, 1, 0);
        }
        if (WarpSettings.useWarpSafety){
            Location safe = SafeLocation.getSafeLocation(l);
            if (safe == null) {
                player.sendMessage(ChatColor.RED
                        + "Warp "
                        + name
                        + " isn't safe. Also no safe loaction could be found close to it.");
                return false;
            }
            if (safe != l) {
                warpEffect(player.getLocation());
                player.teleport(safe);
                player.sendMessage(ChatColor.RED
                        + "Warp "
                        + name
                        + "isn't safe. You were teleported to the closest safe location.");
                return false;
            }
        }
        warpEffect(player.getLocation());
        player.teleport(l);
        return true;
    }
    
    private static void warpEffect(Location loc) {
        if (WarpSettings.warpEffect) {
            loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
            loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
            loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
        }
    }

    private static boolean isHalfBlock(Material type) {
        switch (type.getId()) {
        case 44:
            return true;
        case 126:
            return true;
        case 53:
            return true;
        case 67:
            return true;
        case 92:
            return true;
        case 108:
            return true;
        case 109:
            return true;
        case 114:
            return true;
        case 128:
            return true;
        case 134:
            return true;
        case 135:
            return true;
        case 136:
            return true;
        default:
            return false;
        }
    }
}
