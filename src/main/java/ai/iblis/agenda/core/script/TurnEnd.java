// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.script;

import com.leyantech.ai.TurnStatus;
import com.leyantech.ai.agenda.context.ThreadContext;
import com.leyantech.ai.agenda.core.concept.ConceptConstants;
import com.leyantech.ai.agenda.utils.AgendaUtils;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-10-28
 */
public class TurnEnd extends ZeroArgFunction {

  @Override
  public LuaValue call() {
    String value = ThreadContext.getWorkingInfoState().getNormValue(ConceptConstants.TURN_STATUS);
    TurnStatus turnStatus = AgendaUtils.toTurnStatus(value);
    boolean isTurnStart = TurnStatus.TURN_END.equals(turnStatus);
    return LuaValue.valueOf(isTurnStart);
  }
}
