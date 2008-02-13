/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tctest.statistics;

import com.tc.statistics.StatisticData;
import com.tc.statistics.buffer.StatisticsConsumer;
import com.tc.statistics.config.StatisticsConfig;
import com.tc.statistics.gatherer.StatisticsGatherer;
import com.tc.statistics.gatherer.impl.StatisticsGathererImpl;
import com.tc.statistics.retrieval.StatisticsRetriever;
import com.tc.statistics.store.StatisticsRetrievalCriteria;
import com.tc.statistics.store.StatisticsStore;
import com.tc.statistics.store.h2.H2StatisticsStoreImpl;
import com.tctest.TransparentTestBase;
import com.tctest.TransparentTestIface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StatisticsGathererConfigSampleRateTest extends TransparentTestBase {
  protected void duringRunningCluster() throws Exception {
    File tmp_dir = makeTmpDir(getClass());

    StatisticsStore store = new H2StatisticsStoreImpl(tmp_dir);
    StatisticsGatherer gatherer = new StatisticsGathererImpl(store);

    gatherer.connect("localhost", getAdminPort());

    gatherer.createSession();
    gatherer.enableStatistics(gatherer.getSupportedStatistics());
    gatherer.startCapturing();
    Thread.sleep(10000);
    gatherer.stopCapturing();
    Thread.sleep(5000);
    gatherer.closeSession();

    final List data_list1 = new ArrayList();
    store.retrieveStatistics(new StatisticsRetrievalCriteria(), new StatisticsConsumer() {
      public boolean consumeStatisticData(StatisticData data) {
        data_list1.add(data);
        return true;
      }
    });

    gatherer.setGlobalParam(StatisticsConfig.KEY_GLOBAL_SAMPLE_PERIOD, new Long(StatisticsRetriever.DEFAULT_GLOBAL_SAMPLE_PERIOD.longValue() / 2));
    gatherer.createSession();
    gatherer.enableStatistics(gatherer.getSupportedStatistics());
    gatherer.startCapturing();
    Thread.sleep(10000);
    gatherer.stopCapturing();
    Thread.sleep(5000);
    gatherer.closeSession();

    final List data_list2 = new ArrayList();
    store.retrieveStatistics(new StatisticsRetrievalCriteria(), new StatisticsConsumer() {
      public boolean consumeStatisticData(StatisticData data) {
        data_list2.add(data);
        return true;
      }
    });

    assertTrue(data_list1.size() * 2 <= data_list2.size());

    gatherer.disconnect();
  }

  protected Class getApplicationClass() {
    return StatisticsGathererTestApp.class;
  }

  public void doSetUp(TransparentTestIface t) throws Exception {
    t.getTransparentAppConfig().setClientCount(StatisticsGathererTestApp.NODE_COUNT);
    t.initializeTestRunner();
  }
}