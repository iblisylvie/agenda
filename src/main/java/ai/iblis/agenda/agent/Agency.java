// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.agent;

import static ai.iblis.agenda.Agent.DialogueExecutionReturnCode.DERC_CONTINUE_EXECUTION;

import ai.iblis.agenda.core.DialogueExecutionFailCode;
import ai.iblis.agenda.core.InfoState;
import utils.Logger;

/**
 * An agency is an agent which contains multiple sub agents.
 *
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-11.
 */
public class Agency extends AgentDecorator {

  private static final Logger LOG = new Logger(Agency.class);

  public Agency(Agent agent) {
    super(agent);
  }

  @Override
  public ExecuteResult execute(InfoState infoState) {
    // push sub agent with satisfied condition onto the stack.
    for (AgentDecorator subAgent : getSubAgents()) {
      subAgent.setContextAgent(this);  // set the scheduler in potential.
      if (subAgent.hasCompleted(infoState)) {
        LOG.debug("agent {} has completed.", subAgent.getName());
        continue;
      }
      if (!subAgent.preconditionsSatisfied(infoState)) {
        continue;
      }
      infoState.pushDAOnStack(getHistoryIndex(), subAgent);
      String trace = subAgent.getName();  // agency's trace is next agent.
      return new ExecuteResult(trace, DERC_CONTINUE_EXECUTION, true, null);
    }
    String trace = onError(infoState, DialogueExecutionFailCode.DIALOGUE_INTERNAL_EXCEPTION +
        "Failed to found next executable agent in agency " + getName());
    LOG.info("Failed to found next executable agent in agency {}, use error handler {}", getName(),
        trace);
    return new ExecuteResult(trace, DERC_CONTINUE_EXECUTION, true, null);
  }

  @Override
  public void reset() {
    for (AgentDecorator subAgent : getSubAgents()) {
      subAgent.reset();
    }
  }
}
