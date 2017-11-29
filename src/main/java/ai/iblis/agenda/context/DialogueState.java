// Copyright 2017 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.context;


import ai.iblis.agenda.agent.AgentDecorator;
import ai.iblis.agenda.core.InfoState;
import ai.iblis.agenda.core.trace.BaseAgendaEvent;
import ai.iblis.agenda.core.trace.BaseAgendaEventType;

import java.util.List;

public class DialogueState implements DialogueStateItf {

  private InfoState infoState;
  private AgentDecorator agentDecorator;


  @Override
  public void setWorkingInfoState(InfoState infoState) {
    this.infoState = infoState;
  }

  @Override
  public InfoState getWorkingInfoState() {
    return this.infoState;
  }

  @Override
  public void setWorkingAgent(AgentDecorator workingAgent) {
    this.agentDecorator = workingAgent;
  }

  @Override
  public AgentDecorator getWorkingAgent() {
    return this.agentDecorator;
  }

  @Override
  public void log(BaseAgendaEvent event) {
    this.infoState.log(event);
  }

  @Override
  public List<BaseAgendaEvent> getEventsByType(BaseAgendaEventType type) {
    return infoState.getEventsByType(type);
  }

  @Override
  public void clearAll() {
    this.infoState.clearAll();
    this.infoState = null;
    this.agentDecorator = null;
  }
}
