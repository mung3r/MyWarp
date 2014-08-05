package me.taylorkelly.mywarp.dataconnections.converter;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.jooq.Converter;

public class UUIDBinaryConverter implements Converter<byte[], UUID> {

    /**
     * 
     */
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
