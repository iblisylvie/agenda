// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.agent;

import ai.iblis.agenda.core.InfoState;

/**
 * Agent provide a prompt to the user to show some result.
 *
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-13.
 */
public class Agent extends AgentDecorator {

  public Agent(ai.iblis.agenda.Agent agent) {
    super(agent);
  }

  @Override
  public ExecuteResult execute(InfoState infoState) {
    return new ExecuteResult(getName(), toPb().getDerc(), true, null);
  }
}
