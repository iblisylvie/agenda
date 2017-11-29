// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.trace;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-11-22
 */
public enum AgendaEventType implements BaseAgendaEventType {
  LOG_EVENT("LOG");

  private String type;

  AgendaEventType(String type) {
    this.type = type;
  }

  @Override
  public String getEventType() {
    return type;
  }
}
