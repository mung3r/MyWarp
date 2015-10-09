/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.CommandUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.util.profile.Profile;

/**
 * A cooldown that blocks a player from using warps.
 */
public class WarpCooldown extends TimerAction<Profile> {

  private static final DynamicMessages MESSAGES = new DynamicMessages(CommandUtils.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;

  /**
   * Craetes an instance for the given {@code player}.
   *
   * @param myWarp the MyWarp instance
   * @param player the player who is cooling down
   */
  public WarpCooldown(MyWarp myWarp, LocalPlayer player) {
    super(player.getProfile());
    this.myWarp = myWarp;
  }

  @Override
  public void run() {
    if (myWarp.getSettings().isTimersCooldownNotifyOnFinish()) {
      Optional<LocalPlayer> optionalPlayer = myWarp.getGame().getPlayer(getTimedSuject().getUniqueId());

      if (optionalPlayer.isPresent()) {
        LocalPlayer player = optionalPlayer.get();
        LocaleManager.setLocale(player.getLocale());
        player.sendMessage(MESSAGES.getString("warp-to.cooldown.ended"));
      }
    }
  }

}
