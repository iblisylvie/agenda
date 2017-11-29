// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.trace;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-10-25
 */
public class LogEvent extends BaseLogEvent {

  public String details;

  public LogEvent(String details) {
    this.details = details;
  }

  @Override
  public String getLog() {
    return details;
  }
}
