// Copyright 2016 Leyantech Ltd. All Rights Reserved.
package ai.iblis.agenda.core;

import com.leyantech.ai.ExecutionHistoryItemPb;
import com.leyantech.ai.ExecutionHistoryPb;
import com.leyantech.ai.agenda.agent.AgentDecorator;
import com.leyantech.ai.agenda.core.DialogueExecutionFailCode.AbstractHistory;
import com.leyantech.ai.agenda.core.dtt.DialogueTaskTree;
import com.leyantech.utility.Logger;
import com.leyantech.utility.PbUtils;

import java.util.Iterator;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-12.
 */
public class ExecutionHistory extends AbstractHistory<ExecutionHistory.ExecutionHistoryItem> {
  private static final Logger LOG = new Logger(ExecutionHistory.class);

  public ExecutionHistory(ExecutionHistoryPb eh) {
    fromPb(eh);
  }

  public int countAgentFrequencyInWholeHistory(String agentName) {
    int count = 0;
    for (ExecutionHistoryItem ehi : mHistory) {
      if (ehi.getAgentName().equals(agentName)) {
        ++count;
      }
    }
    return count;
  }

  // 自上次该agent执行依赖，指定concept是否被更新了
  public long getLastExecutionTime(String agentName, int from) {
    Iterator<ExecutionHistoryItem> it = getIterator(from);
    if (it == null) return -1;
    while (it.hasNext()) {
      ExecutionHistoryItem ehi = it.next();
      if (ehi.getAgentName().equals(agentName)) {
        return ehi.getTimeScheduled();
      }
    }
    return -1;
  }

  // 自from开始存在该agent
  public boolean contains(String agentName, int from) {
    Iterator<ExecutionHistoryItem> it = getIterator(from);
    if (it == null) return false;
    while (it.hasNext()) {
      ExecutionHistoryItem ehi = it.next();
      if (ehi.getAgentName().equals(agentName)) {
        return true;
      }
    }
    return false;
  }

  public String getPrevMainTopic(DialogueTaskTree dtt, AgentDecorator currentAgent) {
    int startIdx = mHistory.size() - 1;
    if (currentAgent != null) {
      startIdx = currentAgent.getHistoryIndex() - 1;
    }

    Iterator<ExecutionHistoryItem> it = getIterator(startIdx);
    if (it == null)
      it = mHistory.iterator();
    while (it.hasNext()) {
      ExecutionHistoryItem ehi = it.next();
      AgentDecorator agent = dtt.getAgent(ehi.getAgentName());
      if (agent == null)
        continue;
      if (agent.isMainTopic())
        return ehi.getAgentName();
    }
    return null;
  }

  public String getAgentName(int historyIndex) {
    ExecutionHistoryItem ehi = get(historyIndex);
    if (ehi == null)
      return null;
    return ehi.getAgentName();
  }

  public int getContextAgentHi(int historyIndex) {
    ExecutionHistoryItem ehi = get(historyIndex);
    if (ehi == null)
      return -1;
    return ehi.getContextAgentHi();
  }

  public long getAgentScheduledTime(int historyIndex) {
    ExecutionHistoryItem ehi = get(historyIndex);
    if (ehi != null)
      return ehi.getTimeScheduled();
    return 0;
  }

  public void updateAgentTimeTerminated(int historyIndex, long currentTime) {
    ExecutionHistoryItem ehi = get(historyIndex);
    if (ehi != null)
      ehi.setTimeTerminated(currentTime);
  }

  public void updateAgentExecutionTime(int historyIndex, long currentTime) {
    ExecutionHistoryItem ehi = get(historyIndex);
    if (ehi != null)
      ehi.setTimeScheduled(currentTime);
  }

  public void updateAgentExecutionStatus(int historyIndex, boolean executed) {
    ExecutionHistoryItem ehi = get(historyIndex);
    if (ehi != null)
      ehi.setExecuted(executed);
  }

  protected ExecutionHistoryItem createNewExecutionHistoryItem(String agent, long timeScheduled,
      int parent_hi, int hi) {
    ExecutionHistoryItemPb.Builder itemPb = ExecutionHistoryItemPb.newBuilder().setAgent(agent)
        .setTimeScheduled(timeScheduled).setScheduled(true).setExecuted(false).setCommitted(false)
        .setCancelled(false).setHistoryIndex(hi).setParentHi(parent_hi);
    return new ExecutionHistoryItem(itemPb.build());
  }

  private void fromPb(ExecutionHistoryPb eh) {
    mHistory.clear();
    for (ExecutionHistoryItemPb ehi : eh.getItemList()) {
      mHistory.add(new ExecutionHistoryItem(ehi));
    }
  }

  public ExecutionHistoryPb toPb() {
    ExecutionHistoryPb.Builder ehib = ExecutionHistoryPb.newBuilder();
    mHistory.forEach(item -> ehib.addItem(item.toPb()));
    return ehib.build();
  }

  public String toString() {
    return PbUtils.toShortDebugString(toPb());
  }

  public class ExecutionHistoryItem {
    private ExecutionHistoryItemPb mEHItem;

    public ExecutionHistoryItem(ExecutionHistoryItemPb itemPb) {
      mEHItem = itemPb;
    }

    public ExecutionHistoryItemPb toPb() {
      return mEHItem;
    }

    public String getAgentName() {
      return mEHItem.getAgent();
    }

    public int getContextAgentHi() {
      return mEHItem.getParentHi();
    }

    public long getTimeScheduled() {
      return mEHItem.getTimeScheduled();
    }

    public void setTimeScheduled(long timeScheduled) {
      ExecutionHistoryItemPb.Builder itemBuilder = mEHItem.toBuilder();
      itemBuilder.setTimeScheduled(timeScheduled);
      mEHItem = itemBuilder.build();
    }

    public void setTimeTerminated(long timeTerminated) {
      ExecutionHistoryItemPb.Builder itemBuilder = mEHItem.toBuilder();
      itemBuilder.setTimeTerminated(timeTerminated);
      mEHItem = itemBuilder.build();
    }

    public void setExecuted(boolean executed) {
      ExecutionHistoryItemPb.Builder itemBuilder = mEHItem.toBuilder();
      itemBuilder.setExecuted(executed);
      mEHItem = itemBuilder.build();
    }

    public String toString() {
      return PbUtils.toShortDebugString(mEHItem);
    }
  }
}
