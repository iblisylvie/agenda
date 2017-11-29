// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core.dtt;

import com.leyantech.ai.DTT;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author qli, <qli@leyantech.com>
 * @date 2017-11-21
 */
public class TaskTreeBuildingMaterials {

  String mDttRes;
  List<DTT> mDTTs;
  String mRootDttName;
  String mRootAgentName;

  public TaskTreeBuildingMaterials(String dttRes, String rootDttName, String rootAgent,
      List<DTT> dtts) {
    Preconditions.checkNotNull(dttRes);
    Preconditions.checkNotNull(rootDttName);
    Preconditions.checkNotNull(rootAgent);
    Preconditions.checkNotNull(dtts);

    mDttRes = dttRes;
    mDTTs = dtts;
    mRootAgentName = rootAgent;
    mRootDttName = rootDttName;
  }

  public String getRootDttName() {
    return mRootDttName;
  }

  public List<DTT> getDtts() {
    return mDTTs;
  }

  public String getRootAgentName() {
    return mRootAgentName;
  }
}
