// Copyright 2016 Leyantech Ltd. All Rights Reserved.
package ai.iblis.agenda.core.concept;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-12-14
 */
public enum ConceptInContainer {
  // TODO(qli): 之后和Constant里面的Concept merge.
  CONCEPT_USER_QUERY("query"),
  CONCEPT_TASK("task"),
  CONCEPT_TOPIC("topic"),
  CONCEPT_DIRECT_ANS("direct_ans"),
  CONCEPT_DISPATCH_TARGET("dispatch_target"),
  CONCEPT_DISPATCH_TO("dispatch_to"),
  ITEM_LINK("商品链接"),
  HEIGHT("身高"),
  WEIGHT("体重"),
  AGE("年龄"),
  USER_TRADE_INFO("用户订单信息"),
  CONCEPT_USER_PAYMENT_STATUS("用户已经付款"),
  CONCEPT_PAYMENT_TIME("订单付款时间"),
  CONCEPT_HAS_STOCK("被标记有库存商品"),
  CONCEPT_IS_FEATURED("被标记是爆款商品"),
  CONCEPT_IS_PREORDER("被标记是预售商品"),
  CONCEPT_NUM_TIMES_ASK_FOR_DELIVERY("发货催促次数"),
  CONCEPT_DELIVERY_STATUS("发货状态"),
  CONCEPT_SIZE_RECOMMEND_TABLE("尺码推荐表"),
  CONCEPT_SIZE_SPECIFICATION_TABLE("尺码参数表"),
  CONCEPT_KUCUN_QUANTITY_TABLE("库存数量表"),
  CONCEPT_IS_SPLITTED_ORDER("是否拆单"),
  CONCEPT_UNCOMPLETED_ORDERS("未完成订单号"),
  CONCEPT_COMPLETED_ORDERS_IN_FOUR_WEEKS("四周内已完成订单数量"),
  CONCEPT_EXPECTED_LATEST_DELIVERY_TIME("交易最晚预期发货时间"),
  CONCEPT_RECOMMEND_SIZE("推荐尺码"),
  CONCEPT_ITEM_INTRO("链接描述"),
  CONCEPT_ITEM_DESCRIPTION("商品描述"),
  COLOR("颜色分类"),
  SIZE("尺寸"),
  DEAL("优惠"),
  CONCEPT_COUPON("优惠列表"),
  CONCEPT_STOCKED_ITEM_LIST("有货的宝贝列表"),
  CONCEPT_DELIVERY_TABLE("快递列表"),
  CONCEPT_USER_PROVIDED_ORDER_NUMBER("订单号"),
  CONCEPT_NO_STOCK("无库存"),
  CONCEPT_SOLD_OUT("下架"),
  CONCEPT_NO_MATCH_SIZE("没有合适的推荐尺码"),
  CONCEPT_USER_SPECIFIED_TIME("用户指定时间"),
  CONCEPT_DIALOGUE_REPLY_CODE("回复特定话术的原因"),
  CONCEPT_ANCHOR("直播主播名"),
  CONCEPT_HEIGHT_RANGE("身高范围"),
  CONCEPT_WEIGHT_RANGE("体重范围"),
  CONCEPT_ANSWER_TYPE("answer_type"),
  CONCEPT_SPU_TITLE("spu_title"),
  CONCEPT_SENTIMENT("情感极性"),
  CONCEPT_SERVICE_MODE("service_mode"),
  CONCEPT_WANT_RETURN_GOODS("提及退货相关事宜"),
  CONCEPT_WANT_EXCHANGE_GOODS("提及换货相关事宜"),
  CONCEPT_SIGNAL("signal"),
  CONCEPT_REQUEST_PAY_LINK("催付链接"),
  CONCEPT_KBQA_SWITCH("kbqa_switch"),
  CONCEPT_KBQA_REQUEST_INFO("kbqa_request_info"),
  CONCEPT_KBQA_RESPONSE("kbqa_response"),
  CONCEPT_CONTAINS_OVERDUE_ORDERS("含超时订单"),
  CONCEPT_EARLIEST_DELIVERY_TIME("最早发货时间");

  private String mName;

  ConceptInContainer(String name) {
    mName = name;
  }

  public String getName() {
    return mName;
  }
}
