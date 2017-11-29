// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.context;

import ai.iblis.agenda.agent.AgentDecorator;
import ai.iblis.agenda.core.InfoState;
import ai.iblis.agenda.core.trace.BaseAgendaEvent;
import ai.iblis.agenda.core.trace.BaseAgendaEventType;

import java.util.List;

public class ThreadLocalDialogueState implements DialogueStateItf {

  private final ThreadLocal<DialogueState> dialogueStateThreadLocal = ThreadLocal
      .withInitial(() -> new DialogueState());

  @Override
  public void setWorkingInfoState(InfoState infoState) {
    this.dialogueStateThreadLocal.get().setWorkingInfoState(infoState);
  }

  @Override
  public InfoState getWorkingInfoState() {
    return this.dialogueStateThreadLocal.get().getWorkingInfoState();
  }

  @Override
  public void setWorkingAgent(AgentDecorator workingAgent) {
    this.dialogueStateThreadLocal.get().setWorkingAgent(workingAgent);
  }

  @Override
  public AgentDecorator getWorkingAgent() {
    return this.dialogueStateThreadLocal.get().getWorkingAgent();
  }

  @Override
  public void log(BaseAgendaEvent executeEvent) {
    this.dialogueStateThreadLocal.get().log(executeEvent);
  }

  @Override
  public List<BaseAgendaEvent> getEventsByType(BaseAgendaEventType type) {
    return this.dialogueStateThreadLocal.get().getEventsByType(type);
  }

  @Override
  public void clearAll() {
    this.dialogueStateThreadLocal.get().clearAll();
    this.dialogueStateThreadLocal.remove();
  }
}
