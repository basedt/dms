package com.basedt.dms.plugins.datasource.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PartitionDTO extends ObjectDTO {

    private Long partitionRows;

    private Long partitionBytes;

    private String partitionExpr;

    public String getPartitionName() {
        return this.getObjectName();
    }

}
