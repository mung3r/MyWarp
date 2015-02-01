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

package me.taylorkelly.mywarp.dataconnections.migrators;

import java.util.Collection;

import me.taylorkelly.mywarp.warp.Warp;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A data migrator loads Warps from a read-only data source.
 */
public interface DataMigrator {

    /**
     * Gets all warps from the underlying data-source and cleans up afterwards.
     * 
     * @return a ListanbleFuture containing a Collection of all loaded warps.
     */
    ListenableFuture<Collection<Warp>> getWarps();

}
