// Copyright 2016 Leyantech Ltd. All Rights Reserved.
package ai.iblis.agenda.core;

import ai.iblis.agenda.utils.DttUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-27
 */
public enum SystemAgent {
  HELP_AGENT("HelpAgent", "uri:/system.flow/common.dtt"),
  HANDOVER_TO_HUMAN_AGENT("H2HAgent", "uri:/system.flow/common.dtt"),
  CONCEPT_PREPROCESSOR("ConceptPreprocessor", "uri:/system.flow/common.dtt"),
  REQUEST_ITEM_LINK("要求提供商品链接", "uri:/system.flow/common.dtt");

  private String mAgentName;
  private String mDttRes;

  SystemAgent(String agentName, String dttRes) {
    mAgentName = agentName;
    mDttRes = dttRes;
  }

  public String getAgentName() {
    return mAgentName;
  }

  public String getDTTRes() {
    return mDttRes;
  }

  public static Map<String, DTT> getSystemDTT() {
    Set<String> systemDTTRes =
        Arrays.stream(SystemAgent.values()).map(SystemAgent::getDTTRes).collect(Collectors.toSet());
    return DttUtils.getImportedDTT(new ArrayList<>(systemDTTRes));
  }
}