// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.dtt;

import com.leyantech.ai.Agent;
import com.leyantech.ai.Concept;
import com.leyantech.ai.DTT;
import com.leyantech.ai.agenda.agent.AgentDecorator;
import com.leyantech.ai.agenda.agent.AgentDecoratorFactory;
import com.leyantech.ai.agenda.core.concept.ConceptContainer;
import com.leyantech.ai.customization.utils.CollectionUtils;
import com.leyantech.utility.Logger;
import com.leyantech.utility.PbUtils;
import com.leyantech.utility.StringUtils;

import com.google.common.base.Preconditions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-11-02
 */
public class DialogueTaskTree {

  private static final Logger LOG = new Logger(DialogueTaskTree.class);

  protected List<DTT> mDTTs = new ArrayList<>();
  protected DTT mRootDTT = null;
  protected AgentDecorator mRootAgent = null;
  protected String mDomain = null;
  protected String mName = null;
  protected Map<String, AgentDecorator> mAgentMap = new HashMap<>();
  protected ConceptContainer mConceptContainer;

  public DialogueTaskTree() {
  }

  /**
   * @param dtts this should be unique.
   */
  public void build(String rootDttName, List<DTT> dtts, String rootAgent) {
    Preconditions.checkArgument(dtts != null && !dtts.isEmpty());

    setupDTTs(dtts);

    setupRootDTT(rootDttName, dtts);

    setupDomain(rootDttName, dtts);

    setupName(rootDttName, dtts);

    setupAgentMap(dtts, rootAgent);

    // set up relations in agent map start from root agent.
    buildRelation(mDomain, mRootAgent, mAgentMap);

    buildNonRootRelation(mDomain, mAgentMap);

    // set up concept container.
    setupConcepts();
  }

  protected void setupDTTs(List<DTT> dtts) {
    mDTTs = dtts;
  }

  protected void setupRootDTT(String rootDttName, List<DTT> dtts) {
    List<DTT> rootDtts = dtts.stream().filter(dtt -> dtt.getName().equals(rootDttName))
        .collect(Collectors.toList());
    if (!CollectionUtils.isNullOrEmpty(rootDtts)) {
      this.mRootDTT = rootDtts.get(0);
    }
    Preconditions.checkNotNull(mRootDTT);
  }

  protected void setupDomain(String rootDttName, List<DTT> dtts) {
    List<DTT> rootDtts = dtts.stream().filter(dtt -> dtt.getName().equals(rootDttName))
        .collect(Collectors.toList());
    if (!CollectionUtils.isNullOrEmpty(rootDtts)) {
      mDomain = rootDtts.get(0).getDomain();
    }
    Preconditions.checkArgument(!StringUtils.isEmpty(mDomain));
  }

  protected void setupName(String rootDttName, List<DTT> dtts) {
    List<DTT> rootDtts = dtts.stream().filter(dtt -> dtt.getName().equals(rootDttName))
        .collect(Collectors.toList());
    if (!CollectionUtils.isNullOrEmpty(rootDtts)) {
      mName = rootDtts.get(0).getName();
    }
    Preconditions.checkArgument(!StringUtils.isEmpty(mName));
  }

  protected void setupAgentMap(List<DTT> dtts, String rootAgent) {
    // set up agent map.
    for (DTT v : dtts) {
      mAgentMap.putAll(loadAgentMap(v));
    }
    mRootAgent = mAgentMap.get(rootAgent);
    Preconditions.checkNotNull(mRootAgent);
  }

  protected void setupConcepts() {
    List<Concept> allConcept = new ArrayList<>();
    mDTTs.forEach(v -> {
      allConcept.addAll(v.getConceptList());
    });
    mConceptContainer = new ConceptContainer(allConcept);
  }

  // for example, agents from system config.
  protected void buildNonRootRelation(String domain, Map<String, AgentDecorator> allAgents) {
    allAgents.forEach((adn, ad) -> {
      if (ad.toPb().getSubAgentCount() != ad.getSubAgents().size()) {
        buildRelation(domain, ad, allAgents);
      }
    });
  }

  protected void buildRelation(String domain, AgentDecorator rootAgent,
      Map<String, AgentDecorator> allAgents) {
    Set<String> visited = new HashSet<>();
    Deque<AgentDecorator> queue = new ArrayDeque<>();
    queue.add(rootAgent);
    while (!queue.isEmpty()) {
      AgentDecorator parent = queue.poll();
      parent.setDomain(domain);
      for (String agentName : parent.toPb().getSubAgentList()) {
        AgentDecorator son = allAgents.get(agentName);
        if (son == null) {
          LOG.fatal("agent {} doesn't exist", agentName);
        }
        if (!visited.contains(son.getName())) {
          queue.add(son);
          visited.add(agentName);
        }
        parent.addSubAgent(son);
      }
    }
  }

  public DTT toDTT() {
    DTT.Builder dttBuilder = DTT.newBuilder();
    dttBuilder.setName(mName);
    dttBuilder.setRootAgent(mRootAgent.getName());
    dttBuilder.setDomain(mDomain);
    mAgentMap.forEach((k, v) -> {
      dttBuilder.addAgent(v.toPb());
    });
    dttBuilder.addAllConcept(mConceptContainer.toPb());
    return dttBuilder.build();
  }

  // TODO(qli): 是否需要把树的路径作为这个key呢？
  protected Map<String, AgentDecorator> loadAgentMap(DTT dtt) {
    Map<String, AgentDecorator> agentMap = new HashMap<>();
    for (Agent agent : dtt.getAgentList()) {
      if (agentMap.containsKey(agent.getName())) {
        LOG.fatal("Agent {} has multiple definition in dtt {}", agent.getName(),
            PbUtils.toShortDebugString(dtt));
      }
      agentMap.put(agent.getName(), AgentDecoratorFactory.getInstance().newInstance(agent));
    }
    return agentMap;
  }

  public AgentDecorator getRootAgent() {
    return mRootAgent;
  }

  public DTT getRootDTT() {
    return mRootDTT;
  }

  public List<DTT> getAllDTTs() {
    return mDTTs;
  }

  public String getName() {
    return mName;
  }

  public AgentDecorator getAgent(String agentName) {
    if (StringUtils.isEmpty(agentName)) {
      return null;
    }
    if (!mAgentMap.containsKey(agentName)) {
      LOG.warn("{} is not defined.", agentName);
      return null;
    }
    return mAgentMap.get(agentName);
  }

  public Collection<AgentDecorator> getAllAgents() {
    return mAgentMap.values();
  }

  public ConceptContainer getConceptContainer() {
    return mConceptContainer;
  }
}
