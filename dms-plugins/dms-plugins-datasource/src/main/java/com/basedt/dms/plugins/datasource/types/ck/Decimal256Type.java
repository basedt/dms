package com.basedt.dms.plugins.datasource.types.ck;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import com.basedt.dms.plugins.datasource.types.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Decimal256Type extends Type.NUMBER {

    private Integer scale;

    public Decimal256Type(Integer scale) {
        this.scale = scale;
    }

    public static Decimal256Type get(Integer scale) {
        return new Decimal256Type(scale);
    }

    @Override
    public DbDataType type() {
        return DbDataType.DECIMAL;
    }

    @Override
    public String name() {
        return "Decimal256";
    }

    @Override
    public String formatString() {
        return "Decimal256(" + scale + ")";
    }
}
