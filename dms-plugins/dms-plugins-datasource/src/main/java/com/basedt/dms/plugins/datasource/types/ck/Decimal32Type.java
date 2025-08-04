package com.basedt.dms.plugins.datasource.types.ck;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import com.basedt.dms.plugins.datasource.types.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Decimal32Type extends Type.NUMBER {

    private Integer scale;

    public Decimal32Type(Integer scale) {
        this.scale = scale;
    }

    public static Decimal32Type get(Integer scale) {
        return new Decimal32Type(scale);
    }

    @Override
    public DbDataType type() {
        return DbDataType.DECIMAL;
    }

    @Override
    public String name() {
        return "Decimal32";
    }

    @Override
    public String formatString() {
        return "Decimal32(" + scale + ")";
    }
}
