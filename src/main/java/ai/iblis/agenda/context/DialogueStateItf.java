// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.context;

import ai.iblis.agenda.agent.AgentDecorator;
import ai.iblis.agenda.core.InfoState;
import ai.iblis.agenda.core.trace.BaseAgendaEvent;
import ai.iblis.agenda.core.trace.BaseAgendaEventType;

import java.util.List;

public interface DialogueStateItf {
  void setWorkingInfoState(InfoState infoState);
  InfoState getWorkingInfoState();

  void setWorkingAgent(AgentDecorator workingAgent);
  AgentDecorator getWorkingAgent();

  void log(BaseAgendaEvent event);
  List<BaseAgendaEvent> getEventsByType(BaseAgendaEventType type);

  void clearAll();
}
