// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core;


import ai.iblis.agenda.Concept;
import ai.iblis.agenda.agent.AgentDecorator;
import ai.iblis.agenda.core.dtt.DialogueTaskTree;
import ai.iblis.agenda.core.script.LuaInterpreter;
import ai.iblis.agenda.core.trace.BaseAgendaEvent;
import ai.iblis.agenda.utils.AgendaUtils;
import org.luaj.vm2.LuaValue;
import rx.subjects.ReplaySubject;
import utils.Logger;
import utils.PbUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-12.
 */
public class InfoState {

  private static final Logger LOG = new Logger(InfoState.class);

  private String mDomain;
  private ExecutionStack mExecutionStack;
  public ExecutionHistory mExecutionHistory;
  private ErrorHandlingProcess mErrorHandlingProcess = new ErrorHandlingProcess();
  private DialogueTaskTree mDTT;
  private static long MIN_CONSECUTIVE_EXECUTION_INTERVAL = 200;
  private ReplaySubject<BaseAgendaEvent> replaySubject = ReplaySubject.create(1000);

  public InfoState(InfoStatePb infoStatePb, DialogueTaskTree dtt)
      throws PlatformException {
    fromPb(infoStatePb, dtt);
  }

  public boolean evaluateLuaExpressionAsBoolean(AgentDecorator agentDecorator, String expression) {
    return LuaInterpreter.getInstance().evaluate(this, agentDecorator, expression);
  }

  public LuaValue evaluateLuaExpression(AgentDecorator agentDecorator, String expression) {
    return LuaInterpreter.getInstance().evaluateLuaExpression(this, agentDecorator, expression);
  }

  public String getTradeFromQuestion() {
    return getNormValue(CONCEPT_USER_PROVIDED_ORDER_NUMBER.getName());
  }

  public String getDomain() {
    return mDomain;
  }

  public void bindConcept(Map<String, Concept> conceptMap) {
    Set<String> conceptToRemove = new HashSet<>();
    conceptMap.forEach((k, v) -> {
      if (getDTT().getConceptContainer().updateConcept(v)) {
        conceptToRemove.add(k);
      }
    });
    conceptToRemove.forEach(c -> conceptMap.remove(c));
    conceptMap.forEach((k, v) -> {
      LOG.debug("Failed to find concept definition of {} and thus failed to bind value {} to it.",
          k, PbUtils.toShortDebugString(v));
    });
  }

  public int processErrors() {
    return mErrorHandlingProcess.process(this);
  }

  public void pushErrorHandler(AgentDecorator pusher, String errorHandlerAgentName) {
    LOG.debug("{} push error handler {} caused by {}",
        pusher == null ? null : pusher.getName(), errorHandlerAgentName,
        AgendaUtils.getCallerInfo());
    AgentDecorator errorAgent = getDTT().getAgent(errorHandlerAgentName);
    if (errorAgent == null) {
      LOG.warn("Failed to find the definition of agent {}.", errorHandlerAgentName);
      return;
    }
    ErrorHandlingProcess.ErrorHandler errorHandler = mErrorHandlingProcess
        .createNewErrorHandler(pusher, errorAgent);
    mErrorHandlingProcess.regist(errorHandler);
  }

  public DialogueTaskTree getDTT() {
    return mDTT;
  }

  public void clearAllConcepts() {
    getDTT().getConceptContainer().clear();
  }

  public void clearConcept(String conceptName) {
    getDTT().getConceptContainer().clear(conceptName);
  }

  public Map<String, Concept> getAllConcepts() {
    return getDTT().getConceptContainer().getAllConcepts();
  }

  public Concept getConcept(String conceptName) {
    return getDTT().getConceptContainer().getConcept(conceptName);
  }

  public String getNormValue(String conceptName) {
    return getDTT().getConceptContainer().getNormValue(conceptName);
  }

  public void updateConcept(Concept newConcept) {
    getDTT().getConceptContainer().updateConcept(newConcept);
  }

  public AgentDecorator getRootAgent() {
    return getDTT().getRootAgent();
  }

  private void initExecutionStackAndHistory() {
    LOG.debug("initExecutionStackAndHistory mExecutionStack size {} mExecutionHistory size {} ",
        mExecutionStack.size(), mExecutionHistory.size());
    if (mExecutionStack.isEmpty()) {
      pushDAOnStack(-1, getDTT().getRootAgent());
    }
  }

  public void pushDAOnStack(int pusherAgentHistoryIndex, String daName) {
    AgentDecorator da = getDTT().getAgent(daName);
    if (da == null) {
      LOG.error("Failed to find agent {}.", daName);
      return;
    }
    pushDAOnStack(pusherAgentHistoryIndex, da);
  }

  // Push a new dialogue agent on the execution stack.
  public void pushDAOnStack(int pusherAgentHistoryIndex, AgentDecorator da) {
    LOG.debug("Agent {} push agent {} onto the stack",
        mExecutionHistory.getAgentName(pusherAgentHistoryIndex), da.getName());
    // add entry to execution history.
    int nextHistoryIndex = mExecutionHistory.size();
    mExecutionHistory.push(mExecutionHistory
        .createNewExecutionHistoryItem(da.getName(), System.currentTimeMillis(),
            pusherAgentHistoryIndex, nextHistoryIndex));
    // add entry to execution stack.
    String contextAgentName = mExecutionHistory.getAgentName(pusherAgentHistoryIndex);
    AgentDecorator contextAgent = null;
    if (!StringUtils.isEmpty(contextAgentName)) {
      contextAgent = getDTT().getAgent(contextAgentName);
    } else {
      LOG.debug("No context agent found for agent {}.", da.getName());
    }

    mExecutionStack.push(mExecutionStack
        .createExecutionStackItem(nextHistoryIndex, mExecutionHistory, da, contextAgent));
  }

  public boolean isStackEmpty() {
    return mExecutionStack.isEmpty();
  }

  public String takeStackSnapshot() {
    return mExecutionStack.toString();
  }

  public AgentDecorator getAgentInFocus() {
    return mExecutionStack.getAgentInFocus(this);
  }

  public AgentDecorator getAgent(String agentName) {
    return getDTT().getAgent(agentName);
  }

  public ExecutionStack.ExecutionStackItem peekStack() {
    return mExecutionStack.peek();
  }

  public int getStackSize() {
    return mExecutionStack.size();
  }

  public ExecutionHistory.ExecutionHistoryItem peekHistory() {
    return mExecutionHistory.peek();
  }

  public ExecutionHistory getExecutionHistory() {
    return mExecutionHistory;
  }

  public int countAgentFrequencyInWholeHistory(String agentName) {
    return mExecutionHistory.countAgentFrequencyInWholeHistory(agentName);
  }

  public long getLastExecutionTime(String agentName, int from) {
    return mExecutionHistory.getLastExecutionTime(agentName, from);
  }

  public long getLastExecutionTime(String agentName) {
    return mExecutionHistory.getLastExecutionTime(agentName, mExecutionHistory.size() - 1);
  }

  public boolean onceExecuted(String agentName) {
    return mExecutionHistory.contains(agentName, mExecutionHistory.size() - 2);
  }

  public String getPrevMainTopic(AgentDecorator agentDecorator) {
    return mExecutionHistory.getPrevMainTopic(getDTT(), agentDecorator);
  }

  public Set<String> popCompletedAgentsFromStack() {
    LOG.debug("Try to find completed agents from stack.");
    Set<String> completedAgents = new HashSet<>();
    // eliminated agents包括completed agents 和 那些被completed agents plan的agents.
    Set<String> eliminatedAgents = new HashSet<>();
    boolean foundCompleted;
    do {
      foundCompleted = false;
      for (ExecutionStack.ExecutionStackItem esi : mExecutionStack.getStackTrace()) {
        if (esi.getAgentDecorator().hasCompleted(this)) {
          completedAgents.add(esi.getAgentDecorator().getName());
          eliminatedAgents.addAll(popTopicFromExecutionStack(esi));
          foundCompleted = true;
          break;
        }
      }
    } while (foundCompleted);

    // log and return.
    LOG.debug("Agent {} is completed.", completedAgents);
    LOG.debug("Agent {} is popped from stack.", eliminatedAgents);
    return completedAgents;
  }

  // Pops a dialog agent from the execution stack, together with all the other agents it has ever
  // planned for execution
  private Set<String> popTopicFromExecutionStack(ExecutionStack.ExecutionStackItem topic) {
    if (mExecutionStack.isEmpty()) {
      LOG.error("Cannot pop {} off the execution stack since the stack is empty.");
      return new HashSet<>();
    }
    Set<String> eliminatedAgents = new HashSet<>();

    removeAgentFromStack(topic, eliminatedAgents);

    boolean foundAgentToRemove = true;
    while (foundAgentToRemove) {
      foundAgentToRemove = false;
      for (ExecutionStack.ExecutionStackItem esi : mExecutionStack.getStackTrace()) {
        int contextHi = mExecutionHistory.getContextAgentHi(esi.getHistoryIndex());
        String agent = mExecutionHistory.getAgentName(esi.getHistoryIndex());
        String scheduledBy = mExecutionHistory.getAgentName(contextHi);
        if (eliminatedAgents.contains(scheduledBy)) {
          removeAgentFromStack(esi, eliminatedAgents);
          foundAgentToRemove = true;
          break;
        }
      }
    }

    return eliminatedAgents;
  }

  private void removeAgentFromStack(ExecutionStack.ExecutionStackItem esi,
      Set<String> eliminatedAgents) {
    mExecutionHistory.updateAgentTimeTerminated(esi.getHistoryIndex(), System.currentTimeMillis());
    esi.getAgentDecorator().onCompletion(this);
    boolean success = mExecutionStack.remove(esi);
    if (!success) {
      LOG.error("Failed to remove agent {} from stack.", esi.getAgentDecorator().getName());
      return;
    }

    eliminatedAgents.add(esi.getAgentDecorator().getName());
  }

  private void fromPb(InfoStatePb infoStatePb, DialogueTaskTree dialogueTaskTree) throws PlatformException {
    LOG.debug("Construct infostate from proto {}", PbUtils.toShortDebugString(infoStatePb));
    mDomain = infoStatePb.getDomain();
    Preconditions.checkArgument(!StringUtils.isEmpty(mDomain));
    mDTT = dialogueTaskTree;
    mExecutionHistory = new ExecutionHistory(infoStatePb.getHistory());
    mExecutionStack = new ExecutionStack(infoStatePb.getStack(), mExecutionHistory, getDTT());
    initExecutionStackAndHistory();
  }

  public InfoStatePb toPb() {
    return InfoStatePb.newBuilder()
        .setDomain(mDomain)
        .setHistory(mExecutionHistory.toPb())
        .setStack(mExecutionStack.toPb())
        .setRootDtt(getDTT().getRootDTT().getName())
        .addDtt(getDTT().toDTT())
        // TODO(qli): add output history here??
        .setTimestamp(System.currentTimeMillis())
        .build();
  }

  public String toString() {
    return PbUtils.toShortDebugString(toPb());
  }

  public void log(BaseAgendaEvent event) {
    this.replaySubject.onNext(event);
  }

  public List<BaseAgendaEvent> getEventsByType(BaseAgendaEventType type) {
    List<BaseAgendaEvent> events = new ArrayList<>();
    replaySubject
        .filter(baseAgendaEvent -> baseAgendaEvent.getEventType().equals(type))
        .subscribe(e -> events.add(e));
    return events;
  }

  public void clearAll() {
    this.replaySubject.onCompleted();
    this.replaySubject = ReplaySubject.create(1000);
  }

  protected void finalize() throws Throwable {
    clearAll();
  }
}
