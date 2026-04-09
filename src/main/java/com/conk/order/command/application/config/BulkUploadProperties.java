package com.conk.order.command.application.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/* ORD-003 대용량 업로드 설정 프로퍼티. */
@Validated
@Component
@ConfigurationProperties(prefix = "order.bulk-upload")
public class BulkUploadProperties {

  @Min(1)
  private int maxRowLimit;

  @Min(1)
  private int flushInterval;

  public int getMaxRowLimit() {
    return maxRowLimit;
  }

  public void setMaxRowLimit(int maxRowLimit) {
    this.maxRowLimit = maxRowLimit;
  }

  public int getFlushInterval() {
    return flushInterval;
  }

  public void setFlushInterval(int flushInterval) {
    this.flushInterval = flushInterval;
  }
}
