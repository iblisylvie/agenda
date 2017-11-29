// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.concept;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-10-27
 */
public class ConceptConstants {

  public static final String QUERY = "query";
  public static final String SYSTEM_ACTION_TYPE = "system_action_type";
  public static final String USER_PROVIDED_TRADE_ID = "user_provided_trade_id";
  public static final String USER_PROVIDED_SPU_ID = "user_provided_spu_id";
  public static final String TURN_STATUS = "turn_status";  // TURN_START, TURN_MIDDLE, TURN_END.
  public static final String CONVERSATION_STATUS = "conversation_status";  // CONVERSATION_START, CONVERSATION_MIDDLE, CONVERSATION_END.
  public static final String BUNDLE = "bundle";  // query bundle.
  public static final String CANDIDATE_DAS = "candidate_da";
  public static final String DA_IN_PROCESS = "faq_answer_as_proto";
  public static final String REWRITTEN_INTENT = "rewritten_intent";
  public static final String ATTENTION_RESPONE = "attention_response";
  public static final String RECEIVER = "收货信息";
  public static final String ADD_TO_REPLY = "add_to_reply";  // TODO(qli): tmp solution to delete REGIST_QR_EVENT
  public static final String STORE_NICK = "商铺名称";
  public static final String STORE_ID = "store_id";
  public static final String USER_ID = "user_id";
  public static final String ASSISTANT_ID = "assistant_id";
  public static final String SESSION_ID = "session_id";
  public static final String DIALOGUE_ID = "dialogue_id";
  public static final String DOMAIN = "domain";
  public static final String PATTERN = "pattern";
  public static final String DIALOGUE_QUESTION = "dialogue_question_proto";
  public static final String ANCHOR = "anchor";
  public static final String TURN_START_MILLIS = "turn_start_millis";
  public static final String DIALOGUE_STATUS = "dialogue_status";
}
