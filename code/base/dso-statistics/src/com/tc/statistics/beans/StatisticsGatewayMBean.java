/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.statistics.beans;

public interface StatisticsGatewayMBean extends StatisticsManagerMBean {
  public void setTopologyChangeHandler(TopologyChangeHandler handler);
  public void clearTopologyChangeHandler();
}