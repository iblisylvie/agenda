// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.utils;

import ai.iblis.agenda.Agent.AgentScope;
import ai.iblis.agenda.Agent.AgentStatus;
import ai.iblis.agenda.ConversationStatus;
import ai.iblis.agenda.TurnStatus;
import org.apache.commons.lang3.StringUtils;
import utils.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-10-23
 */
public class AgendaUtils {
  private static final Logger LOG = new Logger(AgendaUtils.class);

  public static String getStackTrace() {
    StringWriter sw = new StringWriter();
    new Throwable("").printStackTrace(new PrintWriter(sw));
    String stackTrace = sw.toString();
    return stackTrace;
  }

  public static String getCallerInfo() {
    StackTraceElement stack[] = Thread.currentThread().getStackTrace();
    StringBuilder sb = new StringBuilder();
    int maxDepth = 4;
    maxDepth = stack.length > maxDepth ? maxDepth : stack.length;
    for (int i = 3; i < maxDepth; ++i) {
      sb.append(stack[i]).append("\n");
    }
    return sb.toString();
  }

  public static String toString(TurnStatus turnStatus) {
    return turnStatus.name();
  }

  public static TurnStatus toTurnStatus(String value) {
    if (StringUtils.isEmpty(value)) {
      return TurnStatus.INVALID_TURN_STATUS;
    }
    try {
      return TurnStatus.valueOf(value);
    } catch (Exception e) {
      LOG.warn("Failed to turn value {} to TurnStatus.", value);
      return TurnStatus.INVALID_TURN_STATUS;
    }
  }

  public static String toString(ConversationStatus conversationStatus) {
    return conversationStatus.name();
  }

  public static ConversationStatus toConversationStatus(String value) {
    if (StringUtils.isEmpty(value)) {
      return ConversationStatus.INVALID_CONVERSATION_STATUS;
    }

    try {
      return ConversationStatus.valueOf(value);
    } catch (Exception e) {
      LOG.warn("Failed to turn value {} to ConversationStatus.", value);
      return ConversationStatus.INVALID_CONVERSATION_STATUS;
    }
  }

  // If the given AgentStatus hyp is equal to any of AgentStatus ref.
  public static boolean in(AgentStatus hyp, AgentStatus... ref) {
    if (hyp == null) {
      return false;
    }
    return Arrays.stream(ref).filter(refAs -> hyp.equals(refAs)).count() > 0;
  }

  // If the given AgentScope is equal to any of AgentScope ref.
  public static boolean in(AgentScope hyp, AgentScope... ref) {
    if (hyp == null) {
      return false;
    }
    return Arrays.stream(ref).filter(refAs -> hyp.equals(refAs)).count() > 0;
  }
}
