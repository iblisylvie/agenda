// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.trace;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-11-11
 */
public abstract class BaseAgendaEvent {
  long when;  // when the event takes place.

  public BaseAgendaEvent() {
    this.when = System.currentTimeMillis();
  }

  protected long getWhen() {
    return this.when;
  }

  public abstract BaseAgendaEventType getEventType();
}

