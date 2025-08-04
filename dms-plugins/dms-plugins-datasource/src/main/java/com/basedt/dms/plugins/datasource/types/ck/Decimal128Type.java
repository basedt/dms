package com.basedt.dms.plugins.datasource.types.ck;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import com.basedt.dms.plugins.datasource.types.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Decimal128Type extends Type.NUMBER {

    private Integer scale;

    public Decimal128Type(Integer scale) {
        this.scale = scale;
    }

    public static Decimal128Type get(Integer scale) {
        return new Decimal128Type(scale);
    }

    @Override
    public DbDataType type() {
        return DbDataType.DECIMAL;
    }

    @Override
    public String name() {
        return "Decimal128";
    }

    @Override
    public String formatString() {
        return "Decimal128(" + scale + ")";
    }
}
