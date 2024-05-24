package com.example.demo.demos.dbswitch.provider.sync;

import com.example.demo.demos.dbswitch.common.util.ObjectCastUtils;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;

import java.util.List;

public class AutoCastTableDataSynchronizeProvider extends DefaultTableDataSynchronizeProvider {

  public AutoCastTableDataSynchronizeProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public long executeInsert(List<Object[]> records) {
    records.forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        row[i] = ObjectCastUtils.castByJdbcType(insertArgsType[i], row[i]);
      }
    });
    return super.executeInsert(records);
  }

  @Override
  public long executeUpdate(List<Object[]> records) {
    records.forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        //row[i] = ObjectCastUtils.castByJdbcType(updateArgsType[i], row[i]);
        row[i] = ObjectCastUtils.castByDetermine(row[i]);
      }
    });
    return super.executeUpdate(records);
  }

}
