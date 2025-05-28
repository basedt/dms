package com.basedt.dms.api.vo.meta;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TableInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String catalog;

    @NotNull
    private String schemaName;

    @NotNull
    private String tableName;

    private String comment;

    private List<ColumnInfoVO> columns;

    private List<IndexInfoVO> indexes;

    private List<PartitionInfoVO> partitions;

}
