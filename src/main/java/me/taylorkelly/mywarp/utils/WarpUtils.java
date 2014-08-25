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
package me.taylorkelly.mywarp.utils;

import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import com.google.common.base.Predicate;

/**
 * Utility methods to work with warps.
 */
public final class WarpUtils {

    /**
     * Block initialization of this class.
     */
    private WarpUtils() {
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the warp being
     * tested is created by the given player.
     * 
     * @see Warp#isCreator(OfflinePlayer)
     * @param player
     *            the player
     * @return a predicate that checks if the given warp is created by the given
     *         player
     */
    public static Predicate<Warp> isCreator(final OfflinePlayer player) {
        return new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isCreator(player);
            }

        };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the warp being
     * tested is modifiable by the given command-sender.
     * 
     * @see Warp#isModifiable(CommandSender)
     * @param sender
     *            the command-sender
     * @return a predicate that checks if the given warp is modifiable by the
     *         given command-sender
     */
    public static Predicate<Warp> isModifiable(final CommandSender sender) {
        return new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isModifiable(sender);
            }

        };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the warp being
     * tested is of the given type.
     * 
     * @see Warp#isType(Type)
     * @param type
     *            the type
     * @return a predicate that checks if the given warp is of the given type
     */
    public static Predicate<Warp> isType(final Type type) {
        return new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isType(type);
            }

        };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the warp being
     * tested is usable by the given entity.
     * 
     * @see Warp#isUsable(Entity)
     * @param entity
     *            the entity
     * @return a predicate that checks if the given warp is usable by the given
     *         entity
     */
    public static Predicate<Warp> isUsable(final Entity entity) {
        return new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isUsable(entity);
            }

        };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the warp being
     * tested is viewable by the given command-sander.
     * 
     * @see Warp#isViewable(CommandSender)
     * @param sender
     *            the command-sender
     * @return a predicate that checks if the given warp is usable by the given
     *         command-sender
     */
    public static Predicate<Warp> isViewable(final CommandSender sender) {
        return new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isViewable(sender);
            }

        };
    }

}
