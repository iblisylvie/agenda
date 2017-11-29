// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.concept;


import ai.iblis.agenda.agent.AgentDecorator;
import ai.iblis.agenda.core.InfoState;
import org.apache.commons.lang3.StringUtils;
import utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-10-26
 */
public enum SystemConcept {
  CONCEPT_TIME("时间", "getTime");

  private static final Logger LOG = new Logger(SystemConcept.class);

  private String mName;
  private String mMethod;

  SystemConcept(String name, String method) {
    mName = name;
    mMethod = method;
  }

  private void checkMethod() {
    try {
      getClass()
          .getDeclaredMethod(getMethod(), InfoState.class, AgentDecorator.class, String[].class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      LOG.fatal(e, "Failed to find execute routine associated with {}", mMethod);
    }
  }

  public String getName() {
    return mName;
  }

  private String getMethod() {
    return mMethod;
  }

  public String execute() {
    LOG.debug("call method {}.", getMethod());
    try {
      Method m = getClass().getDeclaredMethod(getMethod());
      return (String) m.invoke(this);
    } catch (NoSuchMethodException e) {
      LOG.fatal(e, "Failed to find execute routine associated with {}", getMethod());
    } catch (InvocationTargetException e) {
      LOG.fatal(e, "Error occurred during the execution of {}", getMethod());
    } catch (IllegalAccessException e) {
      LOG.fatal(e, "Illegal access to method {}", getMethod());
    }
    return null;
  }

  public static SystemConcept getConcept(String name) {
    if (StringUtils.isEmpty(name)) return null;
    SystemConcept[] concepts = values();
    for (SystemConcept concept : concepts) {
      if (name.equals(concept.getName())) {
        return concept;
      }
    }
    return null;
  }

  private String getTime() {
    LOG.debug("getTime execute.");
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String time = df.format(date);
    return time;
  }

  public static void main(String[] args) {
    System.out.println(SystemConcept.CONCEPT_TIME.execute());
  }
}
