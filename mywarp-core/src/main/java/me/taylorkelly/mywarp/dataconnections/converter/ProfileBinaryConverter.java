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

package me.taylorkelly.mywarp.dataconnections.converter;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.profile.Profile;

import org.jooq.Converter;

/**
 * Converts byte arrays to {@link Profile}s and back.
 *
 */
public class ProfileBinaryConverter implements Converter<byte[], Profile> {

    private static final long serialVersionUID = 713212664614712270L;

    private final UUIDBinaryConverter converter = new UUIDBinaryConverter();

    @Override
    public Profile from(byte[] databaseObject) {
        if (databaseObject == null) {
            return null;
        }
        return MyWarp.getInstance().getProfileService().get(converter.from(databaseObject));
    }

    @Override
    public byte[] to(Profile userObject) {
        if (userObject == null) {
            return null;
        }

        return converter.to(userObject.getUniqueId());
    }

    @Override
    public Class<byte[]> fromType() {
        return byte[].class;
    }

    @Override
    public Class<Profile> toType() {
        return Profile.class;
    }
}
