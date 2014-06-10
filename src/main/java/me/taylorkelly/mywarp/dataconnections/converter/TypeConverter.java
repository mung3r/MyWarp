package me.taylorkelly.mywarp.dataconnections.converter;

import me.taylorkelly.mywarp.data.Warp;

import org.jooq.impl.EnumConverter;
import org.jooq.types.UByte;

public class TypeConverter extends EnumConverter<UByte, Warp.Type> {

    /**
     * 
     */
    private static final long serialVersionUID = -7704911259321558564L;

    public TypeConverter() {
        super(UByte.class, Warp.Type.class);
    }

}
