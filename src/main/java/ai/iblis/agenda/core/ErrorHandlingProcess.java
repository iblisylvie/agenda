// Copyright 2016 Leyantech Ltd. All Rights Reserved.
package ai.iblis.agenda.core;

import com.leyantech.ai.agenda.agent.AgentDecorator;
import com.leyantech.utility.Logger;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-08.
 */
public class ErrorHandlingProcess {
  private static final Logger LOG = new Logger(ErrorHandlingProcess.class);

  // Deque as a queue.
  private Deque<ErrorHandler> mErrorHandlers = new ArrayDeque<>();

  public int process(InfoState infoState) {
    int errorCount = 0;
    for (ErrorHandler errorHandler : mErrorHandlers) {
      int pusherHistoryIndex = -1;
      if (errorHandler.mPusher != null) {
        pusherHistoryIndex = errorHandler.mPusher.getHistoryIndex();
      }
      infoState.pushDAOnStack(pusherHistoryIndex, errorHandler.mAgent);
      LOG.debug("Agent {} push handler {} onto the stack",
          errorHandler.mPusher == null ? "null" : errorHandler.mPusher.getName(),
          errorHandler.mAgent.getName());
      ++errorCount;
    }
    mErrorHandlers.clear();
    return errorCount;
  }

  public void regist(ErrorHandler errorHandler) {
    mErrorHandlers.add(errorHandler);
  }

  public ErrorHandler createNewErrorHandler(AgentDecorator pusher, AgentDecorator agent) {
    ErrorHandler errorHandler = new ErrorHandler();
    errorHandler.mPusher = pusher;
    errorHandler.mAgent = agent;
    return errorHandler;
  }

  public class ErrorHandler {
    public AgentDecorator mPusher;
    public AgentDecorator mAgent;
  }
}
