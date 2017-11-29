// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.script;

import com.leyantech.ai.agenda.agent.AgentDecorator;
import com.leyantech.ai.agenda.context.ThreadContext;
import com.leyantech.ai.agenda.core.InfoState;
import com.leyantech.utility.Logger;
import com.leyantech.utility.StringUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Not thread safe, each thread should have one
 *
 * @author Qian Li, <qli@leyantech.com>
 * @date 2017-01-26
 */

public class LuaInterpreter {

  private static final Logger LOG = new Logger(LuaInterpreter.class);

  private static final String EVALUATE_EXPRESSION_FORMAT = "return (%s)";

  private Globals mGlobals;

  private LuaInterpreter() {
    mGlobals = JsePlatform.standardGlobals();

    regist("TURN_START", new TurnStart());
    regist("TURN_END", new TurnEnd());
    regist("CONVERSATION_START", new ConversationStart());
  }

  // Singleton Initialization.
  private static final class SingletonHolder {

    private static final LuaInterpreter mInstance = new LuaInterpreter();
  }

  public static LuaInterpreter getInstance() {
    return LuaInterpreter.SingletonHolder.mInstance;
  }

  public void regist(String funcName, LuaValue luaFunc) {
    mGlobals.set(funcName, luaFunc);
  }

  public boolean evaluate(InfoState infoState, AgentDecorator agentDecorator, String expression) {
    LuaValue result = evaluateLuaExpression(infoState, agentDecorator, expression);
    if (LuaValue.NIL.equals(result)) {
      return false;
    }
    try {
      return result.checkboolean();
    } catch (LuaError e) {
      LOG.error(e, "[evaluate] Failed to evaluate {}.", expression);
      ThreadContext.getWorkingAgent()
          .onError(ThreadContext.getWorkingInfoState(), "Failed to evaluate " + expression);
      return false;
    }
  }

  public LuaValue evaluateLuaExpression(InfoState workingInfoState, AgentDecorator workingAgent,
      String expression) {
    if (StringUtils.isEmpty(expression)) {
      return null;
    }
    // TODO(qli): back compatibility, get rid of this later.
    if (!expression.contains("return")) {
      expression = String.format(EVALUATE_EXPRESSION_FORMAT, expression);
    }
    Pair<InfoState, AgentDecorator> previousSpot = ThreadContext
        .continueWith(workingInfoState, workingAgent);
    try {
      LuaValue chunk = mGlobals.load(expression);
      LuaValue result = chunk.call();
      return result;
    } catch (Throwable t) {
      LOG.error(t, "[evaluateLuaExpression] Failed to evaluate {}", expression);
      ThreadContext.getWorkingAgent()
          .onError(ThreadContext.getWorkingInfoState(), "Failed to evaluate " + expression);
      return LuaValue.NIL;
    } finally {
      ThreadContext.finishWith(previousSpot, workingInfoState, workingAgent);
    }
  }
}
