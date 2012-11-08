/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.express.tests.nonstop;

import org.terracotta.express.tests.base.AbstractToolkitTestBase;
import org.terracotta.toolkit.cache.ToolkitCache;
import org.terracotta.toolkit.nonstop.NonStopConfigFields.NonStopTimeoutBehavior;

import com.tc.test.config.model.TestConfig;

public class NonStopNoOpTest extends AbstractToolkitTestBase {

  public NonStopNoOpTest(TestConfig testConfig) {
    super(testConfig, NonStopNoOpTestClient.class, NonStopNoOpTestClient.class);
    testConfig.getClientConfig().setParallelClients(true);
  }

  public static class NonStopNoOpTestClient extends AbstractNonStopTestClient {
    public NonStopNoOpTestClient(String[] args) {
      super(args);
    }

    public static void main(String[] args) {
      new NonStopNoOpTestClient(args).run();
    }

    @Override
    protected NonStopTimeoutBehavior getTimeoutBehavior() {
      return NonStopTimeoutBehavior.NO_OP;
    }

    @Override
    protected void addToLocalCache(ToolkitCache<Integer, Integer> cache) {
      //
    }
  }

}