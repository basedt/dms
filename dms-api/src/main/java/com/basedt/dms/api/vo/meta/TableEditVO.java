package com.basedt.dms.api.vo.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TableEditVO {

    @NotNull
    @Schema(name = "dataSourceId", title = "data source id")
    private Long dataSourceId;

    private TableInfoVO originTable;

    private TableInfoVO newTable;

}
