// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.trace;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-11-16
 */
public abstract class BaseLogEvent extends BaseAgendaEvent {

  @Override
  public BaseAgendaEventType getEventType() {
    return AgendaEventType.LOG_EVENT;
  }

  public abstract String getLog();
}
