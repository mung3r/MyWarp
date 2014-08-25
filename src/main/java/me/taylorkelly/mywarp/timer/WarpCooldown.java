/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.mywarp.timer;

import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.entity.Player;

/**
 * A cooldown that blocks a player from teleporting to warps.
 */
public class WarpCooldown extends TimerAction<UUID> {

    /**
     * Initializes this WarpCooldown.
     * 
     * @param player
     *            the player who is cooling down
     */
    public WarpCooldown(Player player) {
        super(player.getUniqueId(), MyWarp.inst().getPermissionsManager().getTimeBundleManager()
                .getBundle(player).getTicks(TimeBundle.Time.WARP_COOLDOWN));
    }

    @Override
    public void action() {
        if (MyWarp.inst().getSettings().isTimersCooldownNotifyOnFinish()) {
            Player player = MyWarp.server().getPlayer(type);

            if (player != null) {
                player.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.warp-to.cooldown.ended", player));
            }
        }
    }

}
