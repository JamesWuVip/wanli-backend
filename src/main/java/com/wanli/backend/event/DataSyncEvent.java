package com.wanli.backend.event;

public class DataSyncEvent extends BaseEvent {
  private String syncType;
  private String sourceSystem;
  private String targetSystem;
  private String dataType;
  private int recordCount;
  private String status;

  public DataSyncEvent(
      String syncType,
      String sourceSystem,
      String targetSystem,
      String dataType,
      int recordCount,
      String status) {
    super();
    this.syncType = syncType;
    this.sourceSystem = sourceSystem;
    this.targetSystem = targetSystem;
    this.dataType = dataType;
    this.recordCount = recordCount;
    this.status = status;
  }

  public String getSyncType() {
    return syncType;
  }

  public String getSourceSystem() {
    return sourceSystem;
  }

  public String getTargetSystem() {
    return targetSystem;
  }

  public String getDataType() {
    return dataType;
  }

  public int getRecordCount() {
    return recordCount;
  }

  public String getStatus() {
    return status;
  }
}
