// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.context;

import ai.iblis.agenda.agent.AgentDecorator;
import ai.iblis.agenda.core.InfoState;
import ai.iblis.agenda.core.trace.BaseAgendaEvent;
import ai.iblis.agenda.core.trace.BaseAgendaEventType;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2017-10-19
 */
public class ThreadContext {

  private static final DialogueStateItf dialogueState = new ThreadLocalDialogueState();

  public static DialogueStateItf getDialogueState() {
    return dialogueState;
  }

  public static void setWorkingInfoState(InfoState infoState) {
    dialogueState.setWorkingInfoState(infoState);
  }

  public static InfoState getWorkingInfoState() {
    return dialogueState.getWorkingInfoState();
  }

  public static void setWorkingAgent(AgentDecorator workingAgent) {
    dialogueState.setWorkingAgent(workingAgent);
  }

  public static AgentDecorator getWorkingAgent() {
    return dialogueState.getWorkingAgent();
  }

  public static void log(BaseAgendaEvent event) {
    dialogueState.log(event);
  }

  public static List<BaseAgendaEvent> getEventsByType(BaseAgendaEventType type) {
    return dialogueState.getEventsByType(type);
  }

  public static void clearAll() {
    dialogueState.clearAll();
  }

  public static Pair<InfoState, AgentDecorator> continueWith(InfoState newInfoState,
      AgentDecorator newAgentDecorator) {
    InfoState oldInfoState = ThreadContext.getWorkingInfoState();
    ThreadContext.setWorkingInfoState(newInfoState);

    AgentDecorator oldAgentDecorator = ThreadContext.getWorkingAgent();
    ThreadContext.setWorkingAgent(newAgentDecorator);

    return Pair.of(oldInfoState, oldAgentDecorator);
  }

  public static void finishWith(Pair<InfoState, AgentDecorator> previousSpot,
      InfoState workingInfoState, AgentDecorator workingAgent) {
    InfoState infoState = ThreadContext.getWorkingInfoState();
    ThreadContext.setWorkingInfoState(previousSpot.getLeft());
    Preconditions.checkArgument(Objects.equals(workingInfoState, infoState));

    AgentDecorator agent = ThreadContext.getWorkingAgent();
    ThreadContext.setWorkingAgent(previousSpot.getRight());
    Preconditions.checkArgument(Objects.equals(workingAgent, agent));
  }
}
