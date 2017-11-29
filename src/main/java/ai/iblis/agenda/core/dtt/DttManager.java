// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.dtt;

import ai.iblis.agenda.DTT;
import ai.iblis.agenda.agent.AgentDecoratorFactory;
import ai.iblis.agenda.core.SystemAgent;
import ai.iblis.agenda.utils.DttUtils;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import utils.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-11-21
 */
public class DttManager {

  private static final Logger LOG = new Logger(DttManager.class);

  private static final class SingletonHolder {

    private static final DttManager mInstance = new DttManager();
  }

  private DttManager() {
  }

  public static DttManager getInstance() {
    return DttManager.SingletonHolder.mInstance;
  }

  // key: domain.
  // value: store pre-configured dtt res file uri.
  private Map<String, TaskTreeBuildingMaterials> dtts = new HashMap<>();

  public void regist(String key, String dttRes) {
    registAgent(dttRes);
    dtts.put(key, loadDTTBuildingMaterials(dttRes));
  }

  public TaskTreeBuildingMaterials getBuildingMaterials(String key) {
    return this.dtts.get(key);
  }

  private void registAgent(String dttRes) {
    // Load dtt.
    DTT.Builder dttBuilder = DTT.newBuilder();
    try (InputStream is = DttManager.class.getResourceAsStream(dttRes)) {
      DttUtils.loadFromYaml(is, dttBuilder);
      // Regist agents.
      Preconditions.checkArgument(!StringUtils.isEmpty(dttBuilder.getDomain()));
      AgentDecoratorFactory.getInstance().regist(dttBuilder.build(), dttBuilder.getDomain());
    } catch (Exception e) {
      // unit test dtt is not available. this is normal.
      LOG.debug(e, "Failed to load DTT from {}.", dttRes);
    }
  }

  private TaskTreeBuildingMaterials loadDTTBuildingMaterials(String dttRes) {
    // Load dtt.
    DTT.Builder dttBuilder = DTT.newBuilder();
    try (InputStream is = DttManager.class.getResourceAsStream(dttRes)) {
      DttUtils.loadFromYaml(is, dttBuilder);
    } catch (Exception e) {
      LOG.fatal(e, "Failed to load DTT from {}.", dttRes);
    }
    String rootDttName = dttBuilder.getName();
    Map<String, DTT> dtts = new HashMap<>();
    dtts.put(dttRes, dttBuilder.build());  // add root
    dtts.putAll(DttUtils.getAllImportedDTT(dttBuilder.build()));
    dtts.putAll(SystemAgent.getSystemDTT());
    return new TaskTreeBuildingMaterials(dttRes, rootDttName, dttBuilder.getRootAgent(),
        new ArrayList<>(dtts.values()));
  }
}
