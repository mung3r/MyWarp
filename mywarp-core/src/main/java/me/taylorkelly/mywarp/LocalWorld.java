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

package me.taylorkelly.mywarp;

import java.util.UUID;

import me.taylorkelly.mywarp.util.Vector3;

/**
 * Represents a world (dimension).
 */
public interface LocalWorld {

    /**
     * Gets the name of this world.
     * 
     * @return this world's name
     */
    String getName();

    /**
     * Gets the unique ID of this world.
     * 
     * @return this world's unique ID
     */
    UUID getUniqueId();

    /**
     * Gets the block at the given position.
     * 
     * @param position
     *            the position vector
     * @return the block
     */
    BlockType getBlock(Vector3 position);

}
