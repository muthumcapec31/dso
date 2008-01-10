/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.statistics;

import org.apache.commons.lang.ClassUtils;

import com.tc.exception.TCRuntimeException;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatisticData implements Serializable {
  private String agentIp;
  private Date moment;
  private String name;
  private int element;
  private Object data;
  
  public static StatisticData buildInstanceForClassAtLocalhost(Class klass, Date moment, Long value) {
    return _buildInstanceForClassAtLocalhost(klass, moment, value);
  }
  
  public static StatisticData buildInstanceForClassAtLocalhost(Class klass, Date moment, String value) {
    return _buildInstanceForClassAtLocalhost(klass, moment, value);
  }
  
  public static StatisticData buildInstanceForClassAtLocalhost(Class klass, Date moment, Date value) {
    return _buildInstanceForClassAtLocalhost(klass, moment, value);
  }
  
  private static StatisticData _buildInstanceForClassAtLocalhost(Class klass, Date moment, Object value) {
    try {
      return new StatisticData()
        .name(ClassUtils.getShortClassName(klass))
        .agentIp(InetAddress.getLocalHost().getHostAddress())
        .data(value)
        .moment(moment);
    } catch (UnknownHostException e) {
      throw new TCRuntimeException(e);
    }
  }

  public String getAgentIp() {
    return agentIp;
  }
  
  public Date getMoment() {
    return moment;
  }
  
  public String getName() {
    return name;
  }
  
  public int getElement() {
    return element;
  }
  
  public Object getData() {
    return data;
  }

  public void setAgentIp(String agentIp) {
    this.agentIp = agentIp;
  }
  
  public StatisticData agentIp(String agentIp) {
    setAgentIp(agentIp);
    return this;
  }
  
  public void setMoment(Date moment) {
    this.moment = moment;
  }
  
  public StatisticData moment(Date moment) {
    setMoment(moment);
    return this;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public StatisticData name(String name) {
    setName(name);
    return this;
  }
  
  public void setElement(int element) {
    this.element = element;
  }
  
  public StatisticData element(int element) {
    setElement(element);
    return this;
  }
  
  private void setData(Object data) {
    this.data = data;
  }
  
  private StatisticData data(Object data) {
    setData(data);
    return this;
  }
  
  public void setData(Long data) {
    setData((Object)data);
  }
  
  public StatisticData data(Long data) {
    return data((Object)data);
  }
  
  public void setData(String data) {
    setData((Object)data);
  }
  
  public StatisticData data(String data) {
    return data((Object)data);
  }
  
  public void setData(Date data) {
    setData((Object)data);
  }
  
  public StatisticData data(Date data) {
    return data((Object)data);
  }
  
  public String toString() {
    return "["
      +"agentIp = "+agentIp+"; "
      +"moment = "+SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(moment)+"; "
      +"name = "+name+"; "
      +"element = "+element+"; "
      +"data = "+(data != null && data instanceof Date ? SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(data) : data)+""
      +"]";
  }
}