// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.script;

import com.leyantech.ai.ConversationStatus;
import com.leyantech.ai.agenda.context.ThreadContext;
import com.leyantech.ai.agenda.core.concept.ConceptConstants;
import com.leyantech.ai.agenda.utils.AgendaUtils;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-10-28
 */
public class ConversationStart extends ZeroArgFunction {

  @Override
  public LuaValue call() {
    String value = ThreadContext.getWorkingInfoState().getNormValue(ConceptConstants.CONVERSATION_STATUS);
    ConversationStatus conversationStatus = AgendaUtils.toConversationStatus(value);
    boolean isConversationStart = ConversationStatus.CONVERSATION_START.equals(conversationStatus);
    return LuaValue.valueOf(isConversationStart);
  }
}
