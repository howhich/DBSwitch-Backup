package com.example.demo.demos.handler;

import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.schema.ColumnDescription;
import com.example.demo.demos.service.DefaultMetadataService;
import com.example.demo.demos.service.MetadataService;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BackupHandler {
    private final CloseableDataSource sourceDataSource;
    private String sourceSchemaName;
    private String sourceTableName;
    private ProductTypeEnum sourceProductType;
    private MetadataService sourceMetaDataService;
    private List<ColumnDescription> sourceColumnDescriptions;
    private List<String> sourcePrimaryKeys;

    private String sourceTableRemarks;

    private List<ColumnDescription> targetColumnDescriptions;






    private static final String QUERY_TABLE_METADATA_SQL =
            "SELECT `TABLE_COMMENT`,`TABLE_TYPE` FROM `information_schema`.`TABLES` "
                    + "WHERE `TABLE_SCHEMA` = ? AND `TABLE_NAME` = ?";

    public BackupHandler(CloseableDataSource sourceDataSource,
                         String sourceSchemaName,
                         String sourceTableName,
                         ProductTypeEnum sourceProductType
                         ) {

        this.sourceDataSource = sourceDataSource;
        this.sourceSchemaName = sourceSchemaName;
        this.sourceTableName = sourceTableName;
        this.sourceProductType = sourceProductType;
    }
    public void backUp(){
        this.sourceMetaDataService = new DefaultMetadataService(sourceDataSource, sourceProductType);

        // 获取 备注 列 主键
        this.sourceTableRemarks = sourceMetaDataService.getTableRemark(sourceSchemaName, sourceTableName);
        this.sourceColumnDescriptions = sourceMetaDataService.queryTableColumnMeta(sourceSchemaName, sourceTableName);
        this.sourcePrimaryKeys =sourceMetaDataService.queryTablePrimaryKeys(sourceSchemaName,sourceTableName);
        // 通过列 获取值

    }

}
