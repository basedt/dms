package com.basedt.dms.plugins.datasource.types.ck;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import com.basedt.dms.plugins.datasource.types.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Decimal64Type extends Type.NUMBER {

    private Integer scale;

    public Decimal64Type(Integer scale) {
        this.scale = scale;
    }

    public static Decimal64Type get(Integer scale) {
        return new Decimal64Type(scale);
    }

    @Override
    public DbDataType type() {
        return DbDataType.DECIMAL;
    }

    @Override
    public String name() {
        return "Decimal64";
    }

    @Override
    public String formatString() {
        return "Decimal64(" + scale + ")";
    }
}
