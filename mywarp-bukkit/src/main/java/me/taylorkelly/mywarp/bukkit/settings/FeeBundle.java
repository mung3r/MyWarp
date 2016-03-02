/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

package me.taylorkelly.mywarp.bukkit.settings;

import me.taylorkelly.mywarp.bukkit.util.permission.ValueBundle;
import me.taylorkelly.mywarp.service.economy.FeeType;

import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.util.EnumMap;

/**
 * A ValueBundle that bundles fees.
 */
public class FeeBundle extends ValueBundle {

  private EnumMap<FeeType, BigDecimal> fees = new EnumMap<FeeType, BigDecimal>(FeeType.class);

  /**
   * Creates a new bundle with the given {@code identifier} and the given {@code values}.
   *
   * <p>Individual fees are read from {@code values}. Non existing entries are read as  {@code 0}.</p>
   *
   * @param identifier the bundle's identifier
   * @param values     the bundle's values
   */
  FeeBundle(String identifier, ConfigurationSection values) {
    this(identifier, BigDecimal.valueOf(values.getDouble("assets", 0)),
         BigDecimal.valueOf(values.getDouble("create", 0)), BigDecimal.valueOf(values.getDouble("createPrivate", 0)),
         BigDecimal.valueOf(values.getDouble("delete", 0)), BigDecimal.valueOf(values.getDouble("give", 0)),
         BigDecimal.valueOf(values.getDouble("help", 0)), BigDecimal.valueOf(values.getDouble("info", 0)),
         BigDecimal.valueOf(values.getDouble("invite", 0)), BigDecimal.valueOf(values.getDouble("list", 0)),
         BigDecimal.valueOf(values.getDouble("point", 0)), BigDecimal.valueOf(values.getDouble("private", 0)),
         BigDecimal.valueOf(values.getDouble("public", 0)), BigDecimal.valueOf(values.getDouble("uninvite", 0)),
         BigDecimal.valueOf(values.getDouble("update", 0)), BigDecimal.valueOf(values.getDouble("warpPlayer", 0)),
         BigDecimal.valueOf(values.getDouble("warpSignCreate", 0)),
         BigDecimal.valueOf(values.getDouble("warpSignUse", 0)), BigDecimal.valueOf(values.getDouble("warpTo", 0)),
         BigDecimal.valueOf(values.getDouble("welcome", 0)));
  }

  /**
   * Creates a new bundle with the given {@code identifier} and the given values.
   *
   * @param identifier        the bundle's identifier
   * @param assetsFee         used when listing a player's warps with limit
   * @param createFee         used when creating a public warp
   * @param createPrivateFee  used when creating a private warp
   * @param deleteFee         used when a warp is deleted
   * @param giveFee           used when a warp is given to other users
   * @param helpFee           used when accessing the help-command
   * @param infoFee           used when using the info-command
   * @param inviteFee         used when inviting a user or a group
   * @param listFee           used when warps are listed via /warp list
   * @param pointFee          used when the compass is pointed to a warp
   * @param privatizeFee      used when a warp is publicized
   * @param publicizeFee      used when a warp is privatized
   * @param uninviteFee       used when uninviting users or groups
   * @param updateFee         used when a warp's location is updated
   * @param warpPlayerFee     used when a player is warped (/warp player)
   * @param warpSignCreateFee used upon warp sign creation
   * @param warpSignUseFee    used upon warp sign usage
   * @param warpFee           used when a users warps to a warp
   * @param welcomeFee        used when the welcome message is changed
   */
  FeeBundle(String identifier, BigDecimal assetsFee, BigDecimal createFee, BigDecimal createPrivateFee,
            BigDecimal deleteFee, BigDecimal giveFee, BigDecimal helpFee, BigDecimal infoFee, BigDecimal inviteFee,
            BigDecimal listFee, BigDecimal pointFee, BigDecimal privatizeFee, BigDecimal publicizeFee,
            BigDecimal uninviteFee, BigDecimal updateFee, BigDecimal warpPlayerFee, BigDecimal warpSignCreateFee,
            BigDecimal warpSignUseFee, BigDecimal warpFee, BigDecimal welcomeFee) {
    super(identifier, "mywarp.economy");

    fees.put(FeeType.ASSETS, assetsFee);
    fees.put(FeeType.CREATE, createFee);
    fees.put(FeeType.CREATE_PRIVATE, createPrivateFee);
    fees.put(FeeType.DELETE, deleteFee);
    fees.put(FeeType.GIVE, giveFee);
    fees.put(FeeType.HELP, helpFee);
    fees.put(FeeType.INFO, infoFee);
    fees.put(FeeType.INVITE, inviteFee);
    fees.put(FeeType.LIST, listFee);
    fees.put(FeeType.POINT, pointFee);
    fees.put(FeeType.PRIVATE, privatizeFee);
    fees.put(FeeType.PUBLIC, publicizeFee);
    fees.put(FeeType.UNINVITE, uninviteFee);
    fees.put(FeeType.UPDATE, updateFee);
    fees.put(FeeType.WARP_PLAYER, warpPlayerFee);
    fees.put(FeeType.WARP_SIGN_CREATE, warpSignCreateFee);
    fees.put(FeeType.WARP_SIGN_USE, warpSignUseFee);
    fees.put(FeeType.WARP_TO, warpFee);
    fees.put(FeeType.WELCOME, welcomeFee);
  }

  /**
   * Gets the fee referenced by the given FeeType.
   *
   * @param type the FeeType
   * @return the fee
   */
  public BigDecimal get(FeeType type) {
    return fees.get(type);
  }
}
