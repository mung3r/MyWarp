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
package me.taylorkelly.mywarp.dataconnections.converter;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.jooq.Converter;

/**
 * Converts byte arrays to {@link UUID}s and back.
 *
 */
public class UUIDBinaryConverter implements Converter<byte[], UUID> {

    private static final long serialVersionUID = 713212664614712270L;

    @Override
    public UUID from(byte[] databaseObject) {
        if (databaseObject == null) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(databaseObject);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    @Override
    public byte[] to(UUID userObject) {
        if (userObject == null) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(userObject.getMostSignificantBits());
        byteBuffer.putLong(userObject.getLeastSignificantBits());
        return byteBuffer.array();
    }

    @Override
    public Class<byte[]> fromType() {
        return byte[].class;
    }

    @Override
    public Class<UUID> toType() {
        return UUID.class;
    }
}
