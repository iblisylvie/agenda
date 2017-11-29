// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core;

import com.leyantech.ai.Agent.AgentStatus;
import com.leyantech.ai.ExecutionStackItemPb;
import com.leyantech.ai.ExecutionStackPb;
import com.leyantech.ai.agenda.agent.AgentDecorator;
import com.leyantech.ai.agenda.core.dtt.DialogueTaskTree;
import com.leyantech.utility.Logger;
import com.leyantech.utility.PbUtils;

import com.google.common.base.Preconditions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-12.
 */
public class ExecutionStack {

  private static final Logger LOG = new Logger(ExecutionStack.class);

  // Deque as stack
  private Deque<ExecutionStackItem> mStackTrace = new ArrayDeque<>();

  public ExecutionStack(ExecutionStackPb es, ExecutionHistory eh, DialogueTaskTree dtt) {
    fromPb(es, eh, dtt);
  }

  public void push(ExecutionStackItem esi) {
    Preconditions.checkNotNull(esi);
    mStackTrace.push(esi);
  }

  public ExecutionStackItem pop() {
    if (mStackTrace.isEmpty()) {
      return null;
    }
    return mStackTrace.pop();
  }

  public ExecutionStackItem peek() {
    if (mStackTrace.isEmpty()) {
      return null;
    }
    return mStackTrace.peek();
  }

  public ExecutionStackItem peekLast() {
    return mStackTrace.peekLast();
  }

  public boolean isEmpty() {
    return mStackTrace.isEmpty();
  }

  public int size() {
    return mStackTrace.size();
  }

  public AgentDecorator getAgentInFocus(InfoState infoState) {
    for (ExecutionStackItem item : mStackTrace) {
      if (item.getAgentDecorator().preconditionsSatisfied(infoState)) {
        return item.getAgentDecorator();
      }
    }
    return null;
  }

  public Deque<ExecutionStackItem> getStackTrace() {
    return mStackTrace;
  }

  public boolean remove(ExecutionStackItem o) {
    o.mAgentDecorator.setAgentStatus(AgentStatus.AS_EXECUTED);
    return mStackTrace.remove(o);
  }

  public ExecutionStackItem createExecutionStackItem(int historyIndex, ExecutionHistory eh,
      AgentDecorator agentDecorator, AgentDecorator contextAgent) {
    return new ExecutionStackItem(historyIndex, eh, agentDecorator, contextAgent);
  }

  private void fromPb(ExecutionStackPb esb, ExecutionHistory eh, DialogueTaskTree dtt) {
    mStackTrace.clear();
    for (ExecutionStackItemPb item : esb.getItemList()) {
      int historyIdx = item.getHistoryIndex();
      String agentName = eh.getAgentName(historyIdx);
      int contextAgentIndex = eh.getContextAgentHi(historyIdx);
      String contextAgentName = eh.getAgentName(contextAgentIndex);
      mStackTrace.add(createExecutionStackItem(historyIdx, eh, dtt.getAgent(agentName),
          dtt.getAgent(contextAgentName)));
    }
  }

  public ExecutionStackPb toPb() {
    ExecutionStackPb.Builder esib = ExecutionStackPb.newBuilder();
    mStackTrace.forEach(item -> {
      esib.addItem(item.toPb());
    });
    return esib.build();
  }

  public String toString() {
    return PbUtils.toShortDebugString(toPb());
  }

  public class ExecutionStackItem {

    private AgentDecorator mAgentDecorator;
    private int mHistoryIndex;

    public ExecutionStackItem(int hisotryIndex, ExecutionHistory eh, AgentDecorator agentDecorator,
        AgentDecorator contextAgent) {
      mHistoryIndex = hisotryIndex;
      mAgentDecorator = agentDecorator;
      if (mAgentDecorator != null) {
        mAgentDecorator.setHistoryIndex(mHistoryIndex);
        mAgentDecorator.setContextAgent(contextAgent);
      }
      mAgentDecorator.setAgentStatus(AgentStatus.AS_SCHEDULED);
    }

    public ExecutionStackItemPb toPb() {
      return ExecutionStackItemPb.newBuilder().setHistoryIndex(mHistoryIndex).build();
    }

    public AgentDecorator getAgentDecorator() {
      return mAgentDecorator;
    }

    public int getHistoryIndex() {
      return mHistoryIndex;
    }
  }
}
