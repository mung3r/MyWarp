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

package me.taylorkelly.mywarp.bukkit.economy;

import com.google.common.collect.ImmutableSortedSet;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.util.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.util.permissions.ValueBundle;
import me.taylorkelly.mywarp.economy.FeeProvider;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.SortedSet;

/**
 * Provides fees when running on Bukkit. The actual fees are stored in {@link FeeBundle}s managed by this provider. <p>
 * Players either need to have a specific permission of a certain bundle or they fall under a default bundle. If a
 * player has the permission for more than one bundle, the alphabetically first bundle will be used. </p>
 */
public class BukkitFeeProvider implements FeeProvider {

  private SortedSet<FeeBundle> configuredFees;
  private FeeBundle defaultFees;

  /**
   * Initializes this provider.
   *
   * @param configuredFees the configured FeeBundles that are assigned to a player via a specific permission
   * @param defaultFees    the default FeeBundle that acts as a fallback if a player has none of the specific
   *                       permissions
   */
  public BukkitFeeProvider(Iterable<FeeBundle> configuredFees, FeeBundle defaultFees) {
    this.configuredFees = ImmutableSortedSet.copyOf(configuredFees);
    this.defaultFees = defaultFees;

    for (ValueBundle bundle : configuredFees) {
      BukkitPermissionsRegistration.INSTANCE.register(new Permission(bundle.getPermission(), PermissionDefault.FALSE));
    }
  }

  @Override
  public BigDecimal getAmount(LocalPlayer player, FeeType fee) {
    return getFeeBundle(player).get(fee);
  }

  /**
   * Gets the appropriate FeeBundle for the given player.
   *
   * @param player the player
   * @return the appropriate FeeBundle
   */
  private FeeBundle getFeeBundle(LocalPlayer player) {
    for (FeeBundle bundle : configuredFees) {
      if (!player.hasPermission(bundle.getPermission())) {
        continue;
      }
      return bundle;
    }
    return defaultFees;
  }

  /**
   * A ValueBundle that bundles fees.
   */
  public static class FeeBundle extends ValueBundle {

    private EnumMap<FeeType, BigDecimal> fees = new EnumMap<FeeType, BigDecimal>(FeeType.class);

    /**
     * Initializes this bundle.
     *
     * @param identifier        the unique identifier
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
    public FeeBundle(String identifier, BigDecimal assetsFee, BigDecimal createFee, BigDecimal createPrivateFee,
                     BigDecimal deleteFee, BigDecimal giveFee, BigDecimal helpFee, BigDecimal infoFee,
                     BigDecimal inviteFee, BigDecimal listFee, BigDecimal pointFee, BigDecimal privatizeFee,
                     BigDecimal publicizeFee, BigDecimal uninviteFee, BigDecimal updateFee, BigDecimal warpPlayerFee,
                     BigDecimal warpSignCreateFee, BigDecimal warpSignUseFee, BigDecimal warpFee,
                     BigDecimal welcomeFee) {
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

}
