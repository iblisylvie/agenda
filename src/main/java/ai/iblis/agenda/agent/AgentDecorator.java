// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.agent;

import ai.iblis.agenda.Agent;
import ai.iblis.agenda.Agent.AgentScope;
import ai.iblis.agenda.Agent.AgentStatus;
import ai.iblis.agenda.Agent.DialogueExecutionReturnCode;
import ai.iblis.agenda.Concept;
import ai.iblis.agenda.DTT;
import ai.iblis.agenda.context.ThreadContext;
import ai.iblis.agenda.core.DialogueExecutionFailCode;
import ai.iblis.agenda.core.InfoState;
import ai.iblis.agenda.core.SystemAgent;
import ai.iblis.agenda.core.trace.LogEvent;
import ai.iblis.agenda.utils.AgendaUtils;
import org.apache.commons.lang3.StringUtils;
import org.luaj.vm2.LuaValue;
import utils.Logger;
import utils.PbUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-07.
 */
public abstract class AgentDecorator {

  private static final Logger LOG = new Logger(AgentDecorator.class);
  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private int mHistoryIndex = -1;
  private Set<AgentDecorator> mParent = new HashSet<>();
  private ai.iblis.agenda.Agent mAgent;
  private List<AgentDecorator> mSubAgents = new ArrayList<>();
  private AgentDecorator mContextAgent;
  protected final int mMaxAttempts;
  private String mDomain = null;

  // TODO(qli): need refactor.
  public DialogueExecutionReturnCode executeExternal(InfoState infoState) {
    setAgentStatus(AgentStatus.AS_EXECUTING);
    LOG.info("{} is executing", getName());
    DialogueExecutionReturnCode derc = onExecution(infoState);
    ExecuteResult result = new ExecuteResult(toPb().getOnExecution(), derc, true, null);
    ExecuteResult agentResult = execute(infoState);
    if (agentResult.mSuccess) {
      onSuccess(infoState);
    } else {
      onFailure(infoState, agentResult.mErrorReason);
    }
    if (DialogueExecutionReturnCode.DERC_CONTINUE_EXECUTION.equals(result.mReturnCode)) {
      result = agentResult;
    }
    if (StringUtils.isEmpty(result.mTrace)) {
      LOG.fatal("Agent {} has no trace.", getName());
    }
    fromPb(mAgent.toBuilder().addExecTrace(result.mTrace).build());

    int attempts = getDuplicateAttempts(infoState);
    if (attempts > mMaxAttempts) {
      LOG.warn("Agent {} have tried {} times. Things are going wrong here. Turn over to human!",
          getName(), attempts);
      onError(infoState, DialogueExecutionFailCode.AGENT_HIGH_EXEC_FREQUENCY);
      return DialogueExecutionReturnCode.DERC_CONTINUE_EXECUTION;
    } else {
      return result.mReturnCode;
    }
  }

  public void setAgentStatus(AgentStatus agentStatus) {
    fromPb(mAgent.toBuilder().setStatus(agentStatus).build());
  }

  public AgentStatus getAgentStatus() {
    return toPb().getStatus();
  }

  public AgentScope getAgentScope() {
    return toPb().getScope();
  }

  public class ExecuteResult {

    String mTrace;
    DialogueExecutionReturnCode mReturnCode;
    boolean mSuccess;
    String mErrorReason;

    public ExecuteResult(String trace, DialogueExecutionReturnCode returnCode,
        boolean success, String errorReason) {
      mTrace = trace;
      mReturnCode = returnCode;
      mSuccess = success;
      mErrorReason = errorReason;
    }
  }

  protected abstract ExecuteResult execute(InfoState infoState);

  // 该agent已经被重复执行的次数，第一次执行为１，第二次执行为2
  protected int getDuplicateAttempts(InfoState infoState) {
    Set<Concept> concepts = getUpdatedConceptsBetweenTwoAttemps(infoState, this);
    if (!concepts.isEmpty()) {
      return 0;
    }
    int attempts = 0;
    int lastTraceIdx = mAgent.getExecTraceCount() - 1;
    if (lastTraceIdx < 0) {
      return attempts;
    }
    ++attempts;
    String trace = mAgent.getExecTrace(lastTraceIdx);
    while (--lastTraceIdx > 0) {
      if (mAgent.getExecTrace(lastTraceIdx).equals(trace)) {
        ++attempts;
      } else {
        break;
      }
    }
    return attempts;
  }

  private Set<Concept> getUpdatedConceptsBetweenTwoAttemps(
      InfoState infoState, AgentDecorator agentDecorator) {
    long previousExecutionTime = infoState.getLastExecutionTime(agentDecorator.getName(),
        agentDecorator.getHistoryIndex() - 1);
    Map<String, Concept> allConcepts = infoState.getAllConcepts();
    return allConcepts.values().stream()
        .filter(c -> c.getUpdatedTime() > 0 && c.getUpdatedTime() > previousExecutionTime)
        .collect(Collectors.toSet());
  }

  public int getAttempts() {
    return mAgent.getExecTraceCount();
  }

  public boolean preconditionsSatisfied(InfoState infoState) {
    if (toPb().getBlocked()) {
      LOG.info("{} is blocked.", getName());
      return false;
    }

    // If precondition not set, it's always true.
    String precondition = toPb().getPrecondition();
    if (StringUtils.isEmpty(precondition)) {
      return true;
    }

    return infoState.evaluateLuaExpressionAsBoolean(this, precondition);
  }

  // If triggered_when is not set, it's always false.
  public boolean triggerConditionSatisfied(InfoState infoState) {
    String triggeredWhen = toPb().getTriggeredWhen();
    if (StringUtils.isEmpty(triggeredWhen)) {
      return false;
    }
    return infoState.evaluateLuaExpressionAsBoolean(this, triggeredWhen);
  }

  public boolean onCompletion(InfoState infoState) {
    String onCompletion = toPb().getOnCompletion();
    if (StringUtils.isEmpty(onCompletion)) {
      return true;
    }
    return infoState.evaluateLuaExpressionAsBoolean(this, onCompletion);
  }

  public DialogueExecutionReturnCode onExecution(InfoState infoState) {
    String onExecution = toPb().getOnExecution();
    if (StringUtils.isEmpty(onExecution)) {
      return DialogueExecutionReturnCode.DERC_CONTINUE_EXECUTION;
    }
    LuaValue luaValue = infoState.evaluateLuaExpression(this, onExecution);
    if (LuaValue.NIL.equals(luaValue)) {
      return DialogueExecutionReturnCode.DERC_CONTINUE_EXECUTION;
    }
    return DialogueExecutionReturnCode.valueOf(luaValue.checkjstring());
  }

  protected boolean onFailure(InfoState infoState, String reason) {
    String onFailure = toPb().getOnFailure();
    if (StringUtils.isEmpty(onFailure)) {// by default, 转人工
      String errorAgent = onError(infoState, reason);
      LOG.info("{} execute fail, use error agent {}.", getClass().getSimpleName(), errorAgent);
      return true;
    }
    return infoState.evaluateLuaExpressionAsBoolean(this, onFailure);
  }

  protected boolean onSuccess(InfoState infoState) {
    String onSuccess = toPb().getOnSuccess();
    if (StringUtils.isEmpty(onSuccess)) {
      return true;
    }
    return infoState.evaluateLuaExpressionAsBoolean(this, onSuccess);
  }

  // by default an agent completes with a failure when the number of attempts at execution exceeds
  // maximum attempts, and the success criteria has not been met yet.
  private boolean failureCriteriaSatisfied(InfoState infoState) {
    int attempts = getDuplicateAttempts(infoState);
    if (attempts <= mMaxAttempts) {
      return false;
    }
    LOG.warn("Agent {} have tried {} times. Things are going wrong here. Turn over to human!",
        getName(), attempts);
    if (successCriteriaSatisfied(infoState)) {
      return false;
    }
    return true;
  }

  // by default an agent success when all the subagents has completed.
  protected boolean successCriteriaSatisfied(InfoState infoState) {
    String succeedsWhen = toPb().getSucceedsWhen();
    if (StringUtils.isEmpty(succeedsWhen)) {
      return defaultSuccessCriteriaSatisfied(infoState);
    }
    return infoState.evaluateLuaExpressionAsBoolean(this, succeedsWhen);
  }

  public void setBlocked(boolean blocked) {
    Agent.Builder builder = mAgent.toBuilder();
    builder.setBlocked(blocked);
    mAgent = builder.build();
  }

  public void setSucceedsWhen(String succeedsWhen) {
    Agent.Builder builder = mAgent.toBuilder();
    builder.setSucceedsWhen(succeedsWhen);
    mAgent = builder.build();
  }

  public String getOnExecution() {
    return toPb().getOnExecution();
  }

  public void setOnExecution(String script) {
    Agent.Builder builder = mAgent.toBuilder();
    builder.setOnExecution(script);
    mAgent = builder.build();
  }

  public void setGrounded(boolean grounded) {
    if (toPb().getGrounded()) {
      return;  // 现在系统内的agent都是循环使用的，很可能会出现例如
    }
    // GetUncompletedOrder call H2HAgent call GetUncompletedOrder.
    // 该agent被ground了，所有该agent的调用者都被grounded.
    // eg. H2HAgent call GetUncompletedCall, call failed then H2HAgent also failed, and it will
    // schedule its ground agent.
    LOG.debug("Agent {} is grounded.", getName());
    Agent.Builder builder = mAgent.toBuilder();
    builder.setGrounded(grounded);
    mAgent = builder.build();
    if (getContextAgent() != null) {
      getContextAgent().setGrounded(grounded);
    }
  }

  public void setSubAgents(Set<String> subAgents) {
    Agent.Builder builder = mAgent.toBuilder();
    builder.clearSubAgent();
    builder.addAllSubAgent(subAgents);
    mAgent = builder.build();
  }

  // Agency的subagent都成功则Agency成功
  // 其他种类的Agency的话，默认执行过一次就算成功了。
  private boolean defaultSuccessCriteriaSatisfied(InfoState infoState) {
    if (!getSubAgents().isEmpty()) {
      for (AgentDecorator subagent : getSubAgents()) {
        if (!subagent.hasCompleted(infoState)) {
          return false;
        }
      }
    } else if (mAgent.getExecTraceCount() == 0) {
      return false;
    }
    return true;
  }

  private boolean mCompleted = false;
  private CompletionType mCompletionType = CompletionType.FAILED;

  private enum CompletionType {
    SUCCESS,
    FAILED;
  }

  public boolean hasCompleted(InfoState infoState) {
    if (toPb().getGrounded()) {
      AgentDecorator groundAgent = infoState.getAgent(getGroundAgent());
      if (groundAgent != null && !groundAgent.getName().equals(getName())) {
        LOG.debug("Agent {} hasCompleted <==> Ground agent {} hasCompleted.", getName(),
            groundAgent.getName());
        return groundAgent.hasCompleted(infoState);
      }
    }
    if (mCompleted) {
      LOG.debug("{} completed since its completed flag is set.", getName());
      return true;
    }
    boolean completed = hasSucceeded(infoState) || hasFailed(infoState);
    if (completed) {
      LOG.debug("{} completed since it satisfied succeeds condition.", getName());
      return true;
    }
    return false;
  }

  private boolean hasSucceeded(InfoState infoState) {
    if (mCompleted && CompletionType.SUCCESS.equals(mCompletionType)) {
      return true;
    }
    return successCriteriaSatisfied(infoState);
  }

  private boolean hasFailed(InfoState infoState) {
    if (mCompleted && CompletionType.FAILED.equals(mCompletionType)) {
      return true;
    }
    return failureCriteriaSatisfied(infoState);
  }

  public void reset() {
    // reset all the concepts defined in this agent.
    LOG.debug("Reset agent {}", getName());
    Agent.Builder agentBuilder = mAgent.toBuilder();
    agentBuilder.getConceptBuilderList().forEach(cb -> {
      LOG.debug("Clear concept {}.", cb.getSlot().getName());
      cb.getSlotBuilder().setValue("").setNormedValue("");
    });
    agentBuilder.clearExecTrace();
    agentBuilder.setGrounded(false);
    agentBuilder.setStatus(AgentStatus.AS_TO_BE_SCHEDULED);
    mAgent = agentBuilder.build();

    // reset all the subagent.
    getSubAgents().forEach(subAgent -> subAgent.reset());

    mCompleted = false;
    mCompletionType = CompletionType.FAILED;
  }

  public void resetTopic() {
    // reset exec trace.
    Agent.Builder agentBuilder = mAgent.toBuilder();
    agentBuilder.clearExecTrace();
    mAgent = agentBuilder.build();

    // reopen all the subagent.
    getSubAgents().forEach(subAgent -> subAgent.resetTopic());
    mCompleted = false;
    mCompletionType = CompletionType.FAILED;
  }

  public String getName() {
    return mAgent.getName();
  }

  public void setHistoryIndex(int index) {
    mHistoryIndex = index;
  }

  public int getHistoryIndex() {
    return mHistoryIndex;
  }

  public void createGroundingModel() {
    LOG.fatal("supposed to do things here.");
  }

  public List<AgentDecorator> getSubAgents() {
    return mSubAgents;
  }

  public void addSubAgent(AgentDecorator agentDecorator) {
    mSubAgents.add(agentDecorator);
    agentDecorator.addParent(this);
  }

  public void setDomain(String domain) {
    if (StringUtils.isEmpty(domain)) {
      LOG.fatal("Agent {} require non empty domain, which should be set in dtt, please check.");
    }
    mDomain = domain;
  }

  public void declareFocus(InfoState infoState, List<AgentDecorator> agentsInFocus) {
    for (AgentDecorator rcAgent : mSubAgents) {
      rcAgent.declareFocus(infoState, agentsInFocus);
    }
    if (mAgent.getIsMainTopic() && preconditionsSatisfied(infoState)) {
      agentsInFocus.add(this);
    }
    if (triggerConditionSatisfied(infoState)) {
      agentsInFocus.add(this);
    }
  }

  private void addParent(AgentDecorator parent) {
    if (parent != null && getName().equals(parent.getName())) {
      LOG.fatal("parent agent {} is not allowed to be the same of this agent.　"
          + "here is the call stack {}", parent.getName(), AgendaUtils.getStackTrace());
    }
    mParent.add(parent);
  }

  public void setContextAgent(AgentDecorator contextAgent) {
    if (contextAgent != null && getName().equals(contextAgent.getName())) {
      LOG.fatal("context agent {} is not allowed to be the same of this agent.　"
          + "here is the call stack {}", contextAgent.getName(), AgendaUtils.getStackTrace());
    }
    mContextAgent = contextAgent;
  }

  public AgentDecorator getContextAgent() {
    return mContextAgent;
  }

  public Set<AgentDecorator> getParentAgents() {
    return mParent;
  }

  public AgentDecorator(Agent agent) {
    mMaxAttempts = agent.getMaxAttempts() > 0 ? agent.getMaxAttempts() : DEFAULT_MAX_ATTEMPTS;
    fromPb(agent);
  }

  public Agent toPb() {
    return mAgent;
  }

  private void fromPb(Agent agent) {
    mAgent = agent;
  }

  public DTT toDTT() {
    DTT.Builder builder = DTT.newBuilder();
    builder.setRootAgent(getName());
    builder.addAgent(toPb());
    Map<String, AgentDecorator> visited = new HashMap<>();
    Deque<AgentDecorator> queue = new ArrayDeque<>();
    queue.add(this);
    while (!queue.isEmpty()) {
      AgentDecorator rootAgent = queue.poll();
      rootAgent.getSubAgents().forEach(a -> {
        if (!visited.containsKey(a.getName())) {
          visited.put(a.getName(), a);
          builder.addAgent(a.toPb());
          queue.add(a);
        }
      });
    }
    builder.setDomain(mDomain);
    return builder.build();
  }

  public String getAgentType() {
    return toPb().getType();
  }

  public boolean isMainTopic() {
    return toPb().getIsMainTopic();
  }

  public String toString() {
    return PbUtils.toShortDebugString(mAgent);
  }

  public String onError(InfoState infoState, String reason) {
    LOG.debug("onError {}", reason);
    setGrounded(true);

    ThreadContext.log(new LogEvent(reason));
    String errorProcessingAgent = getGroundAgent();
    infoState.pushErrorHandler(this, errorProcessingAgent);
    return errorProcessingAgent;
  }

  private String getGroundAgent() {
    String groundAgent = toPb().getGroundAgent();
    if (StringUtils.isEmpty(groundAgent)) {
      if (getContextAgent() != null) {
        // pop up the error to be handled by its context. otherwise use 转人工 as default.
        groundAgent = getContextAgent().getGroundAgent();
      } else {
        groundAgent = SystemAgent.HANDOVER_TO_HUMAN_AGENT.getAgentName();
      }
    }
    return groundAgent;
  }

  public String printTree() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName()).append(": ");
    mSubAgents.forEach(agentDecorator -> {
      sb.append(agentDecorator.getName()).append("\t");
    });
    mSubAgents.forEach(agentDecorator -> {
      sb.append(agentDecorator.printTree()).append("\n");
    });
    return sb.toString();
  }

  public int getPriority() {
    return toPb().getPriority();
  }
}
