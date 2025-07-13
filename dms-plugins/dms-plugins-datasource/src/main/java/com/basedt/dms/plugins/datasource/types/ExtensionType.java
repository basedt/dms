package com.basedt.dms.plugins.datasource.types;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtensionType implements Type {

    private String name;

    public ExtensionType(String name) {
        this.name = name;
    }

    public static ExtensionType get(String name) {
        return new ExtensionType(name);
    }

    @Override
    public DbDataType type() {
        return DbDataType.EXTENSION;
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public String formatString() {
        return getName();
    }
}
