// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.agent;

import com.leyantech.ai.DTT;
import com.leyantech.ai.agenda.utils.DttUtils;
import com.leyantech.utility.Logger;
import com.leyantech.utility.PbUtils;
import com.leyantech.utility.ReflectionUtils;
import com.leyantech.utility.StringUtils;
import com.leyantech.utility.exception.ReflectionException;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-07-11.
 */
public class AgentDecoratorFactory {

  private static final Logger LOG = new Logger(AgentDecoratorFactory.class);
  public static final String AGENT = "AGENT";
  public static final String AGENCY = "AGENCY";
  private static final String CODEBASE = "CODEBASE";
  // r: name c: domain v: class name.
  private HashBasedTable<String, String, String> mNameToClassName = HashBasedTable.create();
  // r: agent type c: domain v: class name.
  private HashBasedTable<String, String, String> mTypeToClassName = HashBasedTable.create();

  // Singleton Initialization.
  private static final class SingletonHolder {

    private static final AgentDecoratorFactory mInstance = new AgentDecoratorFactory();
  }

  private AgentDecoratorFactory() {
    try {
      LOG.debug("Init agent from codebase.");
      initFromCodebase();
    } catch (IOException e) {
      LOG.fatal(e, String.format("Failed to find all agents under %s.", getClass().getName()));
    }
  }

  private void initFromCodebase() throws IOException {
    // init name to class map.
    String packageName = getClass().getPackage().getName();
    Set<Class<?>> allClasses = ReflectionUtils.getClasses(packageName);
    Class[] paramType = {com.leyantech.ai.Agent.class};
    Object[] param = {com.leyantech.ai.Agent.newBuilder().build()};
    allClasses.forEach(cls -> {
      try {
        Object obj = ReflectionUtils.newInstanceFromClassName(cls.getName(), paramType, param);
        if (obj instanceof AgentDecorator) {
          // No domain name available in this moment.
          regist(cls);
        }
      } catch (ReflectionException e) {
      }
    });

    // init type to class map.
    regist(AGENT, Agent.class);
    regist(AGENCY, Agency.class);
  }

  public synchronized static AgentDecoratorFactory getInstance() {
    return SingletonHolder.mInstance;
  }

  public Set<String> getAllRegisteredAgentName() {
    return mNameToClassName.rowKeySet();
  }

  public void regist(Class cls) {
    mNameToClassName.put(cls.getSimpleName(), CODEBASE, cls.getName());
    LOG.debug("Register agent {} with class {}.", cls.getSimpleName(), cls.getName());
  }

  // Register agent type's default impl class so as to use agent type to init agent without specific
  // impl.
  public void regist(String type, Class cls) {
    mTypeToClassName.put(type, CODEBASE, cls.getName());
    LOG.debug("Register agent type {} with class {}.", type, cls.getName());
  }

  public void regist(DTT dtt, String domain) {
    if (!StringUtils.isEmpty(dtt.getDomain()) && !dtt.getDomain().equals(domain)) {
      LOG.fatal("Domain defined in dtt {}, request domain {}, these two should be consistent.",
          dtt.getDomain(), domain);
    }
    registImportDTT(dtt, domain);
    for (com.leyantech.ai.Agent agent : dtt.getAgentList()) {
      if (mNameToClassName.contains(agent.getName(), CODEBASE)) {
        registWithSpecificClass(agent.getName(), domain);
      } else {
        registWithDefaultClass(agent.getName(), agent.getType(), domain);
      }
    }
    checkLoop(dtt, domain);
    checkInitializability(dtt, domain);
  }

  // 检查该dtt中定义的所有的agent都是可被实例化的
  public void checkInitializability(DTT dtt, String domain) {
    Set<String> declearedAgents = new HashSet<>();
    dtt.getAgentList().forEach(agent -> {
      declearedAgents.add(agent.getName());
      agent.getSubAgentList().forEach(subAgentName -> {
        declearedAgents.add(subAgentName);
      });
    });

    Set<String> existedAgent = new HashSet<>();
    declearedAgents.forEach(agentName -> {
      if (existedAgent.contains(agentName)) {
        LOG.fatal("Agent {} has multiple definition in dtt {}", agentName,
            PbUtils.toShortDebugString(dtt));
      }
      if (mNameToClassName.containsRow(agentName)) {
        existedAgent.add(agentName);
      } else {
        LOG.fatal(String.format("Agent %s is not defined in the dialogue task tree.", agentName));
      }
    });
  }

  private void checkLoop(DTT dtt, String domain) {
    Set<String> dttAgents = new HashSet<>();
    for (com.leyantech.ai.Agent agent : dtt.getAgentList()) {
      if (dttAgents.contains(agent.getName())) {
        LOG.fatal("Loop detected in dtt {} because of agent {}.", domain, agent.getName());
      } else {
        dttAgents.add(agent.getName());
      }
    }
  }

  private void registImportDTT(DTT dtt, String domain) {
    Map<String, DTT> importedDTT = DttUtils.getImportedDTT(dtt.getImportList());
    if (importedDTT != null && !importedDTT.isEmpty()) {
      importedDTT.forEach((k, s) -> {
        // TODO(qli): SYSTEM deserves a second thought.
        if (!s.getDomain().equals(domain) && !"SYSTEM".equals(s.getDomain())) {
          LOG.fatal(
              "Some imported dtts in {} has different domain scope {} than the caller {}, pls check.",
              dtt.getImportList(), s.getDomain(), domain);
        }
        regist(s, s.getDomain());
      });
    }
  }

  private void registWithSpecificClass(String agentName, String domain) {
    String specificClass = mNameToClassName.get(agentName, CODEBASE);
    Preconditions.checkArgument(!StringUtils.isEmpty(specificClass));
    mNameToClassName.put(agentName, domain, specificClass);
  }

  // 例如InformAgent, Agency, RequestAgent如果没有显示定义类实现，则使用默认的类
  private void registWithDefaultClass(String agentName, String agentType, String domain) {
    if (!mTypeToClassName.containsRow(agentType)) {
      LOG.fatal("No agent impl for agent {} type {} domain {}.", agentName, agentType, domain);
    }
    String implClassName = mTypeToClassName.get(agentType, CODEBASE);
    if (StringUtils.isEmpty(implClassName)) {
      LOG.fatal("No agent impl for agent {} type {} domain {}.", agentName, agentType, domain);
    }
    mNameToClassName.put(agentName, domain, implClassName);
    LOG.debug("Register agent {} type {} domain {} with class {}", agentName, agentType, domain,
        implClassName);
  }

  public String printAllAvailableAgentToString() {
    StringBuilder sb = new StringBuilder();
    mNameToClassName.rowMap().forEach((r, c) -> {
      sb.append(r).append("\t");
      c.forEach((k, v) -> {
        sb.append(k).append("\t").append(v).append("\t").append("\n");
      });
    });
    return sb.toString();
  }

  public AgentDecorator newInstance(com.leyantech.ai.Agent agent) {
    if (!mNameToClassName.containsRow(agent.getName())) {
      throw new RuntimeException(String
          .format("The requested agent %s is not available in the agent factory.",
              agent.getName()));
    }
    Class[] paramType = {com.leyantech.ai.Agent.class};
    Object[] param = {agent};
    AgentDecorator agentDecorator = null;
    String implClassName =
        new ArrayList<>(mNameToClassName.row(agent.getName()).values()).get(0);
    try {
      agentDecorator = (AgentDecorator) ReflectionUtils
          .newInstanceFromClassName(implClassName, paramType, param);
    } catch (ReflectionException e) {
      LOG.fatal(e,
          "Not supposed to happen, we have checked {}'s instantiation with class {} when this factory is init.",
          agent.getName(), implClassName);
    }
    return agentDecorator;
  }

  public static void main(String[] args) {
    AgentDecoratorFactory.getInstance();
  }
}
