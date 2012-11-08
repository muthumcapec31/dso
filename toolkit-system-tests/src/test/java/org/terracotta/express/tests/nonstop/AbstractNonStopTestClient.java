/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.express.tests.nonstop;

import org.terracotta.express.tests.base.NonStopClientBase;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.cache.ToolkitCache;
import org.terracotta.toolkit.cache.ToolkitCacheConfigBuilder;
import org.terracotta.toolkit.concurrent.ToolkitBarrier;
import org.terracotta.toolkit.nonstop.NonStopConfigBuilder;
import org.terracotta.toolkit.nonstop.NonStopConfigFields.NonStopTimeoutBehavior;
import org.terracotta.toolkit.nonstop.NonStopException;

import java.util.Date;

import junit.framework.Assert;

public abstract class AbstractNonStopTestClient extends NonStopClientBase {
  protected static final int  CLIENT_COUNT            = 2;
  protected static final int  NUMBER_OF_ELEMENTS      = 10;
  protected static final int  MAX_ENTRIES_LOCAL_HEAP  = 0;
  protected static final long NON_STOP_TIMEOUT_MILLIS = 10000;

  public AbstractNonStopTestClient(String[] args) {
    super(args);
  }

  @Override
  protected void test(Toolkit toolkit) throws Throwable {
    ToolkitBarrier barrier = toolkit.getBarrier("testBarrier", CLIENT_COUNT);
    int index = barrier.await();
    ToolkitCache<Integer, Integer> cache = null;
    cache = createCache(toolkit);

    if (index == 0) {
      for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
        cache.put(i, i);
      }
      System.err.println("Cache size " + cache.size() + " at " + new Date());
    }

    barrier.await();

    if (index == 1) {
      addToLocalCache(cache);

      makeServerDie();

      for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
        boolean exceptionOccurred = false;
        long time = System.currentTimeMillis();
        try {
          Integer intValue = cache.get(i);
          checkOnReturnValue(i, intValue);
        } catch (NonStopException e) {
          exceptionOccurred = true;
        }

        time = System.currentTimeMillis() - time;
        Assert.assertTrue((time > (NON_STOP_TIMEOUT_MILLIS - 500)) && (time < (NON_STOP_TIMEOUT_MILLIS + 2000)));
        System.err.println("Time consumed " + time);

        checkNonStopExceptionOnReads(exceptionOccurred);
      }

      restartCrashedServer();
    }
  }

  protected void addToLocalCache(ToolkitCache<Integer, Integer> cache) {
    for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
      long time = System.currentTimeMillis();
      try {
        Assert.assertNotNull(cache.get(i));
      } catch (NonStopException e) {
        System.err.println("Time elapsed " + (System.currentTimeMillis() - time) + " , i=" + i);
        throw e;
      }
    }
  }

  private void checkOnReturnValue(Integer expected, Integer actual) {
    switch (getTimeoutBehavior()) {
      case EXCEPTION_ON_TIMEOUT:
        throw new AssertionError("Expected " + expected + " , actual " + actual + ". But no value should have come.");
      case EXCEPTION_ON_MUTATE_AND_LOCAL_READS:
      case LOCAL_READS:
        Assert.assertEquals(expected, actual);
        break;
      case NO_OP:
        Assert.assertNull(actual);
        break;
    }
  }

  private void checkNonStopExceptionOnReads(boolean exceptionOccurred) {
    switch (getTimeoutBehavior()) {
      case EXCEPTION_ON_TIMEOUT:
        Assert.assertTrue(exceptionOccurred);
        break;
      case EXCEPTION_ON_MUTATE_AND_LOCAL_READS:
      case LOCAL_READS:
      case NO_OP:
        Assert.assertFalse(exceptionOccurred);
        break;
    }
  }

  private void restartCrashedServer() throws Exception {
    getTestControlMbean().reastartLastCrashedServer(0);
  }

  private void makeServerDie() throws Exception {
    getTestControlMbean().crashActiveServer(0);
    Thread.sleep(10 * 1000);
  }

  private ToolkitCache createCache(Toolkit toolkit) {
    String cacheName = "test-cache";

    new NonStopConfigBuilder().timeoutMillis(NON_STOP_TIMEOUT_MILLIS).nonStopTimeoutBehavior(getTimeoutBehavior())
        .apply(toolkit);

    ToolkitCacheConfigBuilder builder = new ToolkitCacheConfigBuilder();
    builder.maxCountLocalHeap(MAX_ENTRIES_LOCAL_HEAP);

    addMoreConfigToBuilder(builder);

    return toolkit.getCache(cacheName, builder.build(), Integer.class);
  }

  protected void addMoreConfigToBuilder(ToolkitCacheConfigBuilder builder) {
    //
  }

  protected abstract NonStopTimeoutBehavior getTimeoutBehavior();
}